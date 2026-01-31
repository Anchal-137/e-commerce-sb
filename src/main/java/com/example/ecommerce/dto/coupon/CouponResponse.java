package com.example.ecommerce.dto.coupon;

import com.example.ecommerce.model.Coupon.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {

    private String id;
    private String code;
    private String description;
    private DiscountType discountType;
    private Double discountValue;
    private Double minimumOrderAmount;
    private Double maximumDiscount;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private Instant validFrom;
    private Instant validUntil;
    private boolean active;
    private boolean valid; // Computed: is currently valid to use
    private Instant createdAt;
}
