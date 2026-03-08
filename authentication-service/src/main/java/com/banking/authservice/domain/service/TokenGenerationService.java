package com.banking.authservice.domain.service;

import com.banking.authservice.domain.model.GeneratedToken;
import com.banking.authservice.domain.model.TokenPayload;
import com.banking.authservice.domain.ports.TokenGenerationUseCase;
import com.banking.authservice.domain.ports.TokenProviderOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Domain service implementing token generation use case.
 * Orchestrates token generation by delegating to the token provider.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenGenerationService implements TokenGenerationUseCase {

    private final TokenProviderOutputPort tokenProvider;
    
    @Value("${app.jwt.expiration}")
    private long expirationSeconds;

    @Override
    public GeneratedToken generateToken(TokenPayload payload) {
        log.debug("Generating token for user: {}", payload.userId());
        
        String token = tokenProvider.signToken(payload, expirationSeconds);
        
        log.info("Token generated successfully for user: {}", payload.userId());
        return new GeneratedToken(token, expirationSeconds, "Token generated successfully");
    }
}
