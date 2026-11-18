package com.neo.movie_service.exceptions.watchlist;

public class MovieAlreadyInWatchlistException extends RuntimeException {
    public MovieAlreadyInWatchlistException(String message) {
        super(message);
    }
}
