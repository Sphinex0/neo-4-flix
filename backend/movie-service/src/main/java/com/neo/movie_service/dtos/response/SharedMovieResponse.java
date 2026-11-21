package com.neo.movie_service.dtos.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedMovieResponse {
    private String movieId;
    private String sharedBy;
    private Instant sharedAt;
}
