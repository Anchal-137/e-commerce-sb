package com.example.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {
    
    private String productId;
    private String productName;
    private String category;
    private Long totalSold;
    private Double totalRevenue;
    private Double averageRating;
}
