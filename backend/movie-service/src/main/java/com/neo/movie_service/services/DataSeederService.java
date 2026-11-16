package com.neo.movie_service.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neo.movie_service.dtos.response.MovieResponse;
import com.neo.movie_service.models.Genre;
import com.neo.movie_service.models.Movie;
import com.neo.movie_service.repositories.MovieRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederService implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final GenreService genreService;
    private final MovieService movieService;

    private static final String[] GENRE_POOL = {
        "Action", "Adventure", "Animation", "Comedy", "Crime",
        "Documentary", "Drama", "Fantasy", "Horror", "Mystery",
        "Romance", "Sci-Fi", "Thriller", "Western"
    };

    private static final String[] SAMPLE_POSTER_IDS = {
        "photo-1489599849927-2ee91cede3ba",
        "photo-1517604931442-7e0c8ed2963c",
        "photo-1536440136628-849c177e76a1",
        "photo-1478760329108-5c3ed9d495a0",
        "photo-1485846234645-a62644f84728",
        "photo-1509198397868-475647b2a1e5",
        "photo-1518676590629-3dcbd9c5a5c9",
        "photo-1524985069026-dd778a71c7b4"
    };

    @Override
    public void run(String... args) {
        long currentCount = movieRepository.count();
        if (currentCount == 0) {
            log.info("Database is empty. Automatically seeding 25 dynamic movies into Neo4j...");
            seedMovies(25);
            log.info("Initial seeding complete! Total movies: {}", movieRepository.count());
        } else {
            log.info("Database already contains {} movies. Skipping initial seed.", currentCount);
        }
    }

    @Transactional
    public List<MovieResponse> seedMovies(int count) {
        Faker faker = new Faker();
        Random random = new Random();
        List<MovieResponse> seeded = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String title = generateUniqueTitle(faker);
            String description = faker.lorem().paragraph(3);
            int releaseYear = faker.number().numberBetween(1985, 2025);
            int duration = faker.number().numberBetween(85, 185);
            String director = faker.name().fullName();

            int genreCount = faker.number().numberBetween(1, 4);
            Set<Genre> movieGenres = new HashSet<>();
            for (int g = 0; g < genreCount; g++) {
                String genreName = GENRE_POOL[random.nextInt(GENRE_POOL.length)];
                movieGenres.add(genreService.getOrCreateGenre(genreName));
            }

            String posterPhoto = SAMPLE_POSTER_IDS[random.nextInt(SAMPLE_POSTER_IDS.length)];
            String posterUrl = "https://images.unsplash.com/" + posterPhoto + "?auto=format&fit=crop&w=500&q=80";
            String trailerUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

            Instant now = Instant.now();
            Movie movie = Movie.builder()
                    .id(UUID.randomUUID().toString())
                    .title(title)
                    .description(description)
                    .releaseYear(releaseYear)
                    .durationMinutes(duration)
                    .posterUrl(posterUrl)
                    .trailerUrl(trailerUrl)
                    .director(director)
                    .genres(movieGenres)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Movie saved = movieRepository.save(movie);
            seeded.add(movieService.mapToResponse(saved));
        }

        log.info("Successfully dynamically generated and saved {} movies to Neo4j graph.", seeded.size());
        return seeded;
    }

    private String generateUniqueTitle(Faker faker) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate;
            int type = faker.number().numberBetween(0, 3);
            if (type == 0) {
                candidate = faker.book().title();
            } else if (type == 1) {
                candidate = "The " + faker.color().name() + " " + faker.animal().name();
            } else {
                candidate = faker.aviation().airport() + " Chronicle";
            }

            if (!movieRepository.existsByTitleIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return "Movie " + UUID.randomUUID().toString().substring(0, 8);
    }
}
