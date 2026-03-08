package com.banking.loginservice.infrastructure.adapters.out;

import com.banking.loginservice.domain.model.User;
import com.banking.loginservice.domain.ports.UserOutputPort;
import com.banking.loginservice.infrastructure.persistence.DatabaseMapper;
import com.banking.loginservice.infrastructure.persistence.UserEntity;
import com.banking.loginservice.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * PostgreSQL implementation of UserOutputPort using Spring Data JPA.
 * This adapter bridges the domain layer with the database infrastructure.
 */
@Component
@RequiredArgsConstructor
public class PostgresUserAdapter implements UserOutputPort {

    private final UserRepository userRepository;
    private final DatabaseMapper databaseMapper;

    @Override
    public Optional<User> findByBankCodeAndBranchCodeAndUsername(String bankCode, String branchCode, String username) {
        return userRepository.findByBankCodeAndBranchCodeAndUserId(bankCode, branchCode, username)
                .map(databaseMapper::toDomainModel);
       // return Optional.of(new User("userID", "passwordHashed", "101", "1119", "SGD"));
    }
}
