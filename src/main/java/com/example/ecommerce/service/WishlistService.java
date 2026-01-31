package com.example.ecommerce.service;

import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.dto.wishlist.WishlistItemResponse;
import com.example.ecommerce.dto.wishlist.WishlistResponse;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.Wishlist;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    /**
     * Add a product to user's wishlist
     */
    @Transactional
    public WishlistItemResponse addToWishlist(String userId, String productId) {
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Product is already in your wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .userId(userId)
                .productId(productId)
                .addedAt(Instant.now())
                .build();

        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        
        log.info("User {} added product {} to wishlist", userId, productId);
        
        return mapToItemResponse(savedWishlist, product);
    }

    /**
     * Remove a product from user's wishlist
     */
    @Transactional
    public void removeFromWishlist(String userId, String productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item", "productId", productId));

        wishlistRepository.delete(wishlist);
        
        log.info("User {} removed product {} from wishlist", userId, productId);
    }

    /**
     * Get user's complete wishlist
     */
    public WishlistResponse getUserWishlist(String userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        
        List<WishlistItemResponse> items = wishlists.stream()
                .map(wishlist -> {
                    Product product = productRepository.findById(wishlist.getProductId())
                            .orElse(null);
                    return mapToItemResponse(wishlist, product);
                })
                .filter(item -> item.getProduct() != null) // Filter out deleted products
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .userId(userId)
                .totalItems(items.size())
                .items(items)
                .build();
    }

    /**
     * Check if a product is in user's wishlist
     */
    public boolean isInWishlist(String userId, String productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * Clear entire wishlist for a user
     */
    @Transactional
    public void clearWishlist(String userId) {
        wishlistRepository.deleteByUserId(userId);
        log.info("Cleared wishlist for user {}", userId);
    }

    /**
     * Get wishlist count for a user
     */
    public long getWishlistCount(String userId) {
        return wishlistRepository.countByUserId(userId);
    }

    /**
     * Move item from wishlist to cart (utility method - actual cart logic in CartService)
     */
    @Transactional
    public void moveToCart(String userId, String productId) {
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Wishlist item", "productId", productId);
        }
        
        // Remove from wishlist - cart addition should be called separately
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        
        log.info("User {} moved product {} from wishlist (ready for cart)", userId, productId);
    }

    private WishlistItemResponse mapToItemResponse(Wishlist wishlist, Product product) {
        ProductResponse productResponse = null;
        
        if (product != null) {
            productResponse = ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .category(product.getCategory())
                    .imageUrls(product.getImageUrls())
                    .sellerId(product.getSellerId())
                    .sellerName(product.getSellerName())
                    .rating(product.getRating())
                    .reviewCount(product.getReviewCount())
                    .active(product.isActive())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
        }

        return WishlistItemResponse.builder()
                .id(wishlist.getId())
                .productId(wishlist.getProductId())
                .product(productResponse)
                .addedAt(wishlist.getAddedAt())
                .build();
    }
}
