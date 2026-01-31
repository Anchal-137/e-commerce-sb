package com.example.ecommerce.repository;

import com.example.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findByProductId(String productId, Pageable pageable);
    
    Page<Review> findByUserId(String userId, Pageable pageable);
    
    Optional<Review> findByUserIdAndProductId(String userId, String productId);
    
    boolean existsByUserIdAndProductId(String userId, String productId);
    
    List<Review> findByProductId(String productId);
    
    @Query("{'productId': ?0}")
    List<Review> findAllByProductIdForStats(String productId);
    
    long countByProductId(String productId);
    
    void deleteByProductId(String productId);
}
