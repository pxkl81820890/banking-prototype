package com.banking.authservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration for the authentication service.
 * Disables default authentication for token generation endpoint.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(new AntPathRequestMatcher("/.well-known/jwks.json")).permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll() // Allow Swagger UI
                .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll() // Allow OpenAPI docs
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
