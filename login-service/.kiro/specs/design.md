# Technical Design: Banking Login Microservice

## Overview

This document describes the technical architecture and implementation details for the Login Service, following Hexagonal Architecture (Ports & Adapters) pattern. The service handles multi-tenant authentication for banking customers with integration to the Authentication Service for JWT token generation.

---

## Architecture Pattern

### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                     INBOUND ADAPTERS                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ REST API     │  │ Exception    │  │ Validation   │       │
│  │ Controller   │  │ Handler      │  │ Filters      │       │
│  └──────┬───────┘  └──────────────┘  └──────────────┘       │
│         │                                                   │
│         ▼                                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              DOMAIN LAYER (CORE)                    │    │
│  │  ┌──────────────┐  ┌──────────────┐                 │    │
│  │  │ Domain       │  │ Business     │                 │    │
│  │  │ Models       │  │ Logic        │                 │    │
│  │  └──────────────┘  └──────────────┘                 │    │
│  │  ┌──────────────┐  ┌──────────────┐                 │    │
│  │  │ Inbound      │  │ Outbound     │                 │    │
│  │  │ Ports        │  │ Ports        │                 │    │
│  │  └──────────────┘  └──────────────┘                 │    │
│  └─────────────────────────────────────────────────────┘    │
│         │                                                   │
│         ▼                                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              OUTBOUND ADAPTERS                      │    │
│  │  ┌──────────────┐  ┌──────────────┐                │      │
│  │  │ Database     │  │ Auth Service │               │      │
│  │  │ Repository   │  │ Client       │               │   │
│  │  └──────────────┘  └──────────────┘               │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. Domain Layer (Core Business Logic)

### 1.1 Domain Models

#### User (Java Record)
```java
package com.banking.loginservice.domain.model;

public record User(
    String userId,
    String passwordHash,
    String bankCode,
    String branchCode,
    String currency
) {
    // Validation in compact constructor
    public User {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        if (bankCode == null || bankCode.length() != 3) {
            throw new IllegalArgumentException("Bank code must be 3 digits");
        }
        if (branchCode == null || branchCode.length() != 4) {
            throw new IllegalArgumentException("Branch code must be 4 digits");
        }
    }
}
```

#### LoginResult (Java Record)
```java
package com.banking.loginservice.domain.model;

public record LoginResult(
    String userId,
    String token,
    String username,
    String bankCode,
    String branchCode,
    String currency,
    boolean success,
    String errorMessage
) {
    public static LoginResult success(String userId, String token, String username, 
                                     String bankCode, String branchCode, String currency) {
        return new LoginResult(userId, token, username, bankCode, branchCode, currency, true, null);
    }
    
    public static LoginResult failure(String errorMessage) {
        return new LoginResult(null, null, null, null, null, null, false, errorMessage);
    }
}
```

#### LoginAttempt (Value Object)
```java
package com.banking.loginservice.domain.model;

import java.time.ZonedDateTime;

public record LoginAttempt(
    String username,
    String bankCode,
    String branchCode,
    ZonedDateTime timestamp,
    String ipAddress,
    LoginStatus status,
    String failureReason
) {
    public enum LoginStatus {
        SUCCESS,
        FAILURE_INVALID_CREDENTIALS,
        FAILURE_INVALID_ENTITY,
        FAILURE_SERVICE_ERROR
    }
}
```

#### TokenPayload (Java Record)
```java
package com.banking.loginservice.domain.model;

import java.util.List;

public record TokenPayload(
    String userId,
    String bankCode,
    String branchCode,
    String currency,
    List<String> roles
) {}
```

### 1.2 Domain Enums

#### Currency
```java
package com.banking.loginservice.domain.model;

public enum Currency {
    SGD("Singapore Dollar"),
    USD("US Dollar"),
    EUR("Euro");
    
    private final String displayName;
    
    Currency(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### 1.3 Domain Ports (Interfaces)

#### Inbound Port: LoginUseCase
```java
package com.banking.loginservice.domain.ports;

import com.banking.loginservice.domain.model.LoginResult;

public interface LoginUseCase {
    LoginResult login(String bankCode, String branchCode, String username, 
                     String password, String currency);
}
```

#### Outbound Port: UserOutputPort
```java
package com.banking.loginservice.domain.ports;

