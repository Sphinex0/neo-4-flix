package com.neo.user_service.models;

import java.time.Instant;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Node("User")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@ToString
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    @ToString.Exclude
    private String password;

    @Builder.Default
    private UserRole role = UserRole.USER;

    private boolean twoFactorEnabled;

    @ToString.Exclude
    private String twoFactorSecret;

    private Instant createdAt;

    private Instant updatedAt;
}
