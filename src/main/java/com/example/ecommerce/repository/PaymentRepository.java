package com.example.ecommerce.repository;

import com.example.ecommerce.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    
    Optional<Payment> findByOrderId(String orderId);
    
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    
    List<Payment> findByUserId(String userId);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findByCreatedAtBetween(Instant start, Instant end);
    
    long countByStatus(Payment.PaymentStatus status);
}