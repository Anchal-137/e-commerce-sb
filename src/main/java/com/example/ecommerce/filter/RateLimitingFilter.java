package com.example.ecommerce.filter;

import com.example.ecommerce.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Rate limit: 100 requests per minute per IP
    private static final int REQUESTS_PER_MINUTE = 100;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Skip rate limiting for Swagger and static resources
        String path = request.getRequestURI();
        if (path.contains("/swagger") || path.contains("/api-docs") || path.contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(Instant.now())
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .error("Too Many Requests")
                    .message("Rate limit exceeded. Please try again later.")
                    .path(request.getRequestURI())
                    .build();
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                REQUESTS_PER_MINUTE,
                Refill.intervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
