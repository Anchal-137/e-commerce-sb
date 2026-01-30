package com.example.ecommerce.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    
    private String id;
    private String userId;
    private String productId;
    private String productName;
    private Double productPrice;
    private String productImageUrl;
    private Integer quantity;
    private Double subtotal;
}
