package com.example.ecommerce.service;

import com.example.ecommerce.dto.auth.AuthResponse;
import com.example.ecommerce.dto.auth.LoginRequest;
import com.example.ecommerce.dto.auth.RegisterRequest;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        // Determine role
        Set<User.Role> roles = new HashSet<>();
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("SELLER")) {
            roles.add(User.Role.ROLE_SELLER);
        } else if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            roles.add(User.Role.ROLE_ADMIN);
        } else {
            roles.add(User.Role.ROLE_USER);
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);

        // Generate token
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .message("Login successful")
                .build();
    }

    public AuthResponse refreshToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String newToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .message("Token refreshed successfully")
                .build();
    }
}
