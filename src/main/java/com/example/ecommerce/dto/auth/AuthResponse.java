package com.example.ecommerce.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String userId;
    private String username;
    private String email;
    private Set<String> roles;
    private String message;
}
