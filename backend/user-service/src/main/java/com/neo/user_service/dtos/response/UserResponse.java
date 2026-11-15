package com.neo.user_service.dtos.response;

import java.time.Instant;

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
}
