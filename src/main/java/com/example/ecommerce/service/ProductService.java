package com.example.ecommerce.service;

import com.example.ecommerce.dto.PageResponse;
import com.example.ecommerce.dto.product.ProductRequest;
import com.example.ecommerce.dto.product.ProductResponse;
import com.example.ecommerce.dto.product.ProductUpdateRequest;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }

    public Product getProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Cacheable(value = "productList", key = "#page + '_' + #size + '_' + #sortBy + '_' + #sortDir")
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return buildPageResponse(productPage);
    }

    public PageResponse<ProductResponse> searchProducts(String keyword, String category,
            Double minPrice, Double maxPrice,
            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage;

        if (keyword != null && !keyword.isEmpty()) {
            productPage = productRepository.searchProducts(keyword, pageable);
        } else if (category != null && !category.isEmpty() && minPrice != null && maxPrice != null) {
            productPage = productRepository.findByActiveTrueAndCategoryAndPriceBetween(category, minPrice, maxPrice,
                    pageable);
        } else if (category != null && !category.isEmpty()) {
            productPage = productRepository.findByActiveTrueAndCategory(category, pageable);
        } else if (minPrice != null && maxPrice != null) {
            productPage = productRepository.findByActiveTrueAndPriceBetween(minPrice, maxPrice, pageable);
        } else {
            productPage = productRepository.findByActiveTrue(pageable);
        }

        return buildPageResponse(productPage);
    }

    public PageResponse<ProductResponse> getProductsBySeller(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findBySellerIdAndActiveTrue(sellerId, pageable);
        return buildPageResponse(productPage);
    }

    @CacheEvict(value = { "products", "productList" }, allEntries = true)
    public ProductResponse createProduct(ProductRequest request, User seller) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .imageUrls(request.getImageUrls())
                .sellerId(seller.getId())
                .sellerName(seller.getUsername())
                .rating(0.0)
                .reviewCount(0)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @CacheEvict(value = { "products", "productList" }, allEntries = true)
    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getPrice() != null)
            product.setPrice(request.getPrice());
        if (request.getStock() != null)
            product.setStock(request.getStock());
        if (request.getCategory() != null)
            product.setCategory(request.getCategory());
        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
        }
        product.setUpdatedAt(Instant.now());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @CacheEvict(value = { "products", "productList" }, allEntries = true)
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }

    @CacheEvict(value = { "products", "productList" }, allEntries = true)
    public void updateStock(String productId, int quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        product.setStock(product.getStock() + quantityChange);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }

    public List<ProductResponse> getTopRatedProducts(int limit) {
        return productRepository.findTop10ByActiveTrueOrderByRatingDesc()
                .stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PageResponse<ProductResponse> buildPageResponse(Page<Product> productPage) {
        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
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
}