package com.example.ecommerce.repository;

import com.example.ecommerce.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {

    Optional<Coupon> findByCode(String code);
    
    boolean existsByCode(String code);
    
    Page<Coupon> findByActiveTrue(Pageable pageable);
    
    @Query("{'active': true, 'validFrom': {$lte: ?0}, 'validUntil': {$gte: ?0}}")
    List<Coupon> findValidCoupons(Instant now);
    
    Page<Coupon> findAll(Pageable pageable);
}
