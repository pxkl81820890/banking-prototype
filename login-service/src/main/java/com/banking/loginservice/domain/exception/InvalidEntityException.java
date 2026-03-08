package com.banking.loginservice.domain.exception;

/**
 * Domain exception thrown when bankCode or branchCode does not exist in the system.
 * Maps to RE-02 requirement for invalid entity details.
 */
public class InvalidEntityException extends RuntimeException {
    
    private final String bankCode;
    private final String branchCode;
    
    public InvalidEntityException(String bankCode, String branchCode) {
        super("Invalid Entity Details");
        this.bankCode = bankCode;
        this.branchCode = branchCode;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public String getBranchCode() {
        return branchCode;
    }
}
