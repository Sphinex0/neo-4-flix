package com.neo.movie_service.dtos.request;

import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieUpdateRequest {

    private String title;

    private String description;

    @Min(value = 1888, message = "Release year must be valid")
    @Max(value = 2100, message = "Release year cannot be in far future")
    private Integer releaseYear;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private String posterUrl;

    private String trailerUrl;

    private String director;

    private Set<String> genres;
}
