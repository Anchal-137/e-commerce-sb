package com.example.ecommerce.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private String id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    private List<String> imageUrls;
    private String sellerId;
    private String sellerName;
    private Double rating;
    private Integer reviewCount;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
