package com.banking.loginservice.domain.ports;

import com.banking.loginservice.domain.model.User;
import java.util.Optional;

/**
 * Outbound Port (Driven Port) for user data retrieval.
 * Defines the interface for accessing user data in the hexagonal architecture.
 * This port is implemented by infrastructure adapters (e.g., database repositories).
 */
public interface UserOutputPort {
    
    /**
     * Finds a user by bank code, branch code, and username.
     * This method supports multi-entity authentication where users are uniquely identified
     * by the combination of bank, branch, and username.
     * 
     * @param bankCode the 3-digit bank code (e.g., "101")
     * @param branchCode the 4-digit branch code (e.g., "1119")
     * @param username the username to search for
     * @return Optional containing the User if found, empty otherwise
     */
    Optional<User> findByBankCodeAndBranchCodeAndUsername(String bankCode, String branchCode, String username);
}
