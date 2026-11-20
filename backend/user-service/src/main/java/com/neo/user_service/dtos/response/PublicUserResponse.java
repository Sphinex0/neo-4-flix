package com.neo.user_service.dtos.response;

import java.time.Instant;

import com.neo.user_service.models.User;
import com.neo.user_service.models.UserRole;

import lombok.Builder;

@Builder
public record PublicUserResponse(
        String id,
        String firstName,
        String lastName,
        String username,
        UserRole role,
        Instant createdAt
) {
    public static PublicUserResponse from(User user) {
        return PublicUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
