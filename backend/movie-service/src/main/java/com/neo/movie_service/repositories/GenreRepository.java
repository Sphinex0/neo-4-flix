package com.neo.movie_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.neo.movie_service.models.Genre;

@Repository
public interface GenreRepository extends Neo4jRepository<Genre, String> {

    Optional<Genre> findByNameIgnoreCase(String name);

    List<Genre> findAllByOrderByNameAsc();
}
