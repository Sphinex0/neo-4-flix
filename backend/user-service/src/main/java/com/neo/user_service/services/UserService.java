package com.neo.user_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.ProfileRequest;
import com.neo.user_service.dtos.response.FriendResponse;
import com.neo.user_service.dtos.response.UserResponse;

@Service
public class UserService {

    public ResponseData<UserResponse> getMe(String userId) {
        return null;
    }

    public ResponseData<UserResponse> updateMe(String userId, ProfileRequest request) {
        return null;
    }

    public ResponseData<String> deleteMe(String userId) {
        return null;
    }

    public ResponseData<List<FriendResponse>> getMyFriends(String userId) {
        return null;
    }
}
