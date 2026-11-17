package com.neo.movie_service.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
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
    @Property("movieId")
    @EqualsAndHashCode.Include
    private String id;

    @Property("title")
    private String title;

    @Property("plot")
    private String description;

    @Property("year")
    private Integer releaseYear;

    @Property("runtime")
    private Integer durationMinutes;

    @Property("poster")
    private String posterUrl;

    @Property("trailerUrl")
    private String trailerUrl;

    @Property("director")
    private String director;

    @Property("imdbRating")
    private Double imdbRating;

    @Property("createdAt")
    private Instant createdAt;

    @Property("updatedAt")
    private Instant updatedAt;

    @Relationship(type = "IN_GENRE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
}
