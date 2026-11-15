package com.neo.user_service.repositories;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.neo.user_service.models.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {
    
}
