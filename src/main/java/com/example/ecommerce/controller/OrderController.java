package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.order.OrderRequest;
import com.example.ecommerce.dto.order.OrderResponse;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order from cart items")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal User user) {
        OrderResponse order = orderService.createOrder(user.getId(), user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Get order details by order ID")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get my orders", description = "Get current user's orders with pagination")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        PageResponse<OrderResponse> orders = orderService.getUserOrders(user.getId(), page, size);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only pending/confirmed orders)")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId,
            @AuthenticationPrincipal User user) {
        OrderResponse order = orderService.cancelOrder(orderId, user.getId());
        return ResponseEntity.ok(order);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (Admin)", description = "Get all orders with pagination (Admin only)")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<OrderResponse> orders = orderService.getAllOrders(page, size, sortBy, sortDir);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by status (Admin)", description = "Get orders filtered by status (Admin only)")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<OrderResponse> orders = orderService.getOrdersByStatus(status, page, size);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (Admin)", description = "Update the status of an order (Admin only)")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable String orderId,
            @Parameter(description = "New status") @RequestParam Order.OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
}