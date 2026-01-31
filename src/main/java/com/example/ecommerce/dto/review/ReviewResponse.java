package com.example.ecommerce.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private String id;
    private String productId;
    private String userId;
    private String username;
    private Integer rating;
    private String title;
    private String comment;
    private Integer helpfulCount;
    private boolean verified;
    private Instant createdAt;
    private Instant updatedAt;
}
