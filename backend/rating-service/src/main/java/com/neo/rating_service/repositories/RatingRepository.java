package com.neo.rating_service.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.neo.rating_service.dtos.response.RatingComparisonResponse;
import com.neo.rating_service.dtos.response.UserRatingStatsResponse;
import com.neo.rating_service.exceptions.movie.MovieNotFoundException;
import com.neo.rating_service.models.Rating;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RatingRepository {

    private final Neo4jClient neo4jClient;

    public Rating save(String userId, String movieId, Integer score) {
        String cypher = """
                MATCH (u:User {id: $userId}), (m:Movie {movieId: $movieId})
                MERGE (u)-[r:RATED]->(m)
                ON CREATE SET r.createdAt = $now
                SET r.score = $score, r.updatedAt = $now
                RETURN u.id AS userId, m.movieId AS movieId, r.score AS score, r.createdAt AS createdAt, r.updatedAt AS updatedAt
                """;

        return neo4jClient.query(cypher)
                .bindAll(Map.of(
                        "userId", userId,
                        "movieId", movieId,
                        "score", score,
                        "now", Instant.now()))
                .fetchAs(Rating.class)
                .mappedBy((typeSystem, record) -> toRating(record))
                .one()
                .orElseThrow(() -> new MovieNotFoundException("Movie not found: " + movieId));
    }

    public Optional<Rating> findByUserIdAndMovieId(String userId, String movieId) {
        String cypher = """
                MATCH (u:User {id: $userId})-[r:RATED]->(m:Movie {movieId: $movieId})
                RETURN u.id AS userId, m.movieId AS movieId, r.score AS score, r.createdAt AS createdAt, r.updatedAt AS updatedAt
                """;

        return neo4jClient.query(cypher)
                .bindAll(Map.of("userId", userId, "movieId", movieId))
                .fetchAs(Rating.class)
                .mappedBy((typeSystem, record) -> toRating(record))
                .one();
    }

    public List<Rating> findAllByUserId(String userId) {
        String cypher = """
                MATCH (u:User {id: $userId})-[r:RATED]->(m:Movie)
                RETURN u.id AS userId, m.movieId AS movieId, r.score AS score, r.createdAt AS createdAt, r.updatedAt AS updatedAt
                """;

        return neo4jClient.query(cypher)
                .bindAll(Map.of("userId", userId))
                .fetchAs(Rating.class)
                .mappedBy((typeSystem, record) -> toRating(record))
                .all()
                .stream()
                .toList();
    }

    public List<Rating> findAllByMovieId(String movieId) {
        String cypher = """
                MATCH (u:User)-[r:RATED]->(m:Movie {movieId: $movieId})
                RETURN u.id AS userId, m.movieId AS movieId, r.score AS score, r.createdAt AS createdAt, r.updatedAt AS updatedAt
                """;

        return neo4jClient.query(cypher)
                .bindAll(Map.of("movieId", movieId))
                .fetchAs(Rating.class)
                .mappedBy((typeSystem, record) -> toRating(record))
                .all()
                .stream()
                .toList();
    }

    public UserRatingStatsResponse getStatsByUserId(String userId) {
        String cypher = """
                MATCH (u:User {id: $userId})-[r:RATED]->(:Movie)
                RETURN count(r) AS ratingsCount, avg(r.score) AS averageRating
                """;

        return neo4jClient.query(cypher)
                .bind(userId).to("userId")
                .fetchAs(UserRatingStatsResponse.class)
                .mappedBy((typeSystem, record) -> toStats(userId, record))
                .one()
                .orElse(UserRatingStatsResponse.builder().userId(userId).ratingsCount(0).averageRating(null).build());
    }

    public List<RatingComparisonResponse.SharedRatingItem> findCommonRatings(String viewerId, String otherUserId) {
        String cypher = """
                MATCH (me:User {id: $viewerId})-[r1:RATED]->(m:Movie)<-[r2:RATED]-(other:User {id: $otherUserId})
                RETURN m.movieId AS movieId, r1.score AS myScore, r2.score AS theirScore
                """;

        return neo4jClient.query(cypher)
                .bindAll(Map.of("viewerId", viewerId, "otherUserId", otherUserId))
                .fetchAs(RatingComparisonResponse.SharedRatingItem.class)
                .mappedBy((typeSystem, record) -> new RatingComparisonResponse.SharedRatingItem(
                        record.get("movieId").asString(),
                        record.get("myScore").asInt(),
                        record.get("theirScore").asInt()))
                .all()
                .stream()
                .toList();
    }

    public List<RatingComparisonResponse.OtherUserRatingItem> findRecentOtherRatings(
            String viewerId, String otherUserId, int limit) {
        String cypher = """
                MATCH (other:User {id: $otherUserId})-[r:RATED]->(m:Movie)
                WHERE NOT EXISTS { MATCH (:User {id: $viewerId})-[:RATED]->(m) }
                RETURN m.movieId AS movieId, r.score AS theirScore, r.updatedAt AS ratedAt
                ORDER BY r.updatedAt DESC
                LIMIT $limit
                """;

        return neo4jClient.query(cypher)
                .bindAll(Map.of("viewerId", viewerId, "otherUserId", otherUserId, "limit", limit))
                .fetchAs(RatingComparisonResponse.OtherUserRatingItem.class)
                .mappedBy((typeSystem, record) -> new RatingComparisonResponse.OtherUserRatingItem(
                        record.get("movieId").asString(),
                        record.get("theirScore").asInt(),
                        record.get("ratedAt").asZonedDateTime().toInstant()))
                .all()
                .stream()
                .toList();
    }

    public void deleteByUserIdAndMovieId(String userId, String movieId) {
        String cypher = """
                MATCH (u:User {id: $userId})-[r:RATED]->(m:Movie {movieId: $movieId})
                DELETE r
                """;

        neo4jClient.query(cypher)
                .bindAll(Map.of("userId", userId, "movieId", movieId))
                .run();
    }

    private Rating toRating(Record record) {
        return Rating.builder()
                .userId(record.get("userId").asString())
                .movieId(record.get("movieId").asString())
                .score(record.get("score").asInt())
                .createdAt(record.get("createdAt").asZonedDateTime().toInstant())
                .updatedAt(record.get("updatedAt").asZonedDateTime().toInstant())
                .build();
    }

    private UserRatingStatsResponse toStats(String userId, Record record) {
        long count = record.get("ratingsCount").asLong();
        Value averageVal = record.get("averageRating");
        Double average = averageVal.isNull() ? null : Math.round(averageVal.asDouble() * 10) / 10.0;

        return UserRatingStatsResponse.builder()
                .userId(userId)
                .ratingsCount(count)
                .averageRating(average)
                .build();
    }
}
