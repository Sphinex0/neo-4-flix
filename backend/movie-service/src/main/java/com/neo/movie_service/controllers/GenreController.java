package com.neo.movie_service.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neo.movie_service.common.ResponseData;
import com.neo.movie_service.dtos.response.GenreResponse;
import com.neo.movie_service.services.GenreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/genres", "/api/genre"})
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<ResponseData<List<GenreResponse>>> getAllGenres() {
        List<GenreResponse> genres = genreService.getAllGenres();
        return ResponseEntity.ok(ResponseData.success("Genres retrieved successfully", genres));
    }
}
