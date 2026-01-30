package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    private String productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}