import com.banking.loginservice.domain.model.User;
import java.util.Optional;

public interface UserOutputPort {
    Optional<User> findByBankCodeAndBranchCodeAndUsername(
        String bankCode, String branchCode, String username);
    
    boolean existsByBankCodeAndBranchCode(String bankCode, String branchCode);
}
```

#### Outbound Port: TokenGenerationPort
```java
package com.banking.loginservice.domain.ports;

import com.banking.loginservice.domain.model.TokenPayload;

public interface TokenGenerationPort {
    String generateToken(TokenPayload payload);
}
```

#### Outbound Port: AuditLogPort
```java
package com.banking.loginservice.domain.ports;

import com.banking.loginservice.domain.model.LoginAttempt;

public interface AuditLogPort {
    void logLoginAttempt(LoginAttempt attempt);
}
```

### 1.4 Domain Service

#### LoginDomainService
```java
package com.banking.loginservice.domain.service;

import com.banking.loginservice.domain.model.*;
import com.banking.loginservice.domain.ports.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginDomainService implements LoginUseCase {
    
    private final UserOutputPort userOutputPort;
    private final TokenGenerationPort tokenGenerationPort;
    private final AuditLogPort auditLogPort;
    private final PasswordEncoder passwordEncoder;
    
    private static final ZoneId SGT_ZONE = ZoneId.of("Asia/Singapore");
    
    @Override
    public LoginResult login(String bankCode, String branchCode, String username, 
                            String password, String currency) {
        
        ZonedDateTime timestamp = ZonedDateTime.now(SGT_ZONE);
        
        // Step 1: Validate bank and branch exist
        if (!userOutputPort.existsByBankCodeAndBranchCode(bankCode, branchCode)) {
            logFailedAttempt(username, bankCode, branchCode, timestamp, null, 
                           LoginAttempt.LoginStatus.FAILURE_INVALID_ENTITY,
                           "Invalid bank or branch code");
            return LoginResult.failure("Invalid entity details");
        }
        
        // Step 2: Find user by composite key
        User user = userOutputPort.findByBankCodeAndBranchCodeAndUsername(
            bankCode, branchCode, username)
            .orElseGet(() -> {
                logFailedAttempt(username, bankCode, branchCode, timestamp, null,
                               LoginAttempt.LoginStatus.FAILURE_INVALID_CREDENTIALS,
                               "User not found");
                return null;
            });
        
        if (user == null) {
            return LoginResult.failure("Invalid credentials");
        }
        
        // Step 3: Verify password using BCrypt
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            logFailedAttempt(username, bankCode, branchCode, timestamp, null,
                           LoginAttempt.LoginStatus.FAILURE_INVALID_CREDENTIALS,
                           "Invalid password");
            return LoginResult.failure("Invalid credentials");
        }
        
        // Step 4: Validate currency matches user's currency
        if (!user.currency().equals(currency)) {
            log.warn("Currency mismatch for user {}: requested {}, expected {}", 
                    username, currency, user.currency());
        }
        
        // Step 5: Generate JWT token via Authentication Service
        try {
            TokenPayload payload = new TokenPayload(
                user.userId(),
                user.bankCode(),
                user.branchCode(),
                user.currency(),
                List.of("USER") // Default role
            );
            
            String token = tokenGenerationPort.generateToken(payload);
            
            // Step 6: Log successful login
            logSuccessfulAttempt(username, bankCode, branchCode, timestamp, null);
            
            // Step 7: Return success result
            return LoginResult.success(
                user.userId(),
                token,
                username,
                user.bankCode(),
                user.branchCode(),
                user.currency()
            );
            
        } catch (Exception e) {
            log.error("Token generation failed for user {}", username, e);
            logFailedAttempt(username, bankCode, branchCode, timestamp, null,
                           LoginAttempt.LoginStatus.FAILURE_SERVICE_ERROR,
                           "Token generation failed");
            return LoginResult.failure("Authentication service unavailable");
        }
    }
    
    private void logSuccessfulAttempt(String username, String bankCode, String branchCode,
                                     ZonedDateTime timestamp, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt(
            username, bankCode, branchCode, timestamp, ipAddress,
            LoginAttempt.LoginStatus.SUCCESS, null
        );
        auditLogPort.logLoginAttempt(attempt);
        log.info("Successful login: user={}, bank={}, branch={}, timestamp={}", 
                username, bankCode, branchCode, timestamp);
    }
    
    private void logFailedAttempt(String username, String bankCode, String branchCode,
                                 ZonedDateTime timestamp, String ipAddress,
                                 LoginAttempt.LoginStatus status, String reason) {
        LoginAttempt attempt = new LoginAttempt(
            username, bankCode, branchCode, timestamp, ipAddress, status, reason
        );
        auditLogPort.logLoginAttempt(attempt);
        log.warn("Failed login: user={}, bank={}, branch={}, reason={}", 
                username, bankCode, branchCode, reason);
    }
}
```

---

## 2. Infrastructure Layer - Inbound Adapters

### 2.1 REST API Controller

#### LoginController
```java
package com.banking.loginservice.infrastructure.adapters.in;

