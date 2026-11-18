package com.neo.rating_service.controllers;

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

import com.neo.rating_service.common.ResponseData;
import com.neo.rating_service.dtos.request.ChangeRatingRequest;
import com.neo.rating_service.dtos.request.RatingRequest;
import com.neo.rating_service.dtos.response.MovieRatingResponse;
import com.neo.rating_service.dtos.response.MyRatingResponse;
import com.neo.rating_service.dtos.response.RatingResponse;
import com.neo.rating_service.services.RatingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<RatingResponse>> rateMovie(@AuthenticationPrincipal String userId,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.rateMovie(userId, request));
    }

    @PutMapping("/{movieId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<RatingResponse>> changeRating(@AuthenticationPrincipal String userId,
            @PathVariable String movieId, @Valid @RequestBody ChangeRatingRequest request) {
        return ResponseEntity.ok(ratingService.changeRating(userId, movieId, request));
    }

    @DeleteMapping("/{movieId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<String>> deleteRating(@AuthenticationPrincipal String userId,
            @PathVariable String movieId) {
        return ResponseEntity.ok(ratingService.deleteRating(userId, movieId));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseData<List<MyRatingResponse>>> getMyRatings(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ratingService.getMyRatings(userId));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ResponseData<List<MovieRatingResponse>>> getMovieRatings(@PathVariable String movieId) {
        return ResponseEntity.ok(ratingService.getMovieRatings(movieId));
    }
}
