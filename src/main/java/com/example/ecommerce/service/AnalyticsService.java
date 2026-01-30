package com.example.ecommerce.service;

import com.example.ecommerce.dto.analytics.*;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public SalesAnalyticsResponse getSalesAnalytics(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        
        // Filter only successful orders
        List<Order> successfulOrders = orders.stream()
                .filter(o -> o.getStatus() != Order.OrderStatus.CANCELLED && 
                            o.getStatus() != Order.OrderStatus.REFUNDED)
                .collect(Collectors.toList());

        double totalRevenue = successfulOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        long totalOrders = successfulOrders.size();
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // Calculate daily sales
        Map<LocalDate, List<Order>> ordersByDate = successfulOrders.stream()
                .collect(Collectors.groupingBy(o -> 
                    LocalDateTime.ofInstant(o.getCreatedAt(), ZoneId.systemDefault()).toLocalDate()
                ));

        List<SalesAnalyticsResponse.DailySales> dailySales = new ArrayList<>();
        LocalDate current = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (!current.isAfter(endDate)) {
            List<Order> dayOrders = ordersByDate.getOrDefault(current, Collections.emptyList());
            double dayRevenue = dayOrders.stream().mapToDouble(Order::getTotalAmount).sum();
            
            dailySales.add(SalesAnalyticsResponse.DailySales.builder()
                    .date(current.format(formatter))
                    .revenue(dayRevenue)
                    .orderCount((long) dayOrders.size())
                    .build());
            
            current = current.plusDays(1);
        }

        // Revenue by category (simplified - using first item's product name as category indicator)
        Map<String, Double> revenueByCategory = new HashMap<>();
        for (Order order : successfulOrders) {
            for (var item : order.getItems()) {
                String category = extractCategory(item.getProductName());
                revenueByCategory.merge(category, item.getSubtotal(), Double::sum);
            }
        }

        return SalesAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .dailySales(dailySales)
                .revenueByCategory(revenueByCategory)
                .build();
    }

    public OrderAnalyticsResponse getOrderAnalytics(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getStatus().name(),
                    Collectors.counting()
                ));

        long totalOrders = orders.size();
        long pendingOrders = ordersByStatus.getOrDefault("PENDING", 0L);
        long confirmedOrders = ordersByStatus.getOrDefault("CONFIRMED", 0L);
        long shippedOrders = ordersByStatus.getOrDefault("SHIPPED", 0L);
        long deliveredOrders = ordersByStatus.getOrDefault("DELIVERED", 0L);
        long cancelledOrders = ordersByStatus.getOrDefault("CANCELLED", 0L);

        double fulfillmentRate = totalOrders > 0 
                ? (double) deliveredOrders / totalOrders * 100 
                : 0;
        double cancellationRate = totalOrders > 0 
                ? (double) cancelledOrders / totalOrders * 100 
                : 0;

        return OrderAnalyticsResponse.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .ordersByStatus(ordersByStatus)
                .fulfillmentRate(fulfillmentRate)
                .cancellationRate(cancellationRate)
                .build();
    }

    public List<TopProductResponse> getTopProducts(int limit) {
        // Get all orders and aggregate product sales
        List<Order> allOrders = orderRepository.findAll();
        
        Map<String, ProductSalesData> productSales = new HashMap<>();
        
        for (Order order : allOrders) {
            if (order.getStatus() == Order.OrderStatus.CANCELLED || 
                order.getStatus() == Order.OrderStatus.REFUNDED) {
                continue;
            }
            
            for (var item : order.getItems()) {
                ProductSalesData data = productSales.computeIfAbsent(
                    item.getProductId(), 
                    k -> new ProductSalesData(item.getProductId(), item.getProductName())
                );
                data.totalSold += item.getQuantity();
                data.totalRevenue += item.getSubtotal();
            }
        }

        return productSales.values().stream()
                .sorted((a, b) -> Long.compare(b.totalSold, a.totalSold))
                .limit(limit)
                .map(data -> TopProductResponse.builder()
                        .productId(data.productId)
                        .productName(data.productName)
                        .category("General")
                        .totalSold(data.totalSold)
                        .totalRevenue(data.totalRevenue)
                        .averageRating(0.0)
                        .build())
                .collect(Collectors.toList());
    }

    public UserAnalyticsResponse getUserAnalytics() {
        List<User> allUsers = userRepository.findAll();
        
        long totalUsers = allUsers.size();
        long activeUsers = userRepository.countByEnabled(true);
        
        Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        
        long newUsersThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(monthAgo))
                .count();
        
        long newUsersThisWeek = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(weekAgo))
                .count();

        Map<String, Long> usersByRole = new HashMap<>();
        for (User user : allUsers) {
            for (User.Role role : user.getRoles()) {
                usersByRole.merge(role.name(), 1L, Long::sum);
            }
        }

        return UserAnalyticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .newUsersThisWeek(newUsersThisWeek)
                .usersByRole(usersByRole)
                .build();
    }

    public DashboardResponse getDashboard() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);

        // This month stats
        SalesAnalyticsResponse currentMonthSales = getSalesAnalytics(monthStart, now);
        SalesAnalyticsResponse lastMonthSales = getSalesAnalytics(lastMonthStart, lastMonthEnd);

        double revenueGrowth = lastMonthSales.getTotalRevenue() > 0
                ? ((currentMonthSales.getTotalRevenue() - lastMonthSales.getTotalRevenue()) 
                   / lastMonthSales.getTotalRevenue()) * 100
                : 100;

        double orderGrowth = lastMonthSales.getTotalOrders() > 0
                ? ((double)(currentMonthSales.getTotalOrders() - lastMonthSales.getTotalOrders()) 
                   / lastMonthSales.getTotalOrders()) * 100
                : 100;

        long totalProducts = productRepository.countByActiveTrue();
        long totalUsers = userRepository.count();

        // Recent orders
        List<Order> recentOrders = orderRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        List<DashboardResponse.RecentOrder> recentOrderDtos = recentOrders.stream()
                .map(o -> DashboardResponse.RecentOrder.builder()
                        .orderId(o.getId())
                        .customerName(o.getUsername())
                        .amount(o.getTotalAmount())
                        .status(o.getStatus().name())
                        .createdAt(o.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalRevenue(currentMonthSales.getTotalRevenue())
                .totalOrders(currentMonthSales.getTotalOrders())
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .revenueGrowth(revenueGrowth)
                .orderGrowth(orderGrowth)
                .recentOrders(recentOrderDtos)
                .topProducts(getTopProducts(5))
                .build();
    }

    private String extractCategory(String productName) {
        // Simple category extraction - in real app, this would come from product data
        if (productName == null) return "Other";
        return "General";
    }

    private static class ProductSalesData {
        String productId;
        String productName;
        long totalSold = 0;
        double totalRevenue = 0;

        ProductSalesData(String productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }
    }
}
