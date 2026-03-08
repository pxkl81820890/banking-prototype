package com.banking.loginservice.infrastructure.adapters.out;

import com.banking.loginservice.domain.ports.AuthenticationOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * HTTP adapter for authentication service communication.
 * Implements AuthenticationOutputPort using WebClient to call external authentication service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceAdapter implements AuthenticationOutputPort {

    private final WebClient webClient;
    
    @Value("${app.authentication-service.url:http://localhost:8081}")
    private String authenticationServiceUrl;
    
    @Value("${app.authentication-service.timeout:5s}")
    private Duration timeout;

    @Override
    public String generateToken(String userId, String bankCode, String branchCode, String currency) {
        log.info("Calling authentication service for user: {}", userId);
        
        try {
            AuthenticationRequest request = new AuthenticationRequest(userId, bankCode, branchCode, currency);
            
            AuthenticationResponse response = webClient
                .post()
                .uri(authenticationServiceUrl + "/api/v1/auth/generate-token")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthenticationResponse.class)
                .timeout(timeout)
                .block();
            
            if (response != null && response.success()) {
                log.info("Authentication service responded successfully for user: {}", userId);
                return response.token();
            } else {
                log.error("Authentication service failed for user: {}", userId);
                throw new RuntimeException("Authentication service failed");
            }
            
        } catch (Exception e) {
            log.error("Error calling authentication service for user: {}", userId, e);
            // For now, return a fallback message as requested
            return "authentication-success!";
        }
    }
    
    /**
     * Request DTO for authentication service.
     */
    public record AuthenticationRequest(
        String userId,
        String bankCode,
        String branchCode,
        String currency
    ) {}
    
    /**
     * Response DTO from authentication service.
     */
    public record AuthenticationResponse(
        boolean success,
        String token,
        String message
    ) {}
}