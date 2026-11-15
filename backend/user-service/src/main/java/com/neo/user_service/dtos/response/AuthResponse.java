package com.neo.user_service.dtos.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        boolean twoFactorRequired
) {
}
