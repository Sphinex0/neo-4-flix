package com.neo.movie_service.dtos.request;

import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieCreateRequest {

    @NotBlank(message = "Movie title is required")
    private String title;

    @NotBlank(message = "Movie description is required")
    private String description;

    @NotNull(message = "Release year is required")
    @Min(value = 1888, message = "Release year must be valid")
    @Max(value = 2100, message = "Release year cannot be in far future")
    private Integer releaseYear;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private String posterUrl;

    private String trailerUrl;

    private String director;

    private Set<String> genres;
}
