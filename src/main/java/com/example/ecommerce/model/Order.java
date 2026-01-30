package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    
    private String userId;
    
    private String username;
    
    private Double totalAmount;
    
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    
    private ShippingAddress shippingAddress;
    
    private String trackingNumber;
    
    private String paymentId;
    
    private String paymentStatus;
    
    private String notes;
    
    private Instant createdAt;
    
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String phoneNumber;
    }

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
}