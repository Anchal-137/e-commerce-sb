package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "coupon_usages")
@CompoundIndex(name = "user_coupon_idx", def = "{'userId': 1, 'couponId': 1}")
public class CouponUsage {

    @Id
    private String id;
    
    private String userId;
    
    private String couponId;
    
    private String orderId;
    
    private Double discountApplied;
    
    private Instant usedAt;
}
