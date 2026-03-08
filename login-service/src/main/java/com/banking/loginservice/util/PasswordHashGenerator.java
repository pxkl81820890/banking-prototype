package com.banking.loginservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for testing.
 * Run this class to generate a hash for "password123".
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("\nVerification test:");
        System.out.println("Matches: " + encoder.matches(password, hash));
        
        // Test with the hash from data.sql
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        System.out.println("\nTesting existing hash from data.sql:");
        System.out.println("Matches: " + encoder.matches(password, existingHash));
    }
}
