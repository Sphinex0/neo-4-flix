package com.neo.recommendation_service.dtos.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedMovieResponse {
    private String id;
    private String title;
    private String description;
    private Integer releaseYear;
    private Integer durationMinutes;
    private String posterUrl;
    private String trailerUrl;
    private String director;
    private Double imdbRating;
    private List<String> genres;
    private String recommendationReason;
    private Double matchScore;
}
