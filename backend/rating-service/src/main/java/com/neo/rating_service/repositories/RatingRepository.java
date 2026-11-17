package com.neo.rating_service.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.driver.Record;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

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
                .orElseThrow(() -> new IllegalStateException("User or movie not found"));
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
}
