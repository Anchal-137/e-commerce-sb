package com.example.ecommerce.repository;

import com.example.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    
    Page<Order> findByUserId(String userId, Pageable pageable);
    
    List<Order> findByUserId(String userId);
    
    List<Order> findByUserIdAndStatus(String userId, Order.OrderStatus status);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    List<Order> findByCreatedAtBetween(Instant start, Instant end);
    
    long countByStatus(Order.OrderStatus status);
    
    long countByCreatedAtBetween(Instant start, Instant end);
    
    List<Order> findByStatusAndCreatedAtBetween(Order.OrderStatus status, Instant start, Instant end);
}