package com.neo.movie_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieConstraintsInitializer implements CommandLineRunner {

    private final Neo4jClient neo4jClient;

    @Override
    public void run(String... args) {
        log.info("Applying Neo4j constraints and indexes for Movie and Genre...");

        neo4jClient.query("CREATE CONSTRAINT movie_id_unique IF NOT EXISTS FOR (m:Movie) REQUIRE m.id IS UNIQUE")
                .run();

        neo4jClient.query("CREATE CONSTRAINT genre_id_unique IF NOT EXISTS FOR (g:Genre) REQUIRE g.id IS UNIQUE")
                .run();

        neo4jClient.query("CREATE CONSTRAINT genre_name_unique IF NOT EXISTS FOR (g:Genre) REQUIRE g.name IS UNIQUE")
                .run();

        neo4jClient.query("CREATE INDEX movie_title_index IF NOT EXISTS FOR (m:Movie) ON (m.title)")
                .run();

        neo4jClient.query("CREATE INDEX movie_year_index IF NOT EXISTS FOR (m:Movie) ON (m.releaseYear)")
                .run();

        log.info("Neo4j Movie and Genre constraints/indexes applied successfully.");
    }
}
