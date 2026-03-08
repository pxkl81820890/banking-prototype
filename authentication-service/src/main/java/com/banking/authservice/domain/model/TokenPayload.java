package com.banking.authservice.domain.model;

/**
 * Value object representing the payload for JWT token generation.
 * Contains user and banking context information.
 */
public record TokenPayload(
    String userId,
    String bankCode,
    String branchCode,
    String currency
) {
    public TokenPayload {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or blank");
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
