package com.neo.user_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.LoginRequest;
import com.neo.user_service.dtos.request.RegisterRequest;
import com.neo.user_service.dtos.request.TwoFactorAuthRequest;
import com.neo.user_service.dtos.response.AuthResponse;
import com.neo.user_service.dtos.response.UserResponse;
import com.neo.user_service.services.AuthentificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthentificationController {
    private final AuthentificationService authentificationService;

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResponseData<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authentificationService.register(request));
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResponseData<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authentificationService.login(request));
    }

    @PostMapping("/2fa/verify")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResponseData<AuthResponse>> verifyTwoFactor(@Valid @RequestBody TwoFactorAuthRequest request) {
        return ResponseEntity.ok(authentificationService.verifyTwoFactor(request));
    }
}
