package com.example.ecommerce.repository;

import com.example.ecommerce.model.CouponUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends MongoRepository<CouponUsage, String> {

    List<CouponUsage> findByUserId(String userId);
    
    List<CouponUsage> findByCouponId(String couponId);
    
    long countByUserIdAndCouponId(String userId, String couponId);
    
    boolean existsByUserIdAndCouponId(String userId, String couponId);
}
