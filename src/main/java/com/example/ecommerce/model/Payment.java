package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;
    
    private String orderId;
    
    private String userId;
    
    private Double amount;
    
    private String currency;
    
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private String paymentIntentId;
    
    private String clientSecret;
    
    private String paymentMethod;
    
    private String failureReason;
    
    private Instant createdAt;
    
    private Instant updatedAt;

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        REFUNDED,
        CANCELLED
    }
}