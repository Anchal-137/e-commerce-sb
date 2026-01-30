package com.example.ecommerce.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private String id;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
