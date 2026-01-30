package com.example.ecommerce.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private String id;
    private String orderId;
    private Double amount;
    private String currency;
    private String status;
    private String paymentIntentId;
    private String clientSecret;
    private Instant createdAt;
}
