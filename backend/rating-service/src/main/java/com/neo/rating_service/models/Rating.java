package com.neo.rating_service.models;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Rating {

    private String userId;

    private String movieId;

    private Integer score;

    private Instant createdAt;

    private Instant updatedAt;
}
