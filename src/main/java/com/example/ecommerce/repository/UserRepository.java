package com.example.ecommerce.repository;

import com.example.ecommerce.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    List<User> findByCreatedAtAfter(Instant date);
    
    long countByEnabled(boolean enabled);
}