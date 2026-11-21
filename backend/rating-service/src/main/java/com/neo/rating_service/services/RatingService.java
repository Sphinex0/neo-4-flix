package com.neo.rating_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neo.rating_service.common.ResponseData;
import com.neo.rating_service.dtos.request.ChangeRatingRequest;
import com.neo.rating_service.dtos.request.RatingRequest;
import com.neo.rating_service.dtos.response.MovieRatingResponse;
import com.neo.rating_service.dtos.response.MyRatingResponse;
import com.neo.rating_service.dtos.response.RatingComparisonResponse;
import com.neo.rating_service.dtos.response.RatingResponse;
import com.neo.rating_service.dtos.response.UserRatingStatsResponse;
import com.neo.rating_service.exceptions.rating.RatingAlreadyExistsException;
import com.neo.rating_service.exceptions.rating.RatingNotFoundException;
import com.neo.rating_service.models.Rating;
import com.neo.rating_service.repositories.RatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingService {
    private static final int RECENT_RATINGS_LIMIT = 5;
    private static final int BOTH_LOVED_LIMIT = 12;

    private final RatingRepository ratingRepository;

    public ResponseData<RatingResponse> rateMovie(String userId, RatingRequest request) {
        boolean alreadyRated = ratingRepository.findByUserIdAndMovieId(userId, request.movieId()).isPresent();
        if (alreadyRated) {
            throw new RatingAlreadyExistsException("You already rated this movie, use update instead");
        }

        Rating rating = ratingRepository.save(userId, request.movieId(), request.score());

        return ResponseData.success("Rating added successfully", RatingResponse.from(rating));
    }

    public ResponseData<RatingResponse> changeRating(String userId, String movieId, ChangeRatingRequest request) {
        ratingRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new RatingNotFoundException("Rating not found"));

        Rating rating = ratingRepository.save(userId, movieId, request.score());

        return ResponseData.success("Rating updated successfully", RatingResponse.from(rating));
    }

    public ResponseData<String> deleteRating(String userId, String movieId) {
        ratingRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new RatingNotFoundException("Rating not found"));

        ratingRepository.deleteByUserIdAndMovieId(userId, movieId);

        return ResponseData.success("Rating removed successfully", null);
    }

    public ResponseData<List<MyRatingResponse>> getMyRatings(String userId) {
        List<MyRatingResponse> ratings = ratingRepository.findAllByUserId(userId).stream()
                .map(MyRatingResponse::from)
                .toList();

        return ResponseData.success("Ratings fetched successfully", ratings);
    }

    public ResponseData<List<MovieRatingResponse>> getMovieRatings(String movieId) {
        List<MovieRatingResponse> ratings = ratingRepository.findAllByMovieId(movieId).stream()
                .map(MovieRatingResponse::from)
                .toList();

        return ResponseData.success("Ratings fetched successfully", ratings);
    }

    public ResponseData<UserRatingStatsResponse> getUserRatingStats(String userId) {
        UserRatingStatsResponse stats = ratingRepository.getStatsByUserId(userId);

        return ResponseData.success("Rating stats fetched successfully", stats);
    }

    public ResponseData<RatingComparisonResponse> getComparison(String viewerId, String otherUserId) {
        List<RatingComparisonResponse.SharedRatingItem> common =
                ratingRepository.findCommonRatings(viewerId, otherUserId);

        long bothRatedCount = common.size();
        Double tasteMatchPercent = null;

        if (bothRatedCount > 0) {
            double averageDifference = common.stream()
                    .mapToInt(item -> Math.abs(item.myScore() - item.theirScore()))
                    .average()
                    .orElse(0);
            tasteMatchPercent = (double) Math.round(100 - (averageDifference / 4.0 * 100));
        }

        List<RatingComparisonResponse.SharedRatingItem> bothLoved = common.stream()
                .filter(item -> item.myScore() >= 4 && item.theirScore() >= 4)
                .sorted((a, b) -> (b.myScore() + b.theirScore()) - (a.myScore() + a.theirScore()))
                .limit(BOTH_LOVED_LIMIT)
                .toList();

        List<RatingComparisonResponse.OtherUserRatingItem> recentRatings =
                ratingRepository.findRecentOtherRatings(viewerId, otherUserId, RECENT_RATINGS_LIMIT);

        RatingComparisonResponse response = RatingComparisonResponse.builder()
                .bothRatedCount(bothRatedCount)
                .tasteMatchPercent(tasteMatchPercent)
                .bothLoved(bothLoved)
                .recentRatings(recentRatings)
                .build();

        return ResponseData.success("Rating comparison fetched successfully", response);
    }
}
