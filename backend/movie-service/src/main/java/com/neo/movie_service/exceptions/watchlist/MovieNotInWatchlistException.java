package com.neo.movie_service.exceptions.watchlist;

public class MovieNotInWatchlistException extends RuntimeException {
    public MovieNotInWatchlistException(String message) {
        super(message);
    }
}
