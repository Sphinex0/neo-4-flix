package com.neo.movie_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

import java.util.List;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreService genreService;

    @InjectMocks
    private MovieService movieService;

    @Test
    @DisplayName("Should create movie successfully when title does not exist")
    void shouldCreateMovieSuccessfully() {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .title("Inception")
                .description("A mind-bending thriller")
                .releaseYear(2010)
                .durationMinutes(148)
                .director("Christopher Nolan")
                .genres(Set.of("Sci-Fi"))
                .build();

        Genre scifi = Genre.builder().id("g-1").name("Sci-Fi").build();
        GenreResponse scifiResponse = GenreResponse.builder().id("g-1").name("Sci-Fi").build();

        when(movieRepository.existsByTitleIgnoreCase("Inception")).thenReturn(false);
        when(genreService.getOrCreateGenre("Sci-Fi")).thenReturn(scifi);
        when(genreService.mapToResponse(scifi)).thenReturn(scifiResponse);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieResponse response = movieService.createMovie(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Inception");
        assertThat(response.getReleaseYear()).isEqualTo(2010);
        assertThat(response.getGenres()).hasSize(1);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should throw MovieAlreadyExistsException when movie title is taken")
    void shouldThrowExceptionWhenTitleExists() {
        MovieCreateRequest request = MovieCreateRequest.builder()
                .title("Inception")
                .description("Thriller")
                .releaseYear(2010)
                .durationMinutes(148)
                .build();

        when(movieRepository.existsByTitleIgnoreCase("Inception")).thenReturn(true);

        assertThatThrownBy(() -> movieService.createMovie(request))
                .isInstanceOf(MovieAlreadyExistsException.class)
                .hasMessageContaining("Inception");
    }

    @Test
    @DisplayName("Should retrieve movie by ID")
    void shouldGetMovieById() {
        Movie movie = Movie.builder()
                .id("m-123")
                .title("Interstellar")
                .releaseYear(2014)
                .durationMinutes(169)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(movieRepository.findByIdWithGenres("m-123")).thenReturn(Optional.of(movie));

        MovieResponse response = movieService.getMovieById("m-123");

        assertThat(response.getId()).isEqualTo("m-123");
        assertThat(response.getTitle()).isEqualTo("Interstellar");
    }

    @Test
    @DisplayName("Should throw MovieNotFoundException when ID does not exist")
    void shouldThrowExceptionWhenNotFound() {
        when(movieRepository.findByIdWithGenres("m-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById("m-999"))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("m-999");
    }

    @Test
    @DisplayName("Should update movie fields")
    void shouldUpdateMovie() {
        Movie existing = Movie.builder()
                .id("m-123")
                .title("Old Title")
                .releaseYear(2000)
                .build();

        MovieUpdateRequest updateReq = MovieUpdateRequest.builder()
                .title("New Title")
                .releaseYear(2001)
                .build();

        when(movieRepository.findByIdWithGenres("m-123")).thenReturn(Optional.of(existing));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieResponse updated = movieService.updateMovie("m-123", updateReq);

        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getReleaseYear()).isEqualTo(2001);
    }

    @Test
    @DisplayName("Should search movies with pagination")
    void shouldSearchMovies() {
        Movie movie = Movie.builder().id("m-1").title("The Matrix").releaseYear(1999).build();
        when(movieRepository.searchMovies(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(movie)));

        PageResponse<MovieResponse> page = movieService.searchMovies(
                "Matrix", null, null, 0, 10, "title", "asc"
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("The Matrix");
    }
}
