package com.banking.authservice.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for token generation endpoint.
 */
@Schema(description = "Token generation response with JWT token")
public record TokenResponse(
    @Schema(description = "Indicates if token generation was successful", example = "true")
    boolean success,
    
    @Schema(description = "RS256-signed JWT token with custom claims", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token,
    
    @Schema(description = "Human-readable status message", example = "Token generated successfully")
    String message,
    
    @Schema(description = "Token expiration time in seconds", example = "3600")
    long expiresIn
) {}
