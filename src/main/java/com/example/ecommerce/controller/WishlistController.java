package com.example.ecommerce.controller;

import com.example.ecommerce.dto.wishlist.WishlistItemResponse;
import com.example.ecommerce.dto.wishlist.WishlistResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist", description = "User wishlist management APIs")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get wishlist", description = "Get current user's complete wishlist with product details")
    public ResponseEntity<WishlistResponse> getWishlist(@AuthenticationPrincipal User user) {
        WishlistResponse response = wishlistService.getUserWishlist(user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{productId}")
    @Operation(summary = "Add to wishlist", description = "Add a product to the wishlist")
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            @PathVariable String productId,
            @AuthenticationPrincipal User user) {
        WishlistItemResponse response = wishlistService.addToWishlist(user.getId(), productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove from wishlist", description = "Remove a product from the wishlist")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable String productId,
            @AuthenticationPrincipal User user) {
        wishlistService.removeFromWishlist(user.getId(), productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check if in wishlist", description = "Check if a product is in the user's wishlist")
    public ResponseEntity<Map<String, Boolean>> checkInWishlist(
            @PathVariable String productId,
            @AuthenticationPrincipal User user) {
        boolean inWishlist = wishlistService.isInWishlist(user.getId(), productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }

    @GetMapping("/count")
    @Operation(summary = "Get wishlist count", description = "Get the number of items in the wishlist")
    public ResponseEntity<Map<String, Long>> getWishlistCount(@AuthenticationPrincipal User user) {
        long count = wishlistService.getWishlistCount(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear wishlist", description = "Remove all items from the wishlist")
    public ResponseEntity<Void> clearWishlist(@AuthenticationPrincipal User user) {
        wishlistService.clearWishlist(user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items/{productId}/move-to-cart")
    @Operation(summary = "Move to cart", description = "Move a product from wishlist (removes from wishlist, ready to add to cart)")
    public ResponseEntity<Map<String, String>> moveToCart(
            @PathVariable String productId,
            @AuthenticationPrincipal User user) {
        wishlistService.moveToCart(user.getId(), productId);
        return ResponseEntity.ok(Map.of("message", "Product removed from wishlist. Please add to cart separately."));
    }
}
