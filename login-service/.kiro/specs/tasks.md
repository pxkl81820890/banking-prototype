# Implementation Tasks: Banking Login Microservice

## Task Status Legend
- `[ ]` Not Started
- `[~]` Queued
- `[-]` In Progress
- `[x]` Completed

---

## Phase 1: Project Setup & Configuration

### Task 1.1: Initialize Maven Project
- [x] Create Spring Boot 3.4.x project with Java 21
- [x] Add required dependencies:
  - spring-boot-starter-web
  - spring-boot-starter-security
  - spring-boot-starter-data-jpa
  - spring-boot-starter-validation
  - h2database (runtime)
  - springdoc-openapi-starter-webmvc-ui
  - lombok
  - spring-boot-starter-test

### Task 1.2: Configure Application Properties
- [x] Create application.yml with:
  - H2 database configuration (in-memory)
  - Server port 8080
  - JPA/Hibernate settings (ddl-auto: create-drop)
  - H2 console enabled
  - SQL initialization mode
  - Authentication service URL
  - Logging levels

### Task 1.3: Create Hexagonal Package Structure
- [x] Create package structure:
  - `domain.model` - Domain entities and value objects
  - `domain.ports` - Inbound and outbound port interfaces
  - `domain.service` - Business logic implementation
  - `infrastructure.adapters.in` - REST controllers and DTOs
  - `infrastructure.adapters.out` - External service adapters
  - `infrastructure.persistence` - JPA entities and repositories
  - `infrastructure.config` - Spring configuration classes

---

## Phase 2: Domain Layer Implementation

### Task 2.1: Create Domain Models (Java Records)
- [x] Create `User` record with:
  - userId, passwordHash, bankCode, branchCode, currency
  - Validation in compact constructor
- [x] Create `LoginResult` record with:
  - Success and failure factory methods
  - All response fields
- [x] Create `LoginAttempt` record with:
  - Timestamp, status, IP address, failure reason
  - LoginStatus enum (SUCCESS, FAILURE_INVALID_CREDENTIALS, etc.)
- [x] Create `TokenPayload` record with:
  - userId, bankCode, branchCode, currency, roles

### Task 2.2: Create Domain Enums
- [x] Create `Currency` enum:
  - SGD, USD, EUR
  - Display names

### Task 2.3: Define Domain Ports (Interfaces)
- [x] Create `LoginUseCase` (inbound port):
  - login() method signature
- [x] Create `UserOutputPort` (outbound port):
  - findByBankCodeAndBranchCodeAndUsername()
  - existsByBankCodeAndBranchCode()
- [x] Create `TokenGenerationPort` (outbound port):
  - generateToken() method
- [x] Create `AuditLogPort` (outbound port):
  - logLoginAttempt() method

### Task 2.4: Implement Domain Service
- [x] Create `LoginDomainService`:
  - Implement LoginUseCase interface
  - Use constructor injection (@RequiredArgsConstructor)
  - Implement login flow:
    1. Validate bank/branch existence
    2. Find user by composite key
    3. Verify password using BCrypt
    4. Validate currency
    5. Generate JWT token
    6. Log audit trail
    7. Return LoginResult
  - Handle all error scenarios
  - Use SGT timezone (Asia/Singapore)

---

## Phase 3: Infrastructure Layer - Inbound Adapters

### Task 3.1: Create DTOs (Java Records)
- [x] Create `LoginRequest` record:
  - All fields with Bean Validation annotations
  - @NotBlank, @Size, @Pattern validations
- [x] Create `LoginResponse` record:
  - userId, token, username, bankCode, branchCode, currency

### Task 3.2: Implement REST Controller
- [x] Create `LoginController`:
  - POST /api/v1/auth/login endpoint
  - Use @Valid for request validation
  - Inject LoginUseCase via constructor
  - Map LoginResult to LoginResponse
  - Return appropriate HTTP status codes
  - Add Swagger annotations (@Operation, @Tag)

### Task 3.3: Implement Global Exception Handler
- [x] Create `GlobalExceptionHandler`:
  - Handle MethodArgumentNotValidException (validation errors)
  - Handle generic Exception (500 errors)
  - Return consistent ErrorResponse format
  - Include timestamp, status, error, message, path
  - Log errors appropriately
  - Never expose stack traces to clients

---

## Phase 4: Infrastructure Layer - Outbound Adapters

### Task 4.1: Create Database Persistence Layer
- [x] Create `UserEntity` (JPA entity):
  - Map to USERS table
  - Use Lombok @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor
  - Define columns with constraints
