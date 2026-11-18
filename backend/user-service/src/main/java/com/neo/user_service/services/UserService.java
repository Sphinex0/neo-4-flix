package com.neo.user_service.services;

import java.time.Instant;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.ChangePasswordRequest;
import com.neo.user_service.dtos.request.ProfileRequest;
import com.neo.user_service.dtos.request.VerifyPasswordRequest;
import com.neo.user_service.dtos.response.FriendResponse;
import com.neo.user_service.dtos.response.TwoFactorSetupResponse;
import com.neo.user_service.dtos.response.UserResponse;
import com.neo.user_service.exceptions.auth.InvalidCredentialsException;
import com.neo.user_service.exceptions.user.EmailAlreadyExistsException;
import com.neo.user_service.exceptions.user.UserNotFoundException;
import com.neo.user_service.exceptions.user.UsernameAlreadyExistsException;
import com.neo.user_service.models.User;
import com.neo.user_service.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final PasswordEncoder passwordEncoder;

    public ResponseData<UserResponse> getMe(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return ResponseData.success("User fetched successfully", UserResponse.from(user));
    }

    public ResponseData<UserResponse> updateMe(String userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        return ResponseData.success("Profile updated successfully", UserResponse.from(savedUser));
    }

    public ResponseData<String> changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseData.success("Password updated successfully", null);
    }

    public ResponseData<String> verifyPassword(String userId, VerifyPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password");
        }

        return ResponseData.success("Password verified", null);
    }

    public ResponseData<String> deleteMe(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(userId);

        return ResponseData.success("Account deleted successfully", null);
    }

    public ResponseData<List<FriendResponse>> getMyFriends(String userId) {
        List<FriendResponse> friends = userRepository.findFriends(userId).stream()
                .map(FriendResponse::from)
                .toList();

        return ResponseData.success("Friends fetched successfully", friends);
    }

    public ResponseData<FriendResponse> addFriend(String userId, String friendId) {
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.addFriend(userId, friendId);

        return ResponseData.success("Friend added successfully", FriendResponse.from(friend));
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

    public ResponseData<String> disableTwoFactor(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseData.success("Two-factor authentication disabled", null);
    }
}
