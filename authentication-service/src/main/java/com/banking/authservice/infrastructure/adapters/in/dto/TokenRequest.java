package com.banking.authservice.infrastructure.adapters.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for token generation endpoint.
 */
@Schema(description = "Token generation request with multi-entity banking context")
public record TokenRequest(
    @NotBlank(message = "userId is required")
    @Schema(description = "Unique user identifier (UUID)", example = "user-uuid-123", required = true)
    String userId,
    
    @NotBlank(message = "bankCode is required")
    @Schema(description = "3-digit bank code", example = "101", required = true)
    String bankCode,
    
    @NotBlank(message = "branchCode is required")
    @Schema(description = "4-digit branch code", example = "1119", required = true)
    String branchCode,
    
    @NotBlank(message = "currency is required")
    @Schema(description = "Currency code (ISO 4217)", example = "SGD", required = true)
    String currency
) {}