- [x] Create `UserRepository` (Spring Data JPA):
  - Extend JpaRepository
  - Add custom query methods
  - Add existence check query
- [x] Create `DatabaseUserAdapter`:
  - Implement UserOutputPort
  - Use UserRepository
  - Map UserEntity to User domain model
  - Implement findByBankCodeAndBranchCodeAndUsername()
  - Implement existsByBankCodeAndBranchCode()

### Task 4.2: Create Authentication Service Adapter
- [x] Create `AuthenticationServiceAdapter`:
  - Implement TokenGenerationPort
  - Use RestTemplate for HTTP calls
  - Call POST /api/v1/auth/generate-token
  - Map TokenPayload to request body
  - Extract token from response
  - Handle connection errors gracefully

### Task 4.3: Create Audit Log Adapter
- [x] Create `AuditLogAdapter`:
  - Implement AuditLogPort
  - Log to application logs (structured format)
  - Include all audit fields
  - Use INFO level for success, WARN for failures

---

## Phase 5: Configuration Classes

### Task 5.1: Create Security Configuration
- [x] Create `SecurityConfig`:
  - Define BCryptPasswordEncoder bean (strength 10)
  - Configure SecurityFilterChain
  - Permit /api/v1/auth/login without authentication
  - Permit H2 console and Swagger UI
  - Disable CSRF for REST API
  - Disable frame options for H2 console

### Task 5.2: Create REST Template Configuration
- [x] Create `RestTemplateConfig`:
  - Define RestTemplate bean
  - Configure timeouts (optional)

### Task 5.3: Create OpenAPI Configuration
- [x] Create `OpenApiConfig`:
  - Define OpenAPI bean
  - Set API title, description, version
  - Configure Swagger UI

---

## Phase 6: Database Setup

### Task 6.1: Create Database Schema
- [x] Create schema.sql (if needed):
  - Define USERS table
  - Add constraints and indexes
  - Composite unique constraint on (bankCode, branchCode, userId)

### Task 6.2: Create Test Data
- [x] Create data.sql:
  - Insert 6 test users
  - Use BCrypt hashed passwords (password123)
  - Different banks, branches, currencies
  - Match users in TEST-USERS.md

---

## Phase 7: Testing

### Task 7.1: Unit Tests - Domain Service
- [x] Create `LoginDomainServiceTest`:
  - Test successful login flow
  - Test invalid bank/branch code
  - Test user not found
  - Test invalid password
  - Test currency mismatch
  - Test token generation failure
  - Mock all dependencies (UserOutputPort, TokenGenerationPort, etc.)
  - Verify audit logging calls
  - Use AssertJ for assertions

### Task 7.2: Integration Tests - REST Controller
- [x] Create `LoginControllerTest`:
  - Test POST /api/v1/auth/login with valid credentials
  - Test with invalid credentials
  - Test with missing required fields
  - Test with invalid field formats
  - Verify HTTP status codes
  - Verify response body structure
  - Use @WebMvcTest or @SpringBootTest

### Task 7.3: Integration Tests - Database Adapter
- [ ] Create `DatabaseUserAdapterTest`:
  - Test findByBankCodeAndBranchCodeAndUsername()
  - Test existsByBankCodeAndBranchCode()
  - Test with non-existent users
  - Test with multiple users in same bank/branch
  - Use @DataJpaTest

### Task 7.4: Integration Tests - Authentication Service Adapter
- [ ] Create `AuthenticationServiceAdapterTest`:
  - Test successful token generation
  - Test with service unavailable
  - Test with invalid response
  - Use MockRestServiceServer or WireMock

### Task 7.5: Property-Based Tests
- [ ] Create `LoginServicePropertyTests`:
  - CP-1: Authentication Integrity
    - Generate random valid/invalid credentials
    - Verify authentication only succeeds with exact match
  - CP-2: Multi-Tenancy Isolation
    - Create users with same username in different banks
    - Verify isolation
  - CP-3: Password Confidentiality
    - Verify passwords never in logs/responses
  - CP-4: Token Generation Consistency
    - Verify token claims match user attributes
  - CP-5: Audit Completeness
    - Verify one audit log per login attempt
  - CP-6: Error Message Safety
    - Verify identical error messages for different failures
  - CP-7: Currency Preservation
    - Verify currency consistency
  - CP-8: BCrypt Verification Correctness
    - Test with known password/hash pairs

### Task 7.6: End-to-End Tests
- [ ] Create `LoginServiceE2ETest`:
  - Start all services (Login + Authentication)
  - Test complete login flow
  - Verify JWT token is valid
  - Test with Login MFE integration

