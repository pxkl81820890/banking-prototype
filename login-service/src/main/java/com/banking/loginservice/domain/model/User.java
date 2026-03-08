package com.banking.loginservice.domain.model;

/**
 * Domain model representing a user in the banking system.
 * Pure Java record with no framework dependencies.
 */
public record User(
    String userId,
    String passwordHash,
    String bankCode,
    String branchCode,
    String currency
) {
    public User {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash cannot be null or blank");
        }
        if (bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("bankCode cannot be null or blank");
        }
        if (branchCode == null || branchCode.isBlank()) {
            throw new IllegalArgumentException("branchCode cannot be null or blank");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency cannot be null or blank");
        }
    }
}
