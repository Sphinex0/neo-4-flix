package com.neo.recommendation_service.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neo.recommendation_service.common.ResponseData;
import com.neo.recommendation_service.dtos.response.RecommendedMovieResponse;
import com.neo.recommendation_service.services.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ResponseData<List<RecommendedMovieResponse>>> getPersonalizedRecommendations(
            @RequestHeader(value = "Neo-User-Id", required = false) String userId,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<RecommendedMovieResponse> recommendations =
                recommendationService.getPersonalizedRecommendations(userId, genre, year, limit);
        return ResponseEntity.ok(ResponseData.success("Recommendations retrieved successfully", recommendations));
    }

    @GetMapping("/trending")
    public ResponseEntity<ResponseData<List<RecommendedMovieResponse>>> getTrendingMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<RecommendedMovieResponse> trending = recommendationService.getTrendingMovies(genre, year, limit);
        return ResponseEntity.ok(ResponseData.success("Trending movies retrieved successfully", trending));
    }

    @GetMapping("/movies/{movieId}")
    public ResponseEntity<ResponseData<List<RecommendedMovieResponse>>> getSimilarMovies(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<RecommendedMovieResponse> similar = recommendationService.getSimilarMovies(movieId, limit);
        return ResponseEntity.ok(ResponseData.success("Similar movies retrieved successfully", similar));
    }
}
