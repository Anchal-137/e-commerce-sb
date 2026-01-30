package com.example.ecommerce.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private String userId;
    private List<CartItemResponse> items;
    private int totalItems;
    private Double totalAmount;
}
