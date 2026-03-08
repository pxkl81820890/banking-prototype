package com.banking.loginservice.domain.ports;

/**
 * Outbound Port (Driven Port) for authentication service communication.
 * Defines the interface for generating authentication tokens in the hexagonal architecture.
 * This port is implemented by infrastructure adapters (e.g., HTTP client to authentication service).
 */
public interface AuthenticationOutputPort {
    
    /**
     * Generates an authentication token for a successfully authenticated user.
     * Calls the external authentication service to create a JWT token with user context.
     * 
     * @param userId the authenticated user's ID
     * @param bankCode the user's bank code
     * @param branchCode the user's branch code
     * @param currency the user's currency
     * @return the generated JWT token
     */
    String generateToken(String userId, String bankCode, String branchCode, String currency);
}