package com.neo.user_service.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyPasswordRequest(
        @NotBlank(message = "Password is required")
        String password
) {
}
