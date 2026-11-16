package com.neo.movie_service.repositories;

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
                "  AND ($year IS NULL OR m.releaseYear = $year) " +
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
                     "  AND ($year IS NULL OR m.releaseYear = $year) " +
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
        value = "MATCH (m:Movie {id: $id}) " +
                "OPTIONAL MATCH (m)-[r:IN_GENRE]->(g:Genre) " +
                "RETURN m, collect(r), collect(g)"
    )
    Optional<Movie> findByIdWithGenres(@Param("id") String id);
}
