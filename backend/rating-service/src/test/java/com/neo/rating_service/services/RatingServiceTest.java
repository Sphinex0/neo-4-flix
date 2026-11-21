package com.neo.rating_service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neo.rating_service.common.ResponseData;
import com.neo.rating_service.dtos.request.ChangeRatingRequest;
import com.neo.rating_service.dtos.request.RatingRequest;
import com.neo.rating_service.dtos.response.RatingResponse;
import com.neo.rating_service.exceptions.rating.RatingAlreadyExistsException;
import com.neo.rating_service.exceptions.rating.RatingNotFoundException;
import com.neo.rating_service.models.Rating;
import com.neo.rating_service.repositories.RatingRepository;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingService ratingService;

    private Rating existingRating;

    private Rating buildRating(Integer score) {
        return Rating.builder()
                .userId("user-1")
                .movieId("movie-1")
                .score(score)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void rateMovie_savesRating_whenNotAlreadyRated() {
        existingRating = buildRating(5);
        RatingRequest request = new RatingRequest("movie-1", 5);
        when(ratingRepository.findByUserIdAndMovieId("user-1", "movie-1")).thenReturn(Optional.empty());
        when(ratingRepository.save("user-1", "movie-1", 5)).thenReturn(existingRating);

        ResponseData<RatingResponse> response = ratingService.rateMovie("user-1", request);

        assertEquals(5, response.getData().score());
    }

    @Test
    void rateMovie_throwsException_whenAlreadyRated() {
        existingRating = buildRating(4);
        RatingRequest request = new RatingRequest("movie-1", 5);
        when(ratingRepository.findByUserIdAndMovieId("user-1", "movie-1")).thenReturn(Optional.of(existingRating));

        assertThrows(RatingAlreadyExistsException.class, () -> ratingService.rateMovie("user-1", request));
    }

    @Test
    void changeRating_updatesScore_whenRatingExists() {
        existingRating = buildRating(3);
        Rating updatedRating = buildRating(2);
        ChangeRatingRequest request = new ChangeRatingRequest(2);
        when(ratingRepository.findByUserIdAndMovieId("user-1", "movie-1")).thenReturn(Optional.of(existingRating));
        when(ratingRepository.save("user-1", "movie-1", 2)).thenReturn(updatedRating);

        ResponseData<RatingResponse> response = ratingService.changeRating("user-1", "movie-1", request);

        assertEquals(2, response.getData().score());
    }

    @Test
    void changeRating_throwsException_whenRatingNotFound() {
        ChangeRatingRequest request = new ChangeRatingRequest(2);
        when(ratingRepository.findByUserIdAndMovieId("user-1", "movie-1")).thenReturn(Optional.empty());

        assertThrows(RatingNotFoundException.class, () -> ratingService.changeRating("user-1", "movie-1", request));
    }

    @Test
    void deleteRating_deletesRating_whenRatingExists() {
        existingRating = buildRating(5);
        when(ratingRepository.findByUserIdAndMovieId("user-1", "movie-1")).thenReturn(Optional.of(existingRating));

        ratingService.deleteRating("user-1", "movie-1");

        verify(ratingRepository).deleteByUserIdAndMovieId("user-1", "movie-1");
    }

    @Test
    void deleteRating_throwsException_whenRatingNotFound() {
        when(ratingRepository.findByUserIdAndMovieId("user-1", "movie-1")).thenReturn(Optional.empty());

        assertThrows(RatingNotFoundException.class, () -> ratingService.deleteRating("user-1", "movie-1"));
        verify(ratingRepository, never()).deleteByUserIdAndMovieId("user-1", "movie-1");
    }
}
