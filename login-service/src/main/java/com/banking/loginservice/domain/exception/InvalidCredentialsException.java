package com.banking.loginservice.domain.exception;

/**
 * Domain exception thrown when password validation fails.
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
