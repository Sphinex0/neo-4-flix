package com.neo.user_service.services;

import org.springframework.stereotype.Service;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.dtos.request.LoginRequest;
import com.neo.user_service.dtos.request.RegisterRequest;
import com.neo.user_service.dtos.request.TwoFactorAuthRequest;
import com.neo.user_service.dtos.response.AuthResponse;
import com.neo.user_service.dtos.response.UserResponse;

@Service
public class AuthentificationService {

    public ResponseData<UserResponse> register(RegisterRequest request) {
        return null;
    }

    public ResponseData<AuthResponse> login(LoginRequest request) {
        return null;
    }

    public ResponseData<AuthResponse> verifyTwoFactor(TwoFactorAuthRequest request) {
        return null;
    }
}
