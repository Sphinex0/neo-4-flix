package com.neo.movie_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.neo.movie_service.services.DataSeederService;
import com.neo.movie_service.services.MovieService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final DataSeederService dataSeederService;

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

    @PostMapping("/seed")
    public ResponseEntity<ResponseData<List<MovieResponse>>> seedMovies(
            @RequestParam(defaultValue = "20") int count
    ) {
        int cappedCount = Math.min(Math.max(count, 1), 100);
        List<MovieResponse> seeded = dataSeederService.seedMovies(cappedCount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.success("Successfully generated " + seeded.size() + " movies", seeded));
    }
}
