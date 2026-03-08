package com.banking.loginservice.domain.exception;

/**
 * Domain exception thrown when the provided currency does not match the user's currency.
 */
public class CurrencyMismatchException extends RuntimeException {
    
    public CurrencyMismatchException() {
        super("Currency mismatch");
    }
}
