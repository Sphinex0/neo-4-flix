package com.neo.rating_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.neo.rating_service.common.ResponseData;
import com.neo.rating_service.exceptions.movie.MovieNotFoundException;
import com.neo.rating_service.exceptions.rating.RatingAlreadyExistsException;
import com.neo.rating_service.exceptions.rating.RatingNotFoundException;

@RestControllerAdvice
public class GlobalExceptions {
    @ExceptionHandler(RatingNotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleRatingNotFound(RatingNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RatingAlreadyExistsException.class)
    public ResponseEntity<ResponseData<Void>> handleRatingAlreadyExists(RatingAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleMovieNotFound(MovieNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
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
