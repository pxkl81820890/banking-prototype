package com.banking.authservice.domain.ports;

import com.banking.authservice.domain.model.TokenPayload;

/**
 * Outbound port (driven port) for JWT token signing operations.
 * Defines the contract for the infrastructure layer to implement.
 */
public interface TokenProviderOutputPort {
    String signToken(TokenPayload payload, long expirationSeconds);
}
