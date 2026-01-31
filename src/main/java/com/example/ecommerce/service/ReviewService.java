package com.example.ecommerce.service;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.review.ReviewRequest;
import com.example.ecommerce.dto.review.ReviewResponse;
import com.example.ecommerce.dto.review.ReviewStatsResponse;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.Review;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Create a new review for a product
     */
    @Transactional
    @CacheEvict(value = {"reviews", "reviewStats", "products"}, allEntries = true)
    public ReviewResponse createReview(ReviewRequest request, User user) {
        // Check if product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), request.getProductId())) {
            throw new BadRequestException("You have already reviewed this product");
        }

        // Check if user has purchased this product (verified purchase)
        boolean isVerifiedPurchase = checkVerifiedPurchase(user.getId(), request.getProductId());

        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(user.getId())
                .username(user.getUsername())
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .verified(isVerifiedPurchase)
                .helpfulCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(request.getProductId());
        
        log.info("User {} created review for product {}", user.getId(), request.getProductId());
        
        return mapToResponse(savedReview);
    }

    /**
     * Update an existing review
     */
    @Transactional
    @CacheEvict(value = {"reviews", "reviewStats", "products"}, allEntries = true)
    public ReviewResponse updateReview(String reviewId, ReviewRequest request, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new BadRequestException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setUpdatedAt(Instant.now());

        Review updatedReview = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(review.getProductId());
        
        log.info("User {} updated review {}", userId, reviewId);
        
        return mapToResponse(updatedReview);
    }

    /**
     * Delete a review
     */
    @Transactional
    @CacheEvict(value = {"reviews", "reviewStats", "products"}, allEntries = true)
    public void deleteReview(String reviewId, String userId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own reviews");
        }

        String productId = review.getProductId();
        reviewRepository.delete(review);
        
        // Update product rating
        updateProductRating(productId);
        
        log.info("Review {} deleted by user {}", reviewId, userId);
    }

    /**
     * Get reviews for a product with pagination
     */
    @Cacheable(value = "reviews", key = "'product_' + #productId + '_' + #page + '_' + #size + '_' + #sortBy")
    public PageResponse<ReviewResponse> getProductReviews(String productId, int page, int size, String sortBy) {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        Sort sort = sortBy.equals("helpful") 
                ? Sort.by("helpfulCount").descending()
                : Sort.by("createdAt").descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);
        
        return buildPageResponse(reviewPage);
    }

    /**
     * Get reviews by a specific user
     */
    public PageResponse<ReviewResponse> getUserReviews(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);
        
        return buildPageResponse(reviewPage);
    }

    /**
     * Get review statistics for a product
     */
    @Cacheable(value = "reviewStats", key = "#productId")
    public ReviewStatsResponse getProductReviewStats(String productId) {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<Review> reviews = reviewRepository.findAllByProductIdForStats(productId);
        
        if (reviews.isEmpty()) {
            return ReviewStatsResponse.builder()
                    .productId(productId)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .fiveStarCount(0)
                    .fourStarCount(0)
                    .threeStarCount(0)
                    .twoStarCount(0)
                    .oneStarCount(0)
                    .build();
        }

        Map<Integer, Long> ratingCounts = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return ReviewStatsResponse.builder()
                .productId(productId)
                .averageRating(Math.round(avgRating * 10.0) / 10.0) // Round to 1 decimal
                .totalReviews(reviews.size())
                .fiveStarCount(ratingCounts.getOrDefault(5, 0L).intValue())
                .fourStarCount(ratingCounts.getOrDefault(4, 0L).intValue())
                .threeStarCount(ratingCounts.getOrDefault(3, 0L).intValue())
                .twoStarCount(ratingCounts.getOrDefault(2, 0L).intValue())
                .oneStarCount(ratingCounts.getOrDefault(1, 0L).intValue())
                .build();
    }

    /**
     * Mark a review as helpful
     */
    @CacheEvict(value = "reviews", allEntries = true)
    public ReviewResponse markReviewHelpful(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        review.setUpdatedAt(Instant.now());
        
        Review updatedReview = reviewRepository.save(review);
        return mapToResponse(updatedReview);
    }

    /**
     * Check if user has purchased the product
     */
    private boolean checkVerifiedPurchase(String userId, String productId) {
        List<Order> userOrders = orderRepository.findByUserId(userId);
        return userOrders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProductId().equals(productId));
    }

    /**
     * Update product's average rating based on reviews
     */
    private void updateProductRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (reviews.isEmpty()) {
            product.setRating(0.0);
            product.setReviewCount(0);
        } else {
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            
            product.setRating(Math.round(avgRating * 10.0) / 10.0);
            product.setReviewCount(reviews.size());
        }
        
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }

    private PageResponse<ReviewResponse> buildPageResponse(Page<Review> reviewPage) {
        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .first(reviewPage.isFirst())
                .last(reviewPage.isLast())
                .build();
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .username(review.getUsername())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .helpfulCount(review.getHelpfulCount())
                .verified(review.isVerified())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
