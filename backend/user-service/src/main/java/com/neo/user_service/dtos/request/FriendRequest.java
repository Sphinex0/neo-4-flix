package com.neo.user_service.dtos.request;

import jakarta.validation.constraints.NotBlank;


public record FriendRequest(
        @NotBlank(message = "Friend id is required")
        String friendId
) {
}