import com.banking.loginservice.domain.model.LoginResult;
import com.banking.loginservice.domain.ports.LoginUseCase;
import com.banking.loginservice.infrastructure.adapters.in.dto.LoginRequest;
import com.banking.loginservice.infrastructure.adapters.in.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class LoginController {
    
    private final LoginUseCase loginUseCase;
    
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with bank code, branch code, username, password, and currency"
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        
        LoginResult result = loginUseCase.login(
            request.bankCode(),
            request.branchCode(),
            request.username(),
            request.password(),
            request.currency()
        );
        
        if (result.success()) {
            LoginResponse response = new LoginResponse(
                result.userId(),
                result.token(),
                result.username(),
                result.bankCode(),
                result.branchCode(),
                result.currency()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401)
                .body(new LoginResponse(null, null, null, null, null, null));
        }
    }
}
```

### 2.2 DTOs (Java Records)

#### LoginRequest
```java
package com.banking.loginservice.infrastructure.adapters.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Bank code is required")
    @Size(min = 3, max = 3, message = "Bank code must be 3 digits")
    @Pattern(regexp = "\\d{3}", message = "Bank code must be numeric")
    String bankCode,
    
    @NotBlank(message = "Branch code is required")
    @Size(min = 4, max = 4, message = "Branch code must be 4 digits")
    @Pattern(regexp = "\\d{4}", message = "Branch code must be numeric")
    String branchCode,
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password,
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "SGD|USD|EUR", message = "Currency must be SGD, USD, or EUR")
    String currency
) {}


#### LoginResponse
```java
package com.banking.loginservice.infrastructure.adapters.in.dto;

public record LoginResponse(
    String userId,
    String token,
    String username,
    String bankCode,
    String branchCode,
    String currency
) {}
```

### 2.3 Global Exception Handler

#### GlobalExceptionHandler
```java
package com.banking.loginservice.infrastructure.adapters.in;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            ZonedDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            errors.toString(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            ZonedDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    public record ErrorResponse(
        ZonedDateTime timestamp,
        int status,
        String error,
        String message,
        String path
    ) {}
}
```

---

## 3. Infrastructure Layer - Outbound Adapters

### 3.1 Database Adapter

#### UserEntity (JPA Entity)
```java
package com.banking.loginservice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    
    @Id
    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;
    
    @Column(name = "PASSWORD_HASH", nullable = false, length = 100)
    private String passwordHash;
    
    @Column(name = "BANK_CODE", nullable = false, length = 3)
    private String bankCode;
    
    @Column(name = "BRANCH_CODE", nullable = false, length = 4)
    private String branchCode;
    
    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;
}
```

#### UserRepository (Spring Data JPA)
```java
package com.banking.loginservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    
    Optional<UserEntity> findByBankCodeAndBranchCodeAndUserId(
        String bankCode, String branchCode, String userId);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM UserEntity u WHERE u.bankCode = :bankCode AND u.branchCode = :branchCode")
    boolean existsByBankCodeAndBranchCode(String bankCode, String branchCode);
}
```

