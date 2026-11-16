package com.neo.user_service.dtos.response;

import com.neo.user_service.models.User;

import lombok.Builder;

@Builder
public record FriendResponse(
        String id,
        String firstName,
        String lastName,
        String username
) {
    public static FriendResponse from(User friend) {
        return FriendResponse.builder()
                .id(friend.getId())
                .firstName(friend.getFirstName())
                .lastName(friend.getLastName())
                .username(friend.getUsername())
                .build();
    }
}
