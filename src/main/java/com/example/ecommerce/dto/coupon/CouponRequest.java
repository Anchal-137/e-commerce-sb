package com.example.ecommerce.dto.coupon;

import com.example.ecommerce.model.Coupon.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 20, message = "Code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must be uppercase alphanumeric")
    private String code;
    
    @Size(max = 200, message = "Description must be less than 200 characters")
    private String description;
    
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;
    
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private Double discountValue;
    
    @PositiveOrZero(message = "Minimum order amount must be zero or positive")
    private Double minimumOrderAmount;
    
    @Positive(message = "Maximum discount must be positive")
    private Double maximumDiscount;
    
    private Integer usageLimit; // -1 for unlimited
    
    private Integer usageLimitPerUser;
    
    private Instant validFrom;
    
    private Instant validUntil;
}
