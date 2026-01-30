package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ecommerce.com}")
    private String fromEmail;

    @Async
    public void sendOrderConfirmation(String to, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Order Confirmation - " + order.getId());
            message.setText(buildOrderConfirmationEmail(order));
            
            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}. Error: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to E-Commerce Store!");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Welcome to our E-Commerce Store!\n\n" +
                "Thank you for registering. You can now:\n" +
                "- Browse our products\n" +
                "- Add items to your cart\n" +
                "- Place orders\n\n" +
                "Happy Shopping!\n\n" +
                "Best regards,\n" +
                "The E-Commerce Team", username
            ));
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Password Reset Request");
            message.setText(String.format(
                "Hello,\n\n" +
                "We received a request to reset your password.\n\n" +
                "Your password reset token is: %s\n\n" +
                "This token will expire in 1 hour.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The E-Commerce Team", resetToken
            ));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendShippingUpdateEmail(String to, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Your Order Has Been Shipped - " + order.getId());
            message.setText(String.format(
                "Hello,\n\n" +
                "Great news! Your order has been shipped.\n\n" +
                "Order ID: %s\n" +
                "Tracking Number: %s\n\n" +
                "You can track your order status in your account.\n\n" +
                "Thank you for shopping with us!\n\n" +
                "Best regards,\n" +
                "The E-Commerce Team", 
                order.getId(), 
                order.getTrackingNumber() != null ? order.getTrackingNumber() : "N/A"
            ));
            
            mailSender.send(message);
            log.info("Shipping update email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send shipping update email to: {}. Error: {}", to, e.getMessage());
        }
    }

    private String buildOrderConfirmationEmail(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello,\n\n");
        sb.append("Thank you for your order!\n\n");
        sb.append("Order Details:\n");
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("Total Amount: $").append(String.format("%.2f", order.getTotalAmount())).append("\n\n");
        
        sb.append("Items:\n");
        for (var item : order.getItems()) {
            sb.append("- ").append(item.getProductName())
              .append(" x ").append(item.getQuantity())
              .append(" = $").append(String.format("%.2f", item.getSubtotal()))
              .append("\n");
        }
        
        if (order.getShippingAddress() != null) {
            sb.append("\nShipping Address:\n");
            sb.append(order.getShippingAddress().getStreet()).append("\n");
            sb.append(order.getShippingAddress().getCity()).append(", ");
            sb.append(order.getShippingAddress().getState()).append(" ");
            sb.append(order.getShippingAddress().getZipCode()).append("\n");
            sb.append(order.getShippingAddress().getCountry()).append("\n");
        }
        
        sb.append("\nWe will notify you when your order ships.\n\n");
        sb.append("Thank you for shopping with us!\n\n");
        sb.append("Best regards,\n");
        sb.append("The E-Commerce Team");
        
        return sb.toString();
    }
}
