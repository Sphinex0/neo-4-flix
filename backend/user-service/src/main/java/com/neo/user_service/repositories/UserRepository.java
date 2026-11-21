package com.neo.user_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.neo.user_service.models.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("MATCH (:User {id: $userId})-[:FRIEND_OF]->(friend:User) RETURN friend")
    List<User> findFriends(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId}), (f:User {id: $friendId}) MERGE (u)-[:FRIEND_OF]->(f)")
    void addFriend(@Param("userId") String userId, @Param("friendId") String friendId);

    @Query("MATCH (:User {id: $userId})-[r:FRIEND_OF]->(:User {id: $friendId}) DELETE r")
    void removeFriend(@Param("userId") String userId, @Param("friendId") String friendId);

    @Query("RETURN EXISTS((:User {id: $userId})-[:FRIEND_OF]->(:User {id: $friendId}))")
    boolean isFriend(@Param("userId") String userId, @Param("friendId") String friendId);

    @Query("MATCH (u:User) WHERE toLower(u.firstName) CONTAINS toLower($query) " +
            "OR toLower(u.lastName) CONTAINS toLower($query) " +
            "OR toLower(u.username) CONTAINS toLower($query) " +
            "RETURN u LIMIT 10")
    List<User> searchUsers(@Param("query") String query);
}
