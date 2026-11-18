package com.neo.user_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmTwoFactorRequest(
        @NotBlank(message = "Code is required")
        @Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
        String code
) {
}
