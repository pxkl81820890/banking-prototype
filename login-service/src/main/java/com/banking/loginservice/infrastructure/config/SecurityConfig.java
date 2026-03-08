package com.banking.loginservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the login service.
 * Provides BCrypt password encoder and configures security for login endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable()) // CORS handled by WebConfig
            .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Allow H2 console frames
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/auth/login").permitAll() // Allow login endpoint
                .requestMatchers("/actuator/health").permitAll() // Allow health check
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll() // Allow Swagger UI
                .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll() // Allow OpenAPI docs
                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console
                .anyRequest().authenticated() // Require authentication for other endpoints
            );
        
        return http.build();
    }
}
