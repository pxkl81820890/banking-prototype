package com.banking.authservice.domain.ports;

import com.banking.authservice.domain.model.GeneratedToken;
import com.banking.authservice.domain.model.TokenPayload;

/**
 * Inbound port (driving port) for token generation use case.
 * Defines the contract for generating JWT tokens.
 */
public interface TokenGenerationUseCase {
    GeneratedToken generateToken(TokenPayload payload);
}
