package com.example.ecommerce.controller;

import com.example.ecommerce.dto.user.UserResponse;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get currently authenticated user's profile")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        UserResponse userResponse = userService.getUserById(user.getId());
        return ResponseEntity.ok(userResponse);
    }
}
