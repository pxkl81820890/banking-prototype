package com.banking.authservice.domain.model;

/**
 * Value object representing a generated JWT token with metadata.
 */
public record GeneratedToken(
    String token,
    long expiresIn,
    String message
) {}
