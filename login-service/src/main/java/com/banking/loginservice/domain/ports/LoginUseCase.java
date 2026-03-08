package com.banking.loginservice.domain.ports;

import com.banking.loginservice.domain.model.LoginResult;

/**
 * Inbound Port (Driving Port) for login operations.
 * Defines the use case interface for authentication in the hexagonal architecture.
 * This port is implemented by the domain service and called by inbound adapters (e.g., REST controllers).
 */
public interface LoginUseCase {
    
    /**
     * Executes the login use case for multi-entity banking authentication.
     * 
     * @param bankCode the 3-digit bank code (e.g., "101")
     * @param branchCode the 4-digit branch code (e.g., "1119")
     * @param username the username for authentication
     * @param password the plain-text password to verify
     * @param currency the currency code (e.g., "SGD")
     * @return LoginResult containing success status, userId, message, and token
     */
    LoginResult login(String bankCode, String branchCode, String username, String password, String currency);
}
