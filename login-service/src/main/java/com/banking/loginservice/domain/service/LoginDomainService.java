package com.banking.loginservice.domain.service;

import com.banking.loginservice.domain.exception.CurrencyMismatchException;
import com.banking.loginservice.domain.exception.InvalidCredentialsException;
import com.banking.loginservice.domain.exception.InvalidEntityException;
import com.banking.loginservice.domain.model.LoginResult;
import com.banking.loginservice.domain.model.User;
import com.banking.loginservice.domain.ports.AuthenticationOutputPort;
import com.banking.loginservice.domain.ports.LoginUseCase;
import com.banking.loginservice.domain.ports.UserOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Domain service implementing the login use case.
 * Contains core business logic for multi-entity banking authentication.
 * Uses constructor injection via Lombok @RequiredArgsConstructor.
 */
@Service
@RequiredArgsConstructor
public class LoginDomainService implements LoginUseCase {

    private final UserOutputPort userOutputPort;
    private final AuthenticationOutputPort authenticationOutputPort;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Executes the login use case with multi-entity validation.
     * 
     * @param bankCode the 3-digit bank code
     * @param branchCode the 4-digit branch code
     * @param username the username for authentication
     * @param password the plain-text password to verify
     * @param currency the currency code
     * @return LoginResult containing success status and details
     */
    @Override
    public LoginResult login(String bankCode, String branchCode, String username, String password, String currency) {
        // Step 1: Identify the Entity - Locate user by bankCode, branchCode, and username
        Optional<User> userOptional = userOutputPort.findByBankCodeAndBranchCodeAndUsername(bankCode, branchCode, username);
        
        if (userOptional.isEmpty()) {
            throw new InvalidEntityException(bankCode, branchCode);
        }
        
        User user = userOptional.get();
        
        // Step 2: Verify Security - Validate password using BCrypt
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        
        // Step 3: Verify Currency - Ensure the provided currency matches the user's currency
        if (!currency.equals(user.currency())) {
            throw new CurrencyMismatchException();
        }
        
        // Step 4: Generate token via Authentication Service
        String token = authenticationOutputPort.generateToken(user.userId(), bankCode, branchCode, currency);
        
        return LoginResult.success(user.userId(), token);
    }
}
