package com.neo.movie_service.repositories;

import java.time.Instant;

public interface SharedMovieRow {
    String getMovieId();

    String getSharedBy();

    Instant getSharedAt();
}
