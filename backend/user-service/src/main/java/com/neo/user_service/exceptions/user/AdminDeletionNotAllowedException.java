package com.neo.user_service.exceptions.user;

public class AdminDeletionNotAllowedException extends RuntimeException {
    public AdminDeletionNotAllowedException(String message) {
        super(message);
    }
}
