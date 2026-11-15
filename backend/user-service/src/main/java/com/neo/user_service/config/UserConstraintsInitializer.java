package com.neo.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserConstraintsInitializer implements CommandLineRunner {
    private final Neo4jClient neo4jClient;

    @Override
    public void run(String... args) {
        neo4jClient.query("CREATE CONSTRAINT user_email_unique IF NOT EXISTS FOR (u:User) REQUIRE u.email IS UNIQUE")
                .run();

        neo4jClient.query("CREATE CONSTRAINT user_username_unique IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE")
                .run();
    }
}
