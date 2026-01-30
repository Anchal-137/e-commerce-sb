package com.example.ecommerce.controller;

import com.example.ecommerce.dto.analytics.*;
import com.example.ecommerce.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Analytics", description = "Analytics and reporting APIs (Admin only)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard", description = "Get dashboard overview with key metrics")
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse dashboard = analyticsService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/sales")
    @Operation(summary = "Get sales analytics", description = "Get sales analytics for a date range")
    public ResponseEntity<SalesAnalyticsResponse> getSalesAnalytics(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SalesAnalyticsResponse analytics = analyticsService.getSalesAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/orders")
    @Operation(summary = "Get order analytics", description = "Get order analytics for a date range")
    public ResponseEntity<OrderAnalyticsResponse> getOrderAnalytics(
            @Parameter(description = "Start date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        OrderAnalyticsResponse analytics = analyticsService.getOrderAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/products/top")
    @Operation(summary = "Get top products", description = "Get top selling products")
    public ResponseEntity<List<TopProductResponse>> getTopProducts(
            @Parameter(description = "Number of products to return") 
            @RequestParam(defaultValue = "10") int limit) {
        List<TopProductResponse> topProducts = analyticsService.getTopProducts(limit);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/users")
    @Operation(summary = "Get user analytics", description = "Get user statistics")
    public ResponseEntity<UserAnalyticsResponse> getUserAnalytics() {
        UserAnalyticsResponse analytics = analyticsService.getUserAnalytics();
        return ResponseEntity.ok(analytics);
    }
}
