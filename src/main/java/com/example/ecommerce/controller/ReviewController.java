package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.review.ReviewRequest;
import com.example.ecommerce.dto.review.ReviewResponse;
import com.example.ecommerce.dto.review.ReviewStatsResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review and rating management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a review", description = "Create a new product review. Users can only review products once.")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal User user) {
        ReviewResponse response = reviewService.createReview(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a review", description = "Update your own review")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal User user) {
        ReviewResponse response = reviewService.updateReview(reviewId, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a review", description = "Delete your own review (or any review if admin)")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String reviewId,
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user.getRoles().contains(User.Role.ROLE_ADMIN);
        reviewService.deleteReview(reviewId, user.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product reviews", description = "Get paginated reviews for a product")
    public ResponseEntity<PageResponse<ReviewResponse>> getProductReviews(
            @PathVariable String productId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by: 'recent' or 'helpful'") @RequestParam(defaultValue = "recent") String sortBy) {
        PageResponse<ReviewResponse> response = reviewService.getProductReviews(productId, page, size, sortBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}/stats")
    @Operation(summary = "Get review statistics", description = "Get rating breakdown and statistics for a product")
    public ResponseEntity<ReviewStatsResponse> getProductReviewStats(@PathVariable String productId) {
        ReviewStatsResponse response = reviewService.getProductReviewStats(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-reviews")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my reviews", description = "Get all reviews written by the current user")
    public ResponseEntity<PageResponse<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ReviewResponse> response = reviewService.getUserReviews(user.getId(), page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reviewId}/helpful")
    @Operation(summary = "Mark review as helpful", description = "Mark a review as helpful (upvote)")
    public ResponseEntity<ReviewResponse> markReviewHelpful(@PathVariable String reviewId) {
        ReviewResponse response = reviewService.markReviewHelpful(reviewId);
        return ResponseEntity.ok(response);
    }
}
