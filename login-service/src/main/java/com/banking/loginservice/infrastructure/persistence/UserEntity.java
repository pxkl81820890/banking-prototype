package com.banking.loginservice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * JPA Entity representing a user in the banking system.
 * Maps to the users table in the database.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "bank_code", nullable = false, length = 3)
    private String bankCode;

    @Column(name = "branch_code", nullable = false, length = 4)
    private String branchCode;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}
