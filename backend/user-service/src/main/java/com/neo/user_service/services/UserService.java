package com.neo.user_service.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.ProfileRequest;
import com.neo.user_service.dtos.response.FriendResponse;
import com.neo.user_service.dtos.response.TwoFactorSetupResponse;
import com.neo.user_service.dtos.response.UserResponse;
import com.neo.user_service.exceptions.user.UserNotFoundException;
import com.neo.user_service.models.User;
import com.neo.user_service.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;

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

    public ResponseData<TwoFactorSetupResponse> enableTwoFactor(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String secret = twoFactorAuthService.generateSecret();

        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        TwoFactorSetupResponse response = TwoFactorSetupResponse.builder()
                .secret(secret)
                .qrCodeUri(twoFactorAuthService.buildQrCodeUri(user.getUsername(), secret))
                .build();

        return ResponseData.success("Two-factor authentication enabled", response);
    }
}
