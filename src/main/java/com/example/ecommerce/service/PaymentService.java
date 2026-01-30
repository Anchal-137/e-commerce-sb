package com.example.ecommerce.service;

import com.example.ecommerce.dto.payment.PaymentRequest;
import com.example.ecommerce.dto.payment.PaymentResponse;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    public PaymentResponse createPaymentIntent(String userId, PaymentRequest request) {
        // Verify order exists and belongs to user
        Order order = orderService.getOrderEntityById(request.getOrderId());
        
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }
        
        if (order.getPaymentStatus() != null && order.getPaymentStatus().equals("SUCCESS")) {
            throw new BadRequestException("Order is already paid");
        }
        
        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            Payment existingPayment = paymentRepository.findByOrderId(request.getOrderId()).get();
            if (existingPayment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                throw new BadRequestException("Payment already completed for this order");
            }
        }
        
        // Create payment record (simulated - in real implementation, integrate with Stripe)
        String paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String clientSecret = paymentIntentId + "_secret_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .orderId(request.getOrderId())
                .userId(userId)
                .amount(request.getAmount() != null ? request.getAmount() : order.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(Payment.PaymentStatus.PENDING)
                .paymentIntentId(paymentIntentId)
                .clientSecret(clientSecret)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Payment intent created for order: {}", request.getOrderId());
        
        return mapToResponse(savedPayment);
    }

    public PaymentResponse confirmPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentIntentId", paymentIntentId));
        
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment already confirmed");
        }
        
        // Simulate payment confirmation (in real implementation, verify with Stripe)
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setUpdatedAt(Instant.now());
        
        Payment confirmedPayment = paymentRepository.save(payment);
        
        // Update order payment status
        orderService.updatePaymentInfo(payment.getOrderId(), payment.getId(), "SUCCESS");
        
        log.info("Payment confirmed for order: {}", payment.getOrderId());
        
        return mapToResponse(confirmedPayment);
    }

    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToResponse(payment);
    }

    public PaymentResponse refundPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Can only refund successful payments");
        }
        
        // Simulate refund (in real implementation, process with Stripe)
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setUpdatedAt(Instant.now());
        
        Payment refundedPayment = paymentRepository.save(payment);
        
        // Update order payment status
        orderService.updatePaymentInfo(payment.getOrderId(), payment.getId(), "REFUNDED");
        
        log.info("Payment refunded for order: {}", payment.getOrderId());
        
        return mapToResponse(refundedPayment);
    }

    public void handleWebhook(String payload, String sigHeader) {
        // In real implementation, verify signature and handle Stripe webhooks
        log.info("Webhook received: {}", payload);
        
        // Parse event type and handle accordingly
        // Example: payment_intent.succeeded, payment_intent.failed, etc.
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentIntentId(payment.getPaymentIntentId())
                .clientSecret(payment.getClientSecret())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}