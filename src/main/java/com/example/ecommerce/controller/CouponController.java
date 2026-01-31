package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.coupon.ApplyCouponRequest;
import com.example.ecommerce.dto.coupon.CouponRequest;
import com.example.ecommerce.dto.coupon.CouponResponse;
import com.example.ecommerce.dto.coupon.CouponValidationResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Coupon and discount management APIs")
public class CouponController {

    private final CouponService couponService;

    // ==================== Admin Endpoints ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create coupon (Admin)", description = "Create a new discount coupon")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all coupons (Admin)", description = "Get all coupons including inactive ones")
    public ResponseEntity<PageResponse<CouponResponse>> getAllCoupons(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        PageResponse<CouponResponse> response = couponService.getAllCoupons(page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update coupon (Admin)", description = "Update an existing coupon")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable String couponId,
            @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{couponId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Deactivate coupon (Admin)", description = "Deactivate a coupon without deleting it")
    public ResponseEntity<CouponResponse> deactivateCoupon(@PathVariable String couponId) {
        CouponResponse response = couponService.deactivateCoupon(couponId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete coupon (Admin)", description = "Permanently delete a coupon")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }

    // ==================== User Endpoints ====================

    @GetMapping("/active")
    @Operation(summary = "Get active coupons", description = "Get all currently active coupons available to users")
    public ResponseEntity<PageResponse<CouponResponse>> getActiveCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CouponResponse> response = couponService.getActiveCoupons(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code", description = "Get coupon details by its code")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        CouponResponse response = couponService.getCouponByCode(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Validate coupon", description = "Validate a coupon and calculate the discount amount")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            @RequestParam Double orderAmount,
            @AuthenticationPrincipal User user) {
        CouponValidationResponse response = couponService.validateCoupon(
                request.getCode(), user.getId(), orderAmount);
        return ResponseEntity.ok(response);
    }
}
