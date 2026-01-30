package com.example.ecommerce.dto.order;

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
public class OrderResponse {
    
    private String id;
    private String userId;
    private String username;
    private Double totalAmount;
    private String status;
    private List<OrderItemDto> items;
    private ShippingAddressDto shippingAddress;
    private String trackingNumber;
    private String paymentStatus;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private Integer quantity;
        private Double price;
        private Double subtotal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String phoneNumber;
    }
}
