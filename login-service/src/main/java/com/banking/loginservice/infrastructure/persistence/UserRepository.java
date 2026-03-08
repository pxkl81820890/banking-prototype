package com.banking.loginservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for UserEntity.
 * Provides database access methods for user data.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    
    /**
     * Finds a user by the combination of bank code, branch code, and username.
     * This supports multi-entity authentication where users are uniquely identified
     * by bank, branch, and username.
     * 
     * @param bankCode the 3-digit bank code
     * @param branchCode the 4-digit branch code
     * @param username the username
     * @return Optional containing the UserEntity if found, empty otherwise
     */
    Optional<UserEntity> findByBankCodeAndBranchCodeAndUserId(String bankCode, String branchCode, String username);
}
