package com.neo.user_service.dtos.response;

import lombok.Builder;

@Builder
public record FriendResponse(
        String id,
        String firstName,
        String lastName,
        String username
) {
}
