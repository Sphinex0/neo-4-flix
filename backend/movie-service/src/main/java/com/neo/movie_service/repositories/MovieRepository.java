package com.neo.movie_service.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.neo.movie_service.models.Movie;

@Repository
public interface MovieRepository extends Neo4jRepository<Movie, String> {

    boolean existsByTitleIgnoreCase(String title);

    @Query(
        value = "MATCH (m:Movie) " +
                "WHERE ($title IS NULL OR toLower(m.title) CONTAINS toLower($title)) " +
                "  AND ($year IS NULL OR m.year = $year) " +
                "  AND ($genre IS NULL OR EXISTS { " +
                "      MATCH (m)-[:IN_GENRE]->(g:Genre) " +
                "      WHERE toLower(g.name) = toLower($genre) " +
                "  }) " +
                "OPTIONAL MATCH (m)-[r:IN_GENRE]->(g:Genre) " +
                "RETURN m, collect(r), collect(g) " +
                ":#{orderBy(#pageable)} " +
                "SKIP $skip LIMIT $limit",
        countQuery = "MATCH (m:Movie) " +
                     "WHERE ($title IS NULL OR toLower(m.title) CONTAINS toLower($title)) " +
                     "  AND ($year IS NULL OR m.year = $year) " +
                     "  AND ($genre IS NULL OR EXISTS { " +
                     "      MATCH (m)-[:IN_GENRE]->(g:Genre) " +
                     "      WHERE toLower(g.name) = toLower($genre) " +
                     "  }) " +
                     "RETURN count(m)"
    )
    Page<Movie> searchMovies(
            @Param("title") String title,
            @Param("year") Integer year,
            @Param("genre") String genre,
            Pageable pageable
    );

    @Query(
        value = "MATCH (m:Movie {movieId: $id}) " +
                "OPTIONAL MATCH (m)-[r:IN_GENRE]->(g:Genre) " +
                "RETURN m, collect(r), collect(g)"
    )
    Optional<Movie> findByIdWithGenres(@Param("id") String id);

    @Query(
        "MATCH (u:User {id: $userId}), (m:Movie {movieId: $movieId}) " +
        "MERGE (u)-[:WANTS_TO_WATCH]->(m)"
    )
    void addToWatchlist(@Param("userId") String userId, @Param("movieId") String movieId);

    @Query(
        "MATCH (u:User {id: $userId})-[r:WANTS_TO_WATCH]->(m:Movie {movieId: $movieId}) " +
        "DELETE r"
    )
    void removeFromWatchlist(@Param("userId") String userId, @Param("movieId") String movieId);

    @Query(
        "MATCH (u:User {id: $userId})-[:WANTS_TO_WATCH]->(m:Movie {movieId: $movieId}) " +
        "RETURN COUNT(m) > 0"
    )
    boolean existsInWatchlist(@Param("userId") String userId, @Param("movieId") String movieId);

    @Query(
        value = "MATCH (u:User {id: $userId})-[:WANTS_TO_WATCH]->(m:Movie) " +
                "OPTIONAL MATCH (m)-[r:IN_GENRE]->(g:Genre) " +
                "RETURN m, collect(r), collect(g) " +
                ":#{orderBy(#pageable)} " +
                "SKIP $skip LIMIT $limit",
        countQuery = "MATCH (u:User {id: $userId})-[:WANTS_TO_WATCH]->(m:Movie) " +
                     "RETURN count(m)"
    )
    Page<Movie> findWatchlistByUserId(@Param("userId") String userId, Pageable pageable);

    @Query(
        "MATCH (u:User {id: $userId}), (m:Movie {movieId: $movieId}) " +
        "MERGE (u)-[r:SHARED_WITH]->(m) " +
        "SET r.sharedBy = $sharedBy, r.sharedAt = $sharedAt"
    )
    void shareMovie(
            @Param("userId") String recipientId,
            @Param("movieId") String movieId,
            @Param("sharedBy") String sharedBy,
            @Param("sharedAt") Instant sharedAt
    );
}
