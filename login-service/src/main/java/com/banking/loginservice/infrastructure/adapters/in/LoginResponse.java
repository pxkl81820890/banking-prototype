package com.banking.loginservice.infrastructure.adapters.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for login endpoint.
 * Java Record as per tech-stack.md requirements.
 */
@Schema(description = "Login response with authentication token")
public record LoginResponse(
    @Schema(description = "Indicates if login was successful", example = "true")
    boolean success,
    
    @Schema(description = "Unique user identifier (UUID)", example = "user-uuid-123")
    String userId,
    
    @Schema(description = "Human-readable status message", example = "Login successful")
    String message,
    
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token
) {
}
