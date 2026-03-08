package com.banking.authservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Authentication Service.
 * Provides interactive API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authentication Service API")
                        .description("JWT token generation service for multi-entity banking platform. Generates RS256-signed JWT tokens with custom claims for bank, branch, and currency context.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Banking Platform Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local development server"),
                        new Server()
                                .url("https://auth.banking.example.com")
                                .description("Production server")
                ));
    }
}
