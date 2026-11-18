package com.neo.movie_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neo.movie_service.common.ResponseData;
import com.neo.movie_service.dtos.request.MovieCreateRequest;
import com.neo.movie_service.dtos.request.MovieUpdateRequest;
import com.neo.movie_service.dtos.response.MovieResponse;
import com.neo.movie_service.dtos.response.PageResponse;
import com.neo.movie_service.services.MovieService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<ResponseData<MovieResponse>> createMovie(@Valid @RequestBody MovieCreateRequest request) {
        MovieResponse created = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.success("Movie created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<MovieResponse>> getMovieById(@PathVariable String id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ResponseData.success("Movie retrieved successfully", movie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<MovieResponse>> updateMovie(
            @PathVariable String id,
            @Valid @RequestBody MovieUpdateRequest request
    ) {
        MovieResponse updated = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ResponseData.success("Movie updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ResponseData.success("Movie deleted successfully", null));
    }

    @PostMapping("/{id}/watchlist")
    public ResponseEntity<ResponseData<Void>> addToWatchlist(
            @PathVariable String id,
            Authentication authentication
    ) {
        movieService.addToWatchlist(authentication.getName(), id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.success("Movie added to watchlist successfully", null));
    }

    @DeleteMapping("/{id}/watchlist")
    public ResponseEntity<ResponseData<Void>> removeFromWatchlist(
            @PathVariable String id,
            Authentication authentication
    ) {
        movieService.removeFromWatchlist(authentication.getName(), id);
        return ResponseEntity.ok(ResponseData.success("Movie removed from watchlist successfully", null));
    }

    @GetMapping("/watchlist")
    public ResponseEntity<ResponseData<PageResponse<MovieResponse>>> getWatchlist(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<MovieResponse> result = movieService.getWatchlist(authentication.getName(), page, size);
        return ResponseEntity.ok(ResponseData.success("Watchlist retrieved successfully", result));
    }

    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<MovieResponse>>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PageResponse<MovieResponse> result = movieService.searchMovies(
                title, year, genre, page, size, sortBy, direction
        );
        return ResponseEntity.ok(ResponseData.success("Movies retrieved successfully", result));
    }
}
