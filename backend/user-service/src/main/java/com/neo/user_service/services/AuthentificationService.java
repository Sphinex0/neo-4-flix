package com.neo.user_service.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.LoginRequest;
import com.neo.user_service.dtos.request.RegisterRequest;
import com.neo.user_service.dtos.request.TwoFactorAuthRequest;
import com.neo.user_service.dtos.response.AuthResponse;
import com.neo.user_service.dtos.response.UserResponse;
import com.neo.user_service.exceptions.auth.InvalidCredentialsException;
import com.neo.user_service.exceptions.auth.InvalidTwoFactorCodeException;
import com.neo.user_service.exceptions.user.EmailAlreadyExistsException;
import com.neo.user_service.exceptions.user.UsernameAlreadyExistsException;
import com.neo.user_service.exceptions.user.UsernameNotFoundException;
import com.neo.user_service.models.User;
import com.neo.user_service.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthentificationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorAuthService twoFactorAuthService;

    public ResponseData<UserResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        Instant now = Instant.now();
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        UserResponse response = UserResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .twoFactorEnabled(savedUser.isTwoFactorEnabled())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();

        return ResponseData.success("Account created successfully", response);
    }

    public ResponseData<AuthResponse> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (user.isTwoFactorEnabled()) {
            AuthResponse response = AuthResponse.builder()
                    .twoFactorRequired(true)
                    .build();

            return ResponseData.success("Two-factor authentication code required", response);
        }

        String token = jwtService.generateToken(user.getId(), user.getRole().name());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .twoFactorRequired(false)
                .build();

        return ResponseData.success("Login successful", response);
    }

    public ResponseData<AuthResponse> verifyTwoFactor(TwoFactorAuthRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new InvalidTwoFactorCodeException("Two-factor authentication is not enabled for this account");
        }

        if (!twoFactorAuthService.isCodeValid(user.getTwoFactorSecret(), request.code())) {
            throw new InvalidTwoFactorCodeException("Invalid or expired code");
        }

        String token = jwtService.generateToken(user.getId(), user.getRole().name());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .twoFactorRequired(false)
                .build();

        return ResponseData.success("Login successful", response);
    }
}
