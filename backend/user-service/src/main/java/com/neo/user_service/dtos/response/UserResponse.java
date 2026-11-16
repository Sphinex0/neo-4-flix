package com.neo.user_service.dtos.response;

import java.time.Instant;

import com.neo.user_service.models.User;
import com.neo.user_service.models.UserRole;

import lombok.Builder;

@Builder
public record UserResponse(
        String id,
        String firstName,
        String lastName,
        String username,
        String email,
        UserRole role,
        boolean twoFactorEnabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
