package com.example.ecommerce.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsResponse {
    
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersThisMonth;
    private Long newUsersThisWeek;
    private Map<String, Long> usersByRole;
}
