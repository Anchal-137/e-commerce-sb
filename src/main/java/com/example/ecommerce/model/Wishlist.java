package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wishlists")
@CompoundIndex(name = "user_product_wishlist_idx", def = "{'userId': 1, 'productId': 1}", unique = true)
public class Wishlist {

    @Id
    private String id;
    
    private String userId;
    
    private String productId;
    
    private Instant addedAt;
}
