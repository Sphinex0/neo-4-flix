package com.neo.movie_service.services;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neo.movie_service.dtos.response.GenreResponse;
import com.neo.movie_service.models.Genre;
import com.neo.movie_service.repositories.GenreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAllByOrderByNameAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public Genre getOrCreateGenre(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
        }

        String normalizedName = normalizeGenreName(rawName);

        return genreRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> {
                    log.info("Creating new Genre node in Neo4j: {}", normalizedName);
                    Genre newGenre = Genre.builder()
                            .id(UUID.randomUUID().toString())
                            .name(normalizedName)
                            .build();
                    return genreRepository.save(newGenre);
                });
    }

    private String normalizeGenreName(String rawName) {
        String trimmed = rawName.trim();
        if (trimmed.length() <= 1) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1).toLowerCase(Locale.ROOT);
    }

    public GenreResponse mapToResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
