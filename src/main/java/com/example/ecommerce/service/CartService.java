package com.example.ecommerce.service;

import com.example.ecommerce.dto.cart.CartItemRequest;
import com.example.ecommerce.dto.cart.CartItemResponse;
import com.example.ecommerce.dto.cart.CartResponse;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartItemResponse addToCart(String userId, CartItemRequest request) {
        Product product = productService.getProductEntityById(request.getProductId());
        
        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }
        
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(Instant.now());
        } else {
            cartItem = CartItem.builder()
                    .userId(userId)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .productImageUrl(product.getImageUrls() != null && !product.getImageUrls().isEmpty() 
                            ? product.getImageUrls().get(0) : null)
                    .quantity(request.getQuantity())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }
        
        CartItem savedItem = cartItemRepository.save(cartItem);
        return mapToResponse(savedItem);
    }

    public CartItemResponse updateCartItem(String userId, String itemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));
        
        if (!cartItem.getUserId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to this user");
        }
        
        Product product = productService.getProductEntityById(cartItem.getProductId());
        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }
        
        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(Instant.now());
        CartItem updatedItem = cartItemRepository.save(cartItem);
        return mapToResponse(updatedItem);
    }

    public CartResponse getCart(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        double totalAmount = itemResponses.stream()
                .mapToDouble(CartItemResponse::getSubtotal)
                .sum();
        
        return CartResponse.builder()
                .userId(userId)
                .items(itemResponses)
                .totalItems(items.size())
                .totalAmount(totalAmount)
                .build();
    }

    public List<CartItem> getCartItems(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void removeFromCart(String userId, String itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));
        
        if (!cartItem.getUserId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to this user");
        }
        
        cartItemRepository.delete(cartItem);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemResponse mapToResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .userId(cartItem.getUserId())
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productPrice(cartItem.getProductPrice())
                .productImageUrl(cartItem.getProductImageUrl())
                .quantity(cartItem.getQuantity())
                .subtotal(cartItem.getProductPrice() * cartItem.getQuantity())
                .build();
    }
}