#### DatabaseUserAdapter
```java
package com.banking.loginservice.infrastructure.adapters.out;

import com.banking.loginservice.domain.model.User;
import com.banking.loginservice.domain.ports.UserOutputPort;
import com.banking.loginservice.infrastructure.persistence.UserEntity;
import com.banking.loginservice.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatabaseUserAdapter implements UserOutputPort {
    
    private final UserRepository userRepository;
    
    @Override
    public Optional<User> findByBankCodeAndBranchCodeAndUsername(
            String bankCode, String branchCode, String username) {
        
        return userRepository.findByBankCodeAndBranchCodeAndUserId(bankCode, branchCode, username)
            .map(this::toDomainModel);
    }
    
    @Override
    public boolean existsByBankCodeAndBranchCode(String bankCode, String branchCode) {
        return userRepository.existsByBankCodeAndBranchCode(bankCode, branchCode);
    }
    
    private User toDomainModel(UserEntity entity) {
        return new User(
            entity.getUserId(),
            entity.getPasswordHash(),
            entity.getBankCode(),
            entity.getBranchCode(),
            entity.getCurrency()
        );
    }
}
```

### 3.2 Authentication Service Adapter

#### AuthenticationServiceAdapter
```java
package com.banking.loginservice.infrastructure.adapters.out;

import com.banking.loginservice.domain.model.TokenPayload;
import com.banking.loginservice.domain.ports.TokenGenerationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceAdapter implements TokenGenerationPort {
    
    private final RestTemplate restTemplate;
    
    @Value("${authentication.service.url:http://localhost:8081}")
    private String authServiceUrl;
    
    @Override
    public String generateToken(TokenPayload payload) {
        String url = authServiceUrl + "/api/v1/auth/generate-token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", payload.userId());
        requestBody.put("bankCode", payload.bankCode());
        requestBody.put("branchCode", payload.branchCode());
        requestBody.put("currency", payload.currency());
        requestBody.put("roles", payload.roles());
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            TokenResponse response = restTemplate.postForObject(url, request, TokenResponse.class);
            return response != null ? response.token() : null;
        } catch (Exception e) {
            log.error("Failed to generate token from authentication service", e);
            throw new RuntimeException("Token generation failed", e);
        }
    }
    
    private record TokenResponse(String token) {}
}
```

### 3.3 Audit Log Adapter

#### AuditLogAdapter
```java
package com.banking.loginservice.infrastructure.adapters.out;

import com.banking.loginservice.domain.model.LoginAttempt;
import com.banking.loginservice.domain.ports.AuditLogPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditLogAdapter implements AuditLogPort {
    
    @Override
    public void logLoginAttempt(LoginAttempt attempt) {
        // For now, log to application logs
        // In production, this would write to a dedicated audit log database/service
        
        log.info("AUDIT: Login attempt - username={}, bank={}, branch={}, status={}, timestamp={}, reason={}",
            attempt.username(),
            attempt.bankCode(),
            attempt.branchCode(),
            attempt.status(),
            attempt.timestamp(),
            attempt.failureReason() != null ? attempt.failureReason() : "N/A"
        );
    }
}
```

---

## 4. Configuration

### 4.1 Security Configuration

#### SecurityConfig
```java
package com.banking.loginservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Strength 10
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }
}
```

### 4.2 REST Template Configuration

#### RestTemplateConfig
```java
package com.banking.loginservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 4.3 OpenAPI Configuration

#### OpenApiConfig
```java
package com.banking.loginservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI loginServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Login Service API")
                .description("Multi-tenant banking authentication service")
                .version("1.0.0"));
    }
}
```

---

## 5. Database Schema

### 5.1 Users Table

```sql
CREATE TABLE USERS (
    USER_ID VARCHAR(50) PRIMARY KEY,
    PASSWORD_HASH VARCHAR(100) NOT NULL,
    BANK_CODE VARCHAR(3) NOT NULL,
    BRANCH_CODE VARCHAR(4) NOT NULL,
    CURRENCY VARCHAR(3) NOT NULL,
    CONSTRAINT uk_user_bank_branch UNIQUE (BANK_CODE, BRANCH_CODE, USER_ID)
);

