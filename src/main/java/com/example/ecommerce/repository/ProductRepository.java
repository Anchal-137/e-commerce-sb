package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    
    Page<Product> findByActiveTrue(Pageable pageable);
    
    Page<Product> findByActiveTrueAndCategory(String category, Pageable pageable);
    
    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("{ 'active': true, 'name': { $regex: ?0, $options: 'i' } }")
    Page<Product> searchByName(String keyword, Pageable pageable);
    
    @Query("{ 'active': true, $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'category': { $regex: ?0, $options: 'i' } } ] }")
    Page<Product> searchProducts(String keyword, Pageable pageable);
    
    Page<Product> findByActiveTrueAndPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
    
    Page<Product> findByActiveTrueAndCategoryAndPriceBetween(String category, Double minPrice, Double maxPrice, Pageable pageable);
    
    List<Product> findBySellerId(String sellerId);
    
    Page<Product> findBySellerIdAndActiveTrue(String sellerId, Pageable pageable);
    
    long countByActiveTrue();
    
    long countByCategory(String category);
    
    List<Product> findTop10ByActiveTrueOrderByRatingDesc();
}