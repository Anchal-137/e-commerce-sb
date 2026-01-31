package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.product.ProductRequest;
import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.dto.product.ProductUpdateRequest;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.ProductService;
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

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Get paginated list of all active products")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<ProductResponse> products = productService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get a single product by its ID")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable String id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products with filters")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<ProductResponse> products = productService.searchProducts(
                keyword, category, minPrice, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated products", description = "Get list of top rated products")
    public ResponseEntity<List<ProductResponse>> getTopRatedProducts(
            @Parameter(description = "Number of products to return") @RequestParam(defaultValue = "10") int limit) {
        List<ProductResponse> products = productService.getTopRatedProducts(limit);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product (Admin/Seller only)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User user) {
        ProductResponse product = productService.createProduct(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product (Admin/Seller only)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Soft delete a product (Admin only)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller", description = "Get all products from a specific seller")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsBySeller(
            @Parameter(description = "Seller ID") @PathVariable String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ProductResponse> products = productService.getProductsBySeller(sellerId, page, size);
        return ResponseEntity.ok(products);
    }
}