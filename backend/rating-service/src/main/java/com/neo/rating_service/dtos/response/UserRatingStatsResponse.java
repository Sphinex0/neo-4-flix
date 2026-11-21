package com.neo.rating_service.dtos.response;

import lombok.Builder;

@Builder
public record UserRatingStatsResponse(
        String userId,
        long ratingsCount,
        Double averageRating
) {
}
