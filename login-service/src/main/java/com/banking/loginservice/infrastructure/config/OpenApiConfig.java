package com.banking.loginservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Login Service.
 * Provides interactive API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loginServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Login Service API")
                        .description("Multi-entity banking login service that handles user authentication with bank, branch, and currency context. Follows hexagonal architecture principles with domain-driven design.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Banking Platform Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.banking.example.com")
                                .description("Production server")
                ));
    }
}
