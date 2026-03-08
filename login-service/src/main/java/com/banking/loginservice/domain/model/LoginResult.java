package com.banking.loginservice.domain.model;

/**
 * Domain model representing the outcome of a login attempt.
 * Pure Java record with no framework dependencies.
 */
public record LoginResult(
    boolean success,
    String userId,
    String message,
    String token
) {
    public static LoginResult success(String userId, String token) {
        return new LoginResult(true, userId, "Login successful", token);
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, null, message, null);
    }
}
