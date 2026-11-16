package com.neo.movie_service.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Node("Movie")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@ToString
public class Movie {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String title;

    private String description;

    private Integer releaseYear;

    private Integer durationMinutes;

    private String posterUrl;

    private String trailerUrl;

    private String director;

    private Instant createdAt;

    private Instant updatedAt;

    @Relationship(type = "IN_GENRE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
}
