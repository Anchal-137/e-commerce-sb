package com.example.ecommerce.controller;

import com.example.ecommerce.dto.payment.PaymentRequest;
import com.example.ecommerce.dto.payment.PaymentResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create payment intent", description = "Create a payment intent for an order")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {
        PaymentResponse payment = paymentService.createPaymentIntent(user.getId(), request);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/process")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Process Payment", description = "Process a payment (Alias for create-intent)")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {
        return createPaymentIntent(request, user);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Confirm payment", description = "Confirm a payment intent")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Parameter(description = "Payment Intent ID") @PathVariable String paymentIntentId) {
        PaymentResponse payment = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get payment by order", description = "Get payment details for an order")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/refund/{paymentId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Refund payment (Admin)", description = "Refund a completed payment (Admin only)")
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(description = "Payment ID") @PathVariable String paymentId) {
        PaymentResponse payment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Payment webhook", description = "Handle payment provider webhooks (Stripe)")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("Webhook processed");
    }
}