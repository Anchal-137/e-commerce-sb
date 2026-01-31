package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "coupons")
public class Coupon {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String code; // e.g., "SAVE20", "WELCOME10"
    
    private String description;
    
    private DiscountType discountType; // PERCENTAGE or FIXED_AMOUNT
    
    private Double discountValue; // 20 for 20% or 100 for $100 off
    
    private Double minimumOrderAmount; // Minimum order value to apply coupon
    
    private Double maximumDiscount; // Cap on discount for percentage type
    
    @Builder.Default
    private Integer usageLimit = -1; // -1 means unlimited
    
    @Builder.Default
    private Integer usedCount = 0;
    
    @Builder.Default
    private Integer usageLimitPerUser = 1; // How many times a single user can use
    
    private Instant validFrom;
    
    private Instant validUntil;
    
    @Builder.Default
    private boolean active = true;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
    
    public boolean isValid() {
        Instant now = Instant.now();
        return active 
                && (validFrom == null || now.isAfter(validFrom))
                && (validUntil == null || now.isBefore(validUntil))
                && (usageLimit == -1 || usedCount < usageLimit);
    }
}
