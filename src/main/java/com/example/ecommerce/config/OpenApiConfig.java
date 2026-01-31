package com.example.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${server.port:8080}")
        private int serverPort;

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("E-Commerce Backend API")
                                                .version("1.0.0")
                                                .description("Production-grade E-Commerce Backend API built with Spring Boot. "
                                                                +
                                                                "This API provides comprehensive functionality for managing products, orders, "
                                                                +
                                                                "carts, payments, and users with JWT authentication and role-based access control.")
                                                .contact(new Contact()
                                                                .name("API Support")
                                                                .email("support@ecommerce.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:" + serverPort)
                                                                .description("Local Development Server")))
                                .components(new Components()
                                                .addSecuritySchemes("Bearer Authentication",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .bearerFormat("JWT")
                                                                                .scheme("bearer")
                                                                                .description("Enter your JWT token obtained from the /api/auth/login endpoint"))
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .bearerFormat("JWT")
                                                                                .scheme("bearer")
                                                                                .description("Enter your JWT token obtained from the /api/auth/login endpoint")));
        }
}
