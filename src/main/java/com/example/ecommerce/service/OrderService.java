package com.example.ecommerce.service;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.order.OrderRequest;
import com.example.ecommerce.dto.order.OrderResponse;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Transactional
    public OrderResponse createOrder(String userId, String username, OrderRequest request) {
        List<CartItem> cartItems = cartService.getCartItems(userId);
        
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty. Add items to cart before placing an order.");
        }
        
        // Validate stock and calculate total
        double totalAmount = 0;
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", cartItem.getProductId()));
            
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
            
            double subtotal = product.getPrice() * cartItem.getQuantity();
            totalAmount += subtotal;
            
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            orderItems.add(orderItem);
            
            // Update stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);
        }
        
        // Create shipping address
        Order.ShippingAddress shippingAddress = Order.ShippingAddress.builder()
                .street(request.getShippingAddress().getStreet())
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .zipCode(request.getShippingAddress().getZipCode())
                .country(request.getShippingAddress().getCountry())
                .phoneNumber(request.getShippingAddress().getPhoneNumber())
                .build();
        
        // Create order
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .username(username)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .items(orderItems)
                .shippingAddress(shippingAddress)
                .notes(request.getNotes())
                .paymentStatus("PENDING")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart
        cartService.clearCart(userId);
        
        // Send order confirmation email (async)
        try {
            emailService.sendOrderConfirmation(username, savedOrder);
        } catch (Exception e) {
            // Log error but don't fail the order
        }
        
        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToResponse(order);
    }

    public Order getOrderEntityById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    public PageResponse<OrderResponse> getUserOrders(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        return buildPageResponse(orderPage);
    }

    public PageResponse<OrderResponse> getAllOrders(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return buildPageResponse(orderPage);
    }

    public PageResponse<OrderResponse> getOrdersByStatus(Order.OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);
        return buildPageResponse(orderPage);
    }

    public OrderResponse updateOrderStatus(String orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        
        if (status == Order.OrderStatus.SHIPPED) {
            order.setTrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    public OrderResponse cancelOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }
        
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }
        
        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setUpdatedAt(Instant.now());
                productRepository.save(product);
            }
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        Order cancelledOrder = orderRepository.save(order);
        
        return mapToResponse(cancelledOrder);
    }

    public void updatePaymentInfo(String orderId, String paymentId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        order.setPaymentId(paymentId);
        order.setPaymentStatus(paymentStatus);
        
        if ("SUCCESS".equals(paymentStatus)) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
        }
        
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    private PageResponse<OrderResponse> buildPageResponse(Page<Order> orderPage) {
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemDto> items = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
        
        OrderResponse.ShippingAddressDto shippingAddress = null;
        if (order.getShippingAddress() != null) {
            shippingAddress = OrderResponse.ShippingAddressDto.builder()
                    .street(order.getShippingAddress().getStreet())
                    .city(order.getShippingAddress().getCity())
                    .state(order.getShippingAddress().getState())
                    .zipCode(order.getShippingAddress().getZipCode())
                    .country(order.getShippingAddress().getCountry())
                    .phoneNumber(order.getShippingAddress().getPhoneNumber())
                    .build();
        }
        
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .username(order.getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .items(items)
                .shippingAddress(shippingAddress)
                .trackingNumber(order.getTrackingNumber())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}