package com.banking.loginservice.infrastructure.adapters.in;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Standard error response structure for banking API.
 * Java Record as per tech-stack.md requirements.
 */
@Schema(description = "Standard error response")
public record ErrorResponse(
    @Schema(description = "Error code identifier", example = "INVALID_CREDENTIALS")
    String errorCode,
    
    @Schema(description = "Human-readable error message", example = "Invalid username or password")
    String message,
    
    @Schema(description = "Timestamp when the error occurred", example = "2024-01-15T10:30:00")
    LocalDateTime timestamp
) {
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, LocalDateTime.now());
    }
}
