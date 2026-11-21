package com.neo.rating_service.dtos.response;

import java.time.Instant;
import java.util.List;

import lombok.Builder;

@Builder
public record RatingComparisonResponse(
        long bothRatedCount,
        Double tasteMatchPercent,
        List<SharedRatingItem> bothLoved,
        List<OtherUserRatingItem> recentRatings
) {
    public record SharedRatingItem(String movieId, int myScore, int theirScore) {
    }

    public record OtherUserRatingItem(String movieId, int theirScore, Instant ratedAt) {
    }
}
