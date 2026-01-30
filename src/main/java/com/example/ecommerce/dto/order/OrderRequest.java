package com.example.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @NotNull(message = "Shipping address is required")
    private ShippingAddressDto shippingAddress;
    
    private String paymentMethod;
    private String notes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        
        @NotBlank(message = "Street is required")
        private String street;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Zip code is required")
        private String zipCode;
        
        @NotBlank(message = "Country is required")
        private String country;
        
        @NotBlank(message = "Phone number is required")
        private String phoneNumber;
    }
}
