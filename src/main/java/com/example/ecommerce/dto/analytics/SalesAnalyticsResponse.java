package com.example.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalyticsResponse {
    
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private List<DailySales> dailySales;
    private Map<String, Double> revenueByCategory;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private String date;
        private Double revenue;
        private Long orderCount;
    }
}
