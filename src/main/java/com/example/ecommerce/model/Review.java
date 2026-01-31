package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
@CompoundIndex(name = "user_product_idx", def = "{'userId': 1, 'productId': 1}", unique = true)
public class Review {

    @Id
    private String id;
    
    private String productId;
    
    private String userId;
    
    private String username;
    
    private Integer rating; // 1-5 stars
    
    private String title;
    
    private String comment;
    
    @Builder.Default
    private Integer helpfulCount = 0;
    
    @Builder.Default
    private boolean verified = false; // Verified purchase
    
    private Instant createdAt;
    
    private Instant updatedAt;
}
