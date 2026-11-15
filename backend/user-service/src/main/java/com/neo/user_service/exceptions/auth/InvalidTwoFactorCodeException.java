package com.neo.user_service.exceptions.auth;

public class InvalidTwoFactorCodeException extends RuntimeException {
    public InvalidTwoFactorCodeException(String message) {
        super(message);
    }
}
