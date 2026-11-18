package com.neo.rating_service.dtos.response;

import java.time.Instant;

import com.neo.rating_service.models.Rating;

import lombok.Builder;

@Builder
public record MovieRatingResponse(
        String userId,
        Integer score,
        Instant createdAt,
        Instant updatedAt
) {
    public static MovieRatingResponse from(Rating rating) {
        return MovieRatingResponse.builder()
                .userId(rating.getUserId())
                .score(rating.getScore())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
