package com.banking.loginservice.infrastructure.persistence;

import com.banking.loginservice.domain.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component to convert between UserEntity (JPA) and User (Domain Model).
 * Centralizes conversion logic for reusability across the infrastructure layer.
 */
@Component
public class DatabaseMapper {

    /**
     * Converts UserEntity (JPA) to User (Domain Model).
     * 
     * @param entity the JPA entity
     * @return the domain model
     * @throws IllegalArgumentException if entity is null
     */
    public User toDomainModel(UserEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("UserEntity cannot be null");
        }
        
        return new User(
                entity.getUserId(),
                entity.getPasswordHash(),
                entity.getBankCode(),
                entity.getBranchCode(),
                entity.getCurrency()
        );
    }

    /**
     * Converts User (Domain Model) to UserEntity (JPA).
     * 
     * @param user the domain model
     * @return the JPA entity
     * @throws IllegalArgumentException if user is null
     */
    public UserEntity toEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        return new UserEntity(
                user.userId(),
                user.passwordHash(),
                user.bankCode(),
                user.branchCode(),
                user.currency()
        );
    }
}
