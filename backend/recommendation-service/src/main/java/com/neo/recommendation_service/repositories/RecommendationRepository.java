package com.neo.recommendation_service.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.neo.recommendation_service.dtos.response.RecommendedMovieResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RecommendationRepository {

    private final Neo4jClient neo4jClient;

    public long countUserRatings(String userId) {
        String cypher = """
                MATCH (u:User {id: $userId})-[r:RATED]->(:Movie)
                RETURN count(r) AS count
                """;
        return neo4jClient.query(cypher)
                .bind(userId).to("userId")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }

    public List<RecommendedMovieResponse> recommendCollaborative(String userId, String genre, int limit) {
        String cypher = """
                MATCH (u:User {id: $userId})-[r1:RATED]->(m:Movie)<-[r2:RATED]-(other:User)-[r3:RATED]->(rec:Movie)
                WHERE r1.score >= 4 AND r2.score >= 4 AND r3.score >= 4
                  AND other <> u
                  AND NOT (u)-[:RATED]->(rec)
                  AND NOT (u)-[:WANTS_TO_WATCH]->(rec)
                  AND ($genre IS NULL OR EXISTS { MATCH (rec)-[:IN_GENRE]->(g:Genre) WHERE toLower(g.name) = toLower($genre) })
                OPTIONAL MATCH (rec)-[:IN_GENRE]->(genre:Genre)
                RETURN rec.movieId AS id, rec.title AS title, rec.plot AS description,
                       rec.year AS releaseYear, rec.runtime AS durationMinutes,
                       rec.poster AS posterUrl, rec.trailerUrl AS trailerUrl,
                       rec.director AS director, rec.imdbRating AS imdbRating,
                       collect(DISTINCT genre.name) AS genres,
                       count(DISTINCT other) AS matchScore
                ORDER BY matchScore DESC, rec.imdbRating DESC
                LIMIT $limit
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("genre", genre);
        params.put("limit", limit);

        return neo4jClient.query(cypher)
                .bindAll(params)
                .fetchAs(RecommendedMovieResponse.class)
                .mappedBy((typeSystem, record) -> mapToRecommended(record, "Users with similar tastes also loved this"))
                .all()
                .stream()
                .toList();
    }

    public List<RecommendedMovieResponse> recommendTrending(String userId, String genre, int limit) {
        String cypher = """
                MATCH (rec:Movie)
                WHERE ($userId IS NULL OR (NOT EXISTS { MATCH (:User {id: $userId})-[:RATED]->(rec) }
                                      AND NOT EXISTS { MATCH (:User {id: $userId})-[:WANTS_TO_WATCH]->(rec) }))
                  AND ($genre IS NULL OR EXISTS { MATCH (rec)-[:IN_GENRE]->(g:Genre) WHERE toLower(g.name) = toLower($genre) })
                OPTIONAL MATCH (rec)-[:IN_GENRE]->(genre:Genre)
                RETURN rec.movieId AS id, rec.title AS title, rec.plot AS description,
                       rec.year AS releaseYear, rec.runtime AS durationMinutes,
                       rec.poster AS posterUrl, rec.trailerUrl AS trailerUrl,
                       rec.director AS director, rec.imdbRating AS imdbRating,
                       collect(DISTINCT genre.name) AS genres,
                       coalesce(rec.imdbRating, 0.0) AS matchScore
                ORDER BY rec.imdbRating DESC
                LIMIT $limit
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("genre", genre);
        params.put("limit", limit);

        return neo4jClient.query(cypher)
                .bindAll(params)
                .fetchAs(RecommendedMovieResponse.class)
                .mappedBy((typeSystem, record) -> mapToRecommended(record, "Top rated on Neo4flix"))
                .all()
                .stream()
                .toList();
    }

    public List<RecommendedMovieResponse> recommendSimilarMovies(String movieId, int limit) {
        String cypher = """
                MATCH (m:Movie {movieId: $movieId})-[:IN_GENRE]->(g:Genre)<-[:IN_GENRE]-(rec:Movie)
                WHERE rec.movieId <> $movieId
                OPTIONAL MATCH (rec)-[:IN_GENRE]->(genre:Genre)
                RETURN rec.movieId AS id, rec.title AS title, rec.plot AS description,
                       rec.year AS releaseYear, rec.runtime AS durationMinutes,
                       rec.poster AS posterUrl, rec.trailerUrl AS trailerUrl,
                       rec.director AS director, rec.imdbRating AS imdbRating,
                       collect(DISTINCT genre.name) AS genres,
                       count(DISTINCT g) AS matchScore
                ORDER BY matchScore DESC, rec.imdbRating DESC
                LIMIT $limit
                """;

        return neo4jClient.query(cypher)
                .bind(movieId).to("movieId")
                .bind(limit).to("limit")
                .fetchAs(RecommendedMovieResponse.class)
                .mappedBy((typeSystem, record) -> mapToRecommended(record, "Because you viewed this movie"))
                .all()
                .stream()
                .toList();
    }

    private RecommendedMovieResponse mapToRecommended(Record record, String defaultReason) {
        Value idVal = record.get("id");
        Value titleVal = record.get("title");
        Value plotVal = record.get("description");
        Value yearVal = record.get("releaseYear");
        Value runtimeVal = record.get("durationMinutes");
        Value posterVal = record.get("posterUrl");
        Value trailerVal = record.get("trailerUrl");
        Value directorVal = record.get("director");
        Value imdbVal = record.get("imdbRating");
        Value genresVal = record.get("genres");
        Value scoreVal = record.get("matchScore");

        return RecommendedMovieResponse.builder()
                .id(!idVal.isNull() ? idVal.asString() : null)
                .title(!titleVal.isNull() ? titleVal.asString() : "")
                .description(!plotVal.isNull() ? plotVal.asString() : "")
                .releaseYear(!yearVal.isNull() ? yearVal.asInt() : null)
                .durationMinutes(!runtimeVal.isNull() ? runtimeVal.asInt() : null)
                .posterUrl(!posterVal.isNull() ? posterVal.asString() : null)
                .trailerUrl(!trailerVal.isNull() ? trailerVal.asString() : null)
                .director(!directorVal.isNull() ? directorVal.asString() : null)
                .imdbRating(!imdbVal.isNull() ? imdbVal.asDouble() : null)
                .genres(!genresVal.isNull() ? genresVal.asList(Value::asString) : List.of())
                .recommendationReason(defaultReason)
                .matchScore(!scoreVal.isNull() ? scoreVal.asDouble() : 0.0)
                .build();
    }
}
