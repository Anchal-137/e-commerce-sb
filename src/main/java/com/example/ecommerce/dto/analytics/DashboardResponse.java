package com.example.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    
    private Double totalRevenue;
    private Long totalOrders;
    private Long totalProducts;
    private Long totalUsers;
    private Double revenueGrowth;
    private Double orderGrowth;
    private List<RecentOrder> recentOrders;
    private List<TopProductResponse> topProducts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private String orderId;
        private String customerName;
        private Double amount;
        private String status;
        private String createdAt;
    }
}
