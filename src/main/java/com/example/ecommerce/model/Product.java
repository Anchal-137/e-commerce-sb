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
@Document(collection = "products")
public class Product {

    @Id
    private String id;
    
    private String name;
    
    private String description;
    
    private Double price;
    
    private Integer stock;
    
    private String category;
    
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
    
    private String sellerId;
    
    private String sellerName;
    
    @Builder.Default
    private Double rating = 0.0;
    
    @Builder.Default
    private Integer reviewCount = 0;
    
    @Builder.Default
    private boolean active = true;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}