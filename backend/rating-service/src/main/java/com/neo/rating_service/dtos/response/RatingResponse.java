package com.neo.rating_service.dtos.response;

import java.time.Instant;

import com.neo.rating_service.models.Rating;

import lombok.Builder;

@Builder
public record RatingResponse(
        String movieId,
        Integer score,
        Instant createdAt,
        Instant updatedAt
) {
    public static RatingResponse from(Rating rating) {
        return RatingResponse.builder()
                .movieId(rating.getMovieId())
                .score(rating.getScore())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
