package com.example.ecommerce.controller;

import com.example.ecommerce.dto.cart.CartItemRequest;
import com.example.ecommerce.dto.cart.CartItemResponse;
import com.example.ecommerce.dto.cart.CartResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Get current user's shopping cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        CartResponse cart = cartService.getCart(user.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    @Operation(summary = "Add to cart", description = "Add a product to the shopping cart")
    public ResponseEntity<CartItemResponse> addToCart(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal User user) {
        CartItemResponse item = cartService.addToCart(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Update quantity of a cart item")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @Parameter(description = "Cart item ID") @PathVariable String itemId,
            @Parameter(description = "New quantity") @RequestParam int quantity,
            @AuthenticationPrincipal User user) {
        CartItemResponse item = cartService.updateCartItem(user.getId(), itemId, quantity);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove from cart", description = "Remove an item from the shopping cart")
    public ResponseEntity<Void> removeFromCart(
            @Parameter(description = "Cart item ID") @PathVariable String itemId,
            @AuthenticationPrincipal User user) {
        cartService.removeFromCart(user.getId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the shopping cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }
}