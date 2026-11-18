package com.neo.user_service.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.ChangePasswordRequest;
import com.neo.user_service.dtos.request.ConfirmTwoFactorRequest;
import com.neo.user_service.dtos.request.ProfileRequest;
import com.neo.user_service.dtos.request.VerifyPasswordRequest;
import com.neo.user_service.dtos.response.FriendResponse;
import com.neo.user_service.dtos.response.TwoFactorSetupResponse;
import com.neo.user_service.dtos.response.UserResponse;
import com.neo.user_service.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<UserResponse>> getMe(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<UserResponse>> updateMe(@AuthenticationPrincipal String userId,
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(userService.updateMe(userId, request));
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<String>> changePassword(@AuthenticationPrincipal String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(userId, request));
    }

    @PostMapping("/me/verify-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<String>> verifyPassword(@AuthenticationPrincipal String userId,
            @Valid @RequestBody VerifyPasswordRequest request) {
        return ResponseEntity.ok(userService.verifyPassword(userId, request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<String>> deleteMe(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userService.deleteMe(userId));
    }

    @GetMapping("/me/friends")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<List<FriendResponse>>> getMyFriends(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userService.getMyFriends(userId));
    }

    @PostMapping("/me/friends/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<FriendResponse>> addFriend(@AuthenticationPrincipal String userId,
            @PathVariable String id) {
        return ResponseEntity.ok(userService.addFriend(userId, id));
    }

    @PostMapping("/me/2fa/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<TwoFactorSetupResponse>> enableTwoFactor(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userService.enableTwoFactor(userId));
    }

    @PostMapping("/me/2fa/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<String>> confirmTwoFactor(@AuthenticationPrincipal String userId,
            @Valid @RequestBody ConfirmTwoFactorRequest request) {
        return ResponseEntity.ok(userService.confirmTwoFactor(userId, request));
    }

    @PostMapping("/me/2fa/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<String>> disableTwoFactor(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userService.disableTwoFactor(userId));
    }
}