---

## Phase 8: Documentation

### Task 8.1: Update OpenAPI Specification
- [x] Generate OpenAPI spec from annotations
- [x] Export to docs/api-spec.yaml
- [x] Verify all endpoints documented
- [x] Add request/response examples

### Task 8.2: Update README
- [x] Document service purpose
- [x] Add quick start guide
- [x] Document API endpoints
- [x] Add test user credentials
- [x] Document H2 console access
- [x] Add troubleshooting section

### Task 8.3: Create Test Users Documentation
- [x] Create TEST-USERS.md
- [x] Document all 6 test users
- [x] Include bank codes, branch codes, currencies
- [x] Add curl examples
- [x] Document integration with Channel Configurations Service

---

## Phase 9: Performance & Security

### Task 9.1: Performance Optimization
- [ ] Add database indexes:
  - Composite index on (bankCode, branchCode, userId)
  - Index on (bankCode, branchCode)
- [ ] Configure connection pooling
- [ ] Add response time logging
- [ ] Load test with 100 concurrent requests
- [ ] Verify < 500ms response time (95th percentile)

### Task 9.2: Security Hardening
- [x] Verify BCrypt strength = 10
- [x] Verify passwords never logged
- [x] Verify generic error messages
- [ ] Add rate limiting (optional)
- [ ] Add IP address logging
- [ ] Security audit of all endpoints

### Task 9.3: Audit Logging Enhancement
- [ ] Add structured logging (JSON format)
- [ ] Add correlation IDs
- [ ] Configure log retention (7 years for audit logs)
- [ ] Add log aggregation configuration (ELK stack)

---

## Phase 10: Deployment

### Task 10.1: Create Dockerfile
- [x] Create multi-stage Dockerfile
- [x] Use eclipse-temurin:21-jre-alpine
- [x] Expose port 8080
- [x] Configure health checks

### Task 10.2: Create Docker Compose
- [ ] Create docker-compose.yml:
  - Login Service
  - Authentication Service
  - PostgreSQL (for production)
  - Network configuration

### Task 10.3: Kubernetes Configuration
- [ ] Create Kubernetes manifests:
  - Deployment
  - Service
  - ConfigMap
  - Secret (for database credentials)
  - Ingress (optional)

---

## Phase 11: Monitoring & Observability

### Task 11.1: Add Spring Boot Actuator
- [ ] Add actuator dependency
- [ ] Configure health endpoints
- [ ] Add custom health indicators:
  - Database connectivity
  - Authentication Service connectivity
- [ ] Expose metrics endpoint

### Task 11.2: Add Metrics Collection
- [ ] Add Micrometer dependency
- [ ] Configure metrics:
  - Login success/failure rates
  - Response time percentiles
  - Authentication Service call latency
  - Database query performance
- [ ] Configure Prometheus export (optional)

### Task 11.3: Add Distributed Tracing
- [ ] Add Spring Cloud Sleuth (optional)
- [ ] Configure trace IDs
- [ ] Add correlation ID to logs
- [ ] Configure Zipkin export (optional)

---

## Phase 12: Production Readiness

### Task 12.1: Replace H2 with PostgreSQL
- [ ] Add PostgreSQL dependency
- [ ] Update application.yml for PostgreSQL
- [ ] Create Flyway migrations
- [ ] Test with PostgreSQL locally
- [ ] Update Docker Compose

### Task 12.2: Externalize Configuration
- [ ] Move sensitive config to environment variables
- [ ] Create application-dev.yml
- [ ] Create application-prod.yml
- [ ] Document all configuration properties

### Task 12.3: Add Graceful Shutdown
- [ ] Configure graceful shutdown timeout
- [ ] Handle in-flight requests
- [ ] Close database connections properly
- [ ] Close RestTemplate connections

---

## Phase 13: CI/CD Pipeline

### Task 13.1: Create Build Pipeline
- [ ] Create GitHub Actions / Jenkins pipeline
- [ ] Run unit tests
- [ ] Run integration tests
- [ ] Generate code coverage report
- [ ] Build Docker image
- [ ] Push to container registry

### Task 13.2: Create Deployment Pipeline
- [ ] Deploy to dev environment
- [ ] Run smoke tests
- [ ] Deploy to staging
- [ ] Run E2E tests
- [ ] Manual approval for production
- [ ] Deploy to production
- [ ] Run health checks

---

## Summary

Total Tasks: 60+
- Completed: 35
- In Progress: 0
- Remaining: 25+

Priority: Complete Phase 7 (Testing) and Phase 9 (Performance & Security) before production deployment.
