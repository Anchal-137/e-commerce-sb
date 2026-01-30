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
@Document(collection = "cart_items")
public class CartItem {

    @Id
    private String id;
    
    private String userId;
    
    private String productId;
    
    private String productName;
    
    private Double productPrice;
    
    private String productImageUrl;
    
    private Integer quantity;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}