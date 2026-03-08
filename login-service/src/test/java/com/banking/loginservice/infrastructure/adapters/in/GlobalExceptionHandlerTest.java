package com.banking.loginservice.infrastructure.adapters.in;

import com.banking.loginservice.domain.exception.CurrencyMismatchException;
import com.banking.loginservice.domain.exception.InvalidCredentialsException;
import com.banking.loginservice.domain.exception.InvalidEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Verifies that domain exceptions are correctly mapped to HTTP responses with banking error codes.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleInvalidEntityException_shouldReturn400WithErrorCode() {
        // Given
        InvalidEntityException exception = new InvalidEntityException("101", "1119");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidEntityException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ENTITY", response.getBody().errorCode());
        assertEquals("Invalid Entity Details", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleInvalidCredentialsException_shouldReturn401WithErrorCode() {
        // Given
        InvalidCredentialsException exception = new InvalidCredentialsException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentialsException(exception);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_CREDENTIALS", response.getBody().errorCode());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    @Test
    void handleCurrencyMismatchException_shouldReturn400WithErrorCode() {
        // Given
        CurrencyMismatchException exception = new CurrencyMismatchException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCurrencyMismatchException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CURRENCY_MISMATCH", response.getBody().errorCode());
        assertEquals("Currency mismatch", response.getBody().message());
    }

    @Test
    void handleGenericException_shouldReturn500WithErrorCode() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().errorCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