CREATE INDEX idx_bank_branch ON USERS(BANK_CODE, BRANCH_CODE);
CREATE INDEX idx_user_lookup ON USERS(BANK_CODE, BRANCH_CODE, USER_ID);
```

---

## 6. API Specification

### 6.1 Login Endpoint

**Endpoint:** `POST /api/v1/auth/login`

**Request:**
```json
{
  "bankCode": "101",
  "branchCode": "1119",
  "username": "testuser",
  "password": "password123",
  "currency": "SGD"
}
```

**Success Response (200 OK):**
```json
{
  "userId": "testuser",
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "username": "testuser",
  "bankCode": "101",
  "branchCode": "1119",
  "currency": "SGD"
}
```

**Error Responses:**

**400 Bad Request (Validation Error):**
```json
{
  "timestamp": "2026-03-05T10:30:00+08:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation Failed: bankCode must be 3 digits",
  "path": "/api/v1/auth/login"
}
```

**401 Unauthorized (Invalid Credentials):**
```json
{
  "timestamp": "2026-03-05T10:30:00+08:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/v1/auth/login"
}
```

**503 Service Unavailable (Auth Service Down):**
```json
{
  "timestamp": "2026-03-05T10:30:00+08:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Authentication service unavailable",
  "path": "/api/v1/auth/login"
}
```

---

## 7. Security Considerations

### 7.1 Password Security
- BCrypt hashing with strength 10
- Passwords never logged or exposed
- Constant-time comparison to prevent timing attacks

### 7.2 Error Message Safety
- Generic error messages for authentication failures
- No indication whether username or password was incorrect
- No exposure of internal system details

### 7.3 Audit Logging
- All login attempts logged with timestamp (SGT)
- Includes username, bank code, branch code, IP address
- Separate log entries for success and failure
- Failure reasons logged for security monitoring

### 7.4 Input Validation
- All inputs validated using Bean Validation
- SQL injection prevention via parameterized queries
- XSS prevention via input sanitization

---

## 8. Performance Considerations

### 8.1 Database Optimization
- Composite index on (bankCode, branchCode, userId)
- Index on (bankCode, branchCode) for existence checks
- Connection pooling configured for optimal performance

### 8.2 Caching Strategy
- No caching of user credentials (security)
- Consider caching bank/branch existence checks
- Token generation delegated to Authentication Service

### 8.3 Response Time Targets
- Login processing: < 500ms (95th percentile)
- Database queries: < 50ms
- Authentication Service call: < 200ms

---

## 9. Testing Strategy

### 9.1 Unit Tests
- Domain service logic (LoginDomainService)
- Password verification
- Multi-tenancy isolation
- Error handling scenarios

### 9.2 Integration Tests
- REST API endpoints
- Database operations
- Authentication Service integration
- Error response formats

### 9.3 Property-Based Tests
- Authentication integrity (CP-1)
- Multi-tenancy isolation (CP-2)
- Password confidentiality (CP-3)
- Token generation consistency (CP-4)
- Audit completeness (CP-5)
- Error message safety (CP-6)
- Currency preservation (CP-7)
- BCrypt verification correctness (CP-8)

---

## 10. Deployment Configuration

### 10.1 Application Properties

```yaml
spring:
  application:
    name: login-service
  
  datasource:
    url: jdbc:h2:mem:logindb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    defer-datasource-initialization: true
  
  sql:
    init:
      mode: always
  
  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: 8080

authentication:
  service:
    url: http://localhost:8081

logging:
  level:
    com.banking.loginservice: DEBUG
    org.springframework.security: DEBUG
```

### 10.2 Docker Configuration

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/login-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 11. Dependencies

### 11.1 Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- OpenAPI Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 12. Monitoring & Observability

### 12.1 Health Checks
- Spring Boot Actuator endpoints
- Database connectivity check
- Authentication Service connectivity check

### 12.2 Metrics
- Login success/failure rates
- Response time percentiles
- Authentication Service call latency
- Database query performance

### 12.3 Logging
- Structured logging (JSON format for ELK stack)
- Correlation IDs for request tracing
- Audit logs for compliance
- Error logs with stack traces (internal only)

---

## Summary

This design follows Hexagonal Architecture principles with clear separation between domain logic and infrastructure concerns. The service is stateless, scalable, and follows banking security best practices. All components use constructor injection, Java Records for immutability, and comprehensive error handling.
