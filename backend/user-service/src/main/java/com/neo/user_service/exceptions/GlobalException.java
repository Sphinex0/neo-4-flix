package com.neo.user_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.neo.user_service.common.ResponseData;
import com.neo.user_service.exceptions.auth.IncorrectPasswordException;
import com.neo.user_service.exceptions.auth.InvalidCredentialsException;
import com.neo.user_service.exceptions.auth.InvalidTwoFactorCodeException;
import com.neo.user_service.exceptions.user.EmailAlreadyExistsException;
import com.neo.user_service.exceptions.user.EmailNotFoundException;
import com.neo.user_service.exceptions.user.UserNotFoundException;
import com.neo.user_service.exceptions.user.UsernameAlreadyExistsException;
import com.neo.user_service.exceptions.user.UsernameNotFoundException;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleEmailNotFound(EmailNotFoundException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleUserNotFound(UserNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ResponseData<Void>> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseData<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ResponseData<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ResponseData<Void>> handleIncorrectPassword(IncorrectPasswordException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidTwoFactorCodeException.class)
    public ResponseEntity<ResponseData<Void>> handleInvalidTwoFactorCode(InvalidTwoFactorCodeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed !";
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseData<Void>> handleMalformedRequest(HttpMessageNotReadableException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Malformed request body !");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseData<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed !");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Endpoint not found !");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Void>> handleGeneralException(Exception ex) {
        ex.printStackTrace(); // prints real error in console
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong !");
    }

    private ResponseEntity<ResponseData<Void>> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ResponseData.error(message));
    }
}
