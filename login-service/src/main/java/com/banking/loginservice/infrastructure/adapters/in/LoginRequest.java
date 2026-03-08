package com.banking.loginservice.infrastructure.adapters.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request DTO for login endpoint.
 * Java Record as per tech-stack.md requirements.
 */
@Schema(description = "Login request with multi-entity banking context")
public record LoginRequest(
    @Schema(description = "3-digit bank code", example = "101", required = true)
    String bankCode,
    
    @Schema(description = "4-digit branch code", example = "1119", required = true)
    String branchCode,
    
    @Schema(description = "User's login username", example = "john.doe", required = true)
    String username,
    
    @Schema(description = "User's password", example = "securePassword123", required = true, format = "password")
    String password,
    
    @Schema(description = "Currency code (ISO 4217)", example = "SGD", required = true)
    String currency
) {
}
