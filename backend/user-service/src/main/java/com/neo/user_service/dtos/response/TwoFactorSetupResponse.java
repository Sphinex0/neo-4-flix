package com.neo.user_service.dtos.response;

import lombok.Builder;

@Builder
public record TwoFactorSetupResponse(
        String secret,
        String qrCodeUri
) {
}
