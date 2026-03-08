package com.banking.loginservice.infrastructure.adapters.in;

import com.banking.loginservice.domain.exception.CurrencyMismatchException;
import com.banking.loginservice.domain.exception.InvalidCredentialsException;
import com.banking.loginservice.domain.exception.InvalidEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the login service.
 * Maps domain exceptions to HTTP responses with banking-specific error codes.
 * Implements RE-02 requirement for error handling and security logging.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles InvalidEntityException when bankCode or branchCode does not exist.
     * As per RE-02: Returns 400 Bad Request with "Invalid Entity Details" message
     * and logs as "Potential Unauthorized Entity Access".
     */
    @ExceptionHandler(InvalidEntityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEntityException(InvalidEntityException ex) {
        log.warn("Potential Unauthorized Entity Access - bankCode: {}, branchCode: {}", 
                 ex.getBankCode(), ex.getBranchCode());
        
        ErrorResponse errorResponse = ErrorResponse.of("INVALID_ENTITY", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles InvalidCredentialsException when password validation fails.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        log.warn("Invalid credentials provided during login attempt");
        
        ErrorResponse errorResponse = ErrorResponse.of("INVALID_CREDENTIALS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles CurrencyMismatchException when provided currency doesn't match user's currency.
     */
    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatchException(CurrencyMismatchException ex) {
        log.warn("Currency mismatch during login attempt");
        
        ErrorResponse errorResponse = ErrorResponse.of("CURRENCY_MISMATCH", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Generic exception handler for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
