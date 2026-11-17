package com.neo.movie_service.dtos.response;

import java.time.Instant;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieResponse {
    private String id;
    private String title;
    private String description;
    private Integer releaseYear;
    private Integer durationMinutes;
    private String posterUrl;
    private String trailerUrl;
    private String director;
    private Double imdbRating;
    private Set<GenreResponse> genres;
    private Instant createdAt;
    private Instant updatedAt;
}
