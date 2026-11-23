package com.neo.recommendation_service.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neo.recommendation_service.dtos.response.RecommendedMovieResponse;
import com.neo.recommendation_service.repositories.RecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    @Transactional(readOnly = true)
    public List<RecommendedMovieResponse> getPersonalizedRecommendations(String userId, String genre, Integer year, int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), 50);
        String normalizedGenre = (genre != null && !genre.isBlank()) ? genre.trim().toLowerCase() : null;

        // Anonymous or unauthenticated: return trending
        if (userId == null || userId.isBlank()) {
            return recommendationRepository.recommendTrending(null, normalizedGenre, year, boundedLimit);
        }

        long ratingCount = recommendationRepository.countUserRatings(userId);
        if (ratingCount == 0) {
            log.info("User {} has 0 ratings (cold-start). Returning top rated movies.", userId);
            return recommendationRepository.recommendTrending(userId, normalizedGenre, year, boundedLimit);
        }

        // Primary: Collaborative filtering (users with similar taste)
        List<RecommendedMovieResponse> results = new ArrayList<>(
                recommendationRepository.recommendCollaborative(userId, normalizedGenre, year, boundedLimit)
        );

        // Fallback: fill remaining slots with top rated movies
        if (results.size() < boundedLimit) {
            Set<String> alreadyIncluded = results.stream().map(RecommendedMovieResponse::getId).collect(Collectors.toSet());
            List<RecommendedMovieResponse> trending = recommendationRepository.recommendTrending(
                    userId, normalizedGenre, year, boundedLimit - results.size()
            );
            for (var m : trending) {
                if (alreadyIncluded.add(m.getId())) {
                    results.add(m);
                }
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<RecommendedMovieResponse> getSimilarMovies(String movieId, int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), 20);
        return recommendationRepository.recommendSimilarMovies(movieId, boundedLimit);
    }

    @Transactional(readOnly = true)
    public List<RecommendedMovieResponse> getTrendingMovies(String genre, Integer year, int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), 50);
        String normalizedGenre = (genre != null && !genre.isBlank()) ? genre.trim().toLowerCase() : null;
        return recommendationRepository.recommendTrending(null, normalizedGenre, year, boundedLimit);
    }
}
