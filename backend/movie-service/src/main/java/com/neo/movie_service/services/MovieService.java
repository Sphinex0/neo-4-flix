package com.neo.movie_service.services;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neo.movie_service.dtos.request.MovieCreateRequest;
import com.neo.movie_service.dtos.request.MovieUpdateRequest;
import com.neo.movie_service.dtos.response.GenreResponse;
import com.neo.movie_service.dtos.response.MovieResponse;
import com.neo.movie_service.dtos.response.PageResponse;
import com.neo.movie_service.exceptions.movie.MovieAlreadyExistsException;
import com.neo.movie_service.exceptions.movie.MovieNotFoundException;
import com.neo.movie_service.models.Genre;
import com.neo.movie_service.models.Movie;
import com.neo.movie_service.repositories.MovieRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreService genreService;

    @Transactional
    public MovieResponse createMovie(MovieCreateRequest request) {
        if (movieRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new MovieAlreadyExistsException("Movie with title '" + request.getTitle() + "' already exists!");
        }

        Set<Genre> resolvedGenres = resolveGenres(request.getGenres());

        Instant now = Instant.now();
        Movie movie = Movie.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .releaseYear(request.getReleaseYear())
                .durationMinutes(request.getDurationMinutes())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .director(request.getDirector())
                .genres(resolvedGenres)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Movie saved = movieRepository.save(movie);
        log.info("Created new Movie node [id={}, title={}] in Neo4j", saved.getId(), saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovieById(String id) {
        Movie movie = movieRepository.findByIdWithGenres(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie with id '" + id + "' not found!"));
        return mapToResponse(movie);
    }

    @Transactional
    public MovieResponse updateMovie(String id, MovieUpdateRequest request) {
        Movie movie = movieRepository.findByIdWithGenres(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie with id '" + id + "' not found!"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            movie.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            movie.setDescription(request.getDescription().trim());
        }
        if (request.getReleaseYear() != null) {
            movie.setReleaseYear(request.getReleaseYear());
        }
        if (request.getDurationMinutes() != null) {
            movie.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getPosterUrl() != null) {
            movie.setPosterUrl(request.getPosterUrl());
        }
        if (request.getTrailerUrl() != null) {
            movie.setTrailerUrl(request.getTrailerUrl());
        }
        if (request.getDirector() != null) {
            movie.setDirector(request.getDirector());
        }
        if (request.getGenres() != null) {
            Set<Genre> resolvedGenres = resolveGenres(request.getGenres());
            movie.setGenres(resolvedGenres);
        }

        movie.setUpdatedAt(Instant.now());
        Movie updated = movieRepository.save(movie);
        log.info("Updated Movie node [id={}, title={}]", updated.getId(), updated.getTitle());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteMovie(String id) {
        if (!movieRepository.existsById(id)) {
            throw new MovieNotFoundException("Movie with id '" + id + "' not found!");
        }
        movieRepository.deleteById(id);
        log.info("Deleted Movie node [id={}] and its relationships from Neo4j", id);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovieResponse> searchMovies(
            String title,
            Integer year,
            String genre,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        String cleanSort = (sortBy == null || sortBy.isBlank()) ? "title" : sortBy.trim();
        if ("releaseYear".equals(cleanSort) || "year".equals(cleanSort)) {
            cleanSort = "year";
        }
        String sortTarget = cleanSort.startsWith("m.") ? cleanSort : "m." + cleanSort;

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortTarget
        );
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        String normalizedTitle = (title != null && !title.isBlank()) ? title.trim() : null;
        String normalizedGenre = (genre != null && !genre.isBlank()) ? genre.trim() : null;

        Page<Movie> moviePage = movieRepository.searchMovies(normalizedTitle, year, normalizedGenre, pageable);

        return PageResponse.<MovieResponse>builder()
                .content(moviePage.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(moviePage.getNumber())
                .pageSize(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .last(moviePage.isLast())
                .build();
    }

    private Set<Genre> resolveGenres(Set<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            return new HashSet<>();
        }
        return genreNames.stream()
                .map(genreService::getOrCreateGenre)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public MovieResponse mapToResponse(Movie movie) {
        Set<GenreResponse> genreResponses = (movie.getGenres() == null) ? Collections.emptySet()
                : movie.getGenres().stream()
                        .map(genreService::mapToResponse)
                        .collect(Collectors.toSet());

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .releaseYear(movie.getReleaseYear())
                .durationMinutes(movie.getDurationMinutes())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .director(movie.getDirector())
                .imdbRating(movie.getImdbRating())
                .genres(genreResponses)
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
