package com.neo.recommendation_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neo.recommendation_service.dtos.response.RecommendedMovieResponse;
import com.neo.recommendation_service.repositories.RecommendationRepository;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    @DisplayName("Should return trending movies for anonymous users")
    void shouldReturnTrendingForAnonymous() {
        RecommendedMovieResponse movie = RecommendedMovieResponse.builder()
                .id("m-1")
                .title("Inception")
                .imdbRating(8.8)
                .build();

        when(recommendationRepository.recommendTrending(eq(null), eq(null), eq(10)))
                .thenReturn(List.of(movie));

        List<RecommendedMovieResponse> result = recommendationService.getPersonalizedRecommendations(null, null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("Should fall back to trending for users with 0 ratings (cold start)")
    void shouldFallbackToTrendingForColdStart() {
        when(recommendationRepository.countUserRatings("user-1")).thenReturn(0L);

        RecommendedMovieResponse movie = RecommendedMovieResponse.builder()
                .id("m-1")
                .title("Interstellar")
                .imdbRating(8.7)
                .build();

        when(recommendationRepository.recommendTrending(eq("user-1"), eq(null), eq(10)))
                .thenReturn(List.of(movie));

        List<RecommendedMovieResponse> result = recommendationService.getPersonalizedRecommendations("user-1", null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Interstellar");
        verify(recommendationRepository).recommendTrending(eq("user-1"), eq(null), eq(10));
    }

    @Test
    @DisplayName("Should return collaborative recommendations when user has ratings")
    void shouldReturnCollaborativeRecommendations() {
        when(recommendationRepository.countUserRatings("user-1")).thenReturn(5L);

        RecommendedMovieResponse movie1 = RecommendedMovieResponse.builder()
                .id("m-1")
                .title("The Dark Knight")
                .matchScore(4.0)
                .build();
        RecommendedMovieResponse movie2 = RecommendedMovieResponse.builder()
                .id("m-2")
                .title("Memento")
                .matchScore(3.0)
                .build();

        when(recommendationRepository.recommendCollaborative(eq("user-1"), eq(null), eq(2)))
                .thenReturn(List.of(movie1, movie2));

        List<RecommendedMovieResponse> result = recommendationService.getPersonalizedRecommendations("user-1", null, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("The Dark Knight");
        assertThat(result.get(1).getTitle()).isEqualTo("Memento");
    }

    @Test
    @DisplayName("Should augment with genre recommendations if collaborative is below limit")
    void shouldAugmentWithGenreRecommendations() {
        when(recommendationRepository.countUserRatings("user-1")).thenReturn(3L);

        RecommendedMovieResponse movie1 = RecommendedMovieResponse.builder()
                .id("m-1")
                .title("Movie 1")
                .build();
        RecommendedMovieResponse movie2 = RecommendedMovieResponse.builder()
                .id("m-2")
                .title("Movie 2")
                .build();

        when(recommendationRepository.recommendCollaborative("user-1", null, 2))
                .thenReturn(List.of(movie1));
        when(recommendationRepository.recommendFromFriends("user-1", null, 1))
                .thenReturn(List.of());
        when(recommendationRepository.recommendByFavoriteGenres("user-1", null, 1))
                .thenReturn(List.of(movie2));

        List<RecommendedMovieResponse> result = recommendationService.getPersonalizedRecommendations("user-1", null, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Movie 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Movie 2");
    }

    @Test
    @DisplayName("Should return similar movies by movieId")
    void shouldReturnSimilarMovies() {
        RecommendedMovieResponse similar = RecommendedMovieResponse.builder()
                .id("m-2")
                .title("The Matrix Reloaded")
                .build();

        when(recommendationRepository.recommendSimilarMovies("m-1", 5))
                .thenReturn(List.of(similar));

        List<RecommendedMovieResponse> result = recommendationService.getSimilarMovies("m-1", 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("The Matrix Reloaded");
    }
}
