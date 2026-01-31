package com.example.ecommerce.service;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.coupon.CouponRequest;
import com.example.ecommerce.dto.coupon.CouponResponse;
import com.example.ecommerce.dto.coupon.CouponValidationResponse;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Coupon;
import com.example.ecommerce.model.CouponUsage;
import com.example.ecommerce.repository.CouponRepository;
import com.example.ecommerce.repository.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    /**
     * Create a new coupon (Admin only)
     */
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponse createCoupon(CouponRequest request) {
        // Check if code already exists
        if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new BadRequestException("Coupon code already exists: " + request.getCode());
        }

        // Validate discount value for percentage type
        if (request.getDiscountType() == Coupon.DiscountType.PERCENTAGE && request.getDiscountValue() > 100) {
            throw new BadRequestException("Percentage discount cannot exceed 100%");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : 0.0)
                .maximumDiscount(request.getMaximumDiscount())
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : -1)
                .usageLimitPerUser(request.getUsageLimitPerUser() != null ? request.getUsageLimitPerUser() : 1)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .usedCount(0)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);
        
        log.info("Created new coupon: {}", savedCoupon.getCode());
        
        return mapToResponse(savedCoupon);
    }

    /**
     * Get coupon by code
     */
    @Cacheable(value = "coupons", key = "#code")
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));
        return mapToResponse(coupon);
    }

    /**
     * Get all coupons with pagination (Admin only)
     */
    public PageResponse<CouponResponse> getAllCoupons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Coupon> couponPage = couponRepository.findAll(pageable);
        return buildPageResponse(couponPage);
    }

    /**
     * Get active coupons (for users)
     */
    public PageResponse<CouponResponse> getActiveCoupons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Coupon> couponPage = couponRepository.findByActiveTrue(pageable);
        return buildPageResponse(couponPage);
    }

    /**
     * Validate and calculate discount for a coupon
     */
    public CouponValidationResponse validateCoupon(String code, String userId, Double orderAmount) {
        try {
            Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));

            // Check if coupon is valid
            if (!coupon.isValid()) {
                return buildInvalidResponse(code, orderAmount, "Coupon has expired or is no longer active");
            }

            // Check minimum order amount
            if (coupon.getMinimumOrderAmount() != null && orderAmount < coupon.getMinimumOrderAmount()) {
                return buildInvalidResponse(code, orderAmount, 
                        String.format("Minimum order amount of $%.2f required", coupon.getMinimumOrderAmount()));
            }

            // Check user usage limit
            long userUsageCount = couponUsageRepository.countByUserIdAndCouponId(userId, coupon.getId());
            if (userUsageCount >= coupon.getUsageLimitPerUser()) {
                return buildInvalidResponse(code, orderAmount, "You have already used this coupon the maximum number of times");
            }

            // Calculate discount
            double discountAmount = calculateDiscount(coupon, orderAmount);
            double finalAmount = orderAmount - discountAmount;

            return CouponValidationResponse.builder()
                    .valid(true)
                    .code(code.toUpperCase())
                    .message("Coupon applied successfully!")
                    .originalAmount(orderAmount)
                    .discountAmount(discountAmount)
                    .finalAmount(Math.max(0, finalAmount))
                    .build();

        } catch (ResourceNotFoundException e) {
            return buildInvalidResponse(code, orderAmount, "Invalid coupon code");
        }
    }

    /**
     * Apply coupon to an order (records usage)
     */
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public Double applyCoupon(String code, String userId, String orderId, Double orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));

        // Validate coupon
        CouponValidationResponse validation = validateCoupon(code, userId, orderAmount);
        if (!validation.isValid()) {
            throw new BadRequestException(validation.getMessage());
        }

        double discountAmount = calculateDiscount(coupon, orderAmount);

        // Record usage
        CouponUsage usage = CouponUsage.builder()
                .userId(userId)
                .couponId(coupon.getId())
                .orderId(orderId)
                .discountApplied(discountAmount)
                .usedAt(Instant.now())
                .build();
        couponUsageRepository.save(usage);

        // Update coupon used count
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        coupon.setUpdatedAt(Instant.now());
        couponRepository.save(coupon);

        log.info("Coupon {} applied to order {} by user {}. Discount: ${}", 
                code, orderId, userId, discountAmount);

        return discountAmount;
    }

    /**
     * Deactivate a coupon (Admin only)
     */
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponse deactivateCoupon(String couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", couponId));

        coupon.setActive(false);
        coupon.setUpdatedAt(Instant.now());
        
        Coupon updatedCoupon = couponRepository.save(coupon);
        
        log.info("Deactivated coupon: {}", coupon.getCode());
        
        return mapToResponse(updatedCoupon);
    }

    /**
     * Update a coupon (Admin only)
     */
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponse updateCoupon(String couponId, CouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", couponId));

        // Check if new code conflicts with existing (if code is being changed)
        if (!coupon.getCode().equals(request.getCode().toUpperCase())) {
            if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
                throw new BadRequestException("Coupon code already exists: " + request.getCode());
            }
            coupon.setCode(request.getCode().toUpperCase());
        }

        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumOrderAmount(request.getMinimumOrderAmount());
        coupon.setMaximumDiscount(request.getMaximumDiscount());
        if (request.getUsageLimit() != null) {
            coupon.setUsageLimit(request.getUsageLimit());
        }
        if (request.getUsageLimitPerUser() != null) {
            coupon.setUsageLimitPerUser(request.getUsageLimitPerUser());
        }
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setUpdatedAt(Instant.now());

        Coupon updatedCoupon = couponRepository.save(coupon);
        
        log.info("Updated coupon: {}", coupon.getCode());
        
        return mapToResponse(updatedCoupon);
    }

    /**
     * Delete a coupon (Admin only)
     */
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public void deleteCoupon(String couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", couponId));

        couponRepository.delete(coupon);
        
        log.info("Deleted coupon: {}", coupon.getCode());
    }

    /**
     * Calculate discount amount based on coupon type
     */
    private double calculateDiscount(Coupon coupon, Double orderAmount) {
        double discount;

        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderAmount * (coupon.getDiscountValue() / 100.0);
            
            // Apply maximum discount cap if set
            if (coupon.getMaximumDiscount() != null && discount > coupon.getMaximumDiscount()) {
                discount = coupon.getMaximumDiscount();
            }
        } else {
            // FIXED_AMOUNT
            discount = coupon.getDiscountValue();
        }

        // Discount cannot exceed order amount
        return Math.min(discount, orderAmount);
    }

    private CouponValidationResponse buildInvalidResponse(String code, Double orderAmount, String message) {
        return CouponValidationResponse.builder()
                .valid(false)
                .code(code.toUpperCase())
                .message(message)
                .originalAmount(orderAmount)
                .discountAmount(0.0)
                .finalAmount(orderAmount)
                .build();
    }

    private PageResponse<CouponResponse> buildPageResponse(Page<Coupon> couponPage) {
        List<CouponResponse> content = couponPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CouponResponse>builder()
                .content(content)
                .pageNumber(couponPage.getNumber())
                .pageSize(couponPage.getSize())
                .totalElements(couponPage.getTotalElements())
                .totalPages(couponPage.getTotalPages())
                .first(couponPage.isFirst())
                .last(couponPage.isLast())
                .build();
    }

    private CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumOrderAmount(coupon.getMinimumOrderAmount())
                .maximumDiscount(coupon.getMaximumDiscount())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .usageLimitPerUser(coupon.getUsageLimitPerUser())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .active(coupon.isActive())
                .valid(coupon.isValid())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
