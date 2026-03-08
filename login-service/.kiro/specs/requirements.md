# Login Service - Requirements Specification

## Overview

The Login Service is a critical microservice in the banking platform that handles user authentication for multi-tenant banking operations. It validates user credentials against bank-specific and branch-specific user records, integrates with the Authentication Service for JWT token generation, and maintains audit trails for all login attempts.

### Purpose
Provide secure, multi-tenant user authentication for banking customers across different banks, branches, and currencies while maintaining compliance with banking security standards.

### Scope
- User credential validation (username/password)
- Multi-tenant authentication (bank code, branch code)
- Currency-specific session management
- Integration with Authentication Service for JWT token generation
- Audit logging for security compliance
- Error handling and security event monitoring

### Out of Scope
- Password reset functionality
- User registration/onboarding
- Multi-factor authentication (MFA)
- Session management (handled by Authentication Service)
- Authorization/permissions (handled by Channel Configurations Service)

---

## Functional Requirements

### REQ-1: Multi-Tenant User Authentication

#### User Story (US-1)
**As a** banking customer  
**I want to** log in using my bank code, branch code, username, password, and preferred currency  
**So that** I can access my banking services with the correct context

**Priority:** High  
**Estimation:** 5 story points

#### Acceptance Criteria

**AC-1.1: Successful Login Flow**

**Given** a valid user exists in the system with:
- Bank Code: "101"
- Branch Code: "1119"
- Username: "testuser"
- Password: "password123" (BCrypt hashed)
- Currency: "SGD"

**When** the user submits a login request with correct credentials

**Then** the system shall:
1. Validate the bank code and branch code exist
2. Locate the user record matching bankCode + branchCode + username
3. Verify the password using BCrypt comparison
4. Call Authentication Service to generate JWT with claims:
   - userId
   - bankCode
   - branchCode
   - currency
   - roles
5. Return HTTP 200 with response containing:
   - userId
   - token (JWT)
   - username
   - bankCode
   - branchCode
   - currency
6. Log successful login event with timestamp (SGT/UTC+8)

**AC-1.2: Multi-Currency Support**

**Given** users can have different preferred currencies

**When** a user logs in with currency "SGD", "USD"

**Then** the system shall:
1. Accept the currency value
2. Include currency in JWT token claims
3. Return currency in login response
4. Support all configured currencies (SGD, USD)

---

### REQ-2: Invalid Credentials Handling

#### User Story (US-2)
**As a** banking customer  
**I want to** receive clear feedback when my login credentials are incorrect  
**So that** I can correct my input and successfully authenticate

**Priority:** High  
**Estimation:** 3 story points

#### Acceptance Criteria

**AC-2.1: Invalid Username**

**Given** a user submits login credentials with valid bank/branch codes

**When** the username does not exist for that bank/branch combination

**Then** the system shall:
1. Return HTTP 401 Unauthorized
2. Include generic error message: "Invalid credentials"
3. Log failed login attempt with username and IP address
4. NOT expose that username doesn't exist (security)

**AC-2.2: Invalid Password**

**Given** a user exists in the system

**When** the user submits an incorrect password

**Then** the system shall:
1. Return HTTP 401 Unauthorized
2. Include generic error message: "Invalid credentials"
3. Log failed login attempt with username and IP address
4. NOT expose that password was incorrect (security)

**AC-2.3: Error Message Safety**

**Given** any error occurs during login

**When** returning error responses

**Then** the system shall:
1. Use consistent error response format:
   ```json
   {
     "timestamp": "2026-03-05T10:30:00",
     "status": 400,
     "error": "Bad Request",
     "message": "Validation failed",
     "path": "/api/v1/auth/login"
   }
   ```
2. NEVER expose internal error details
3. NEVER expose stack traces
4. Use appropriate HTTP status codes

---

### REQ-3: Multi-Bank and Multi-Branch Support

#### User Story (US-3)
**As a** system administrator  
**I want to** support multiple banks and branches in a single service  
**So that** we can serve different banking entities from one platform

**Priority:** High  
**Estimation:** 3 story points

#### Acceptance Criteria

**AC-3.1: Invalid Bank/Branch Code**

**Given** a user submits login credentials

**When** the bank code or branch code does not exist in the system

**Then** the system shall:
1. Return HTTP 400 Bad Request
2. Include error message: "Invalid entity details"
3. Log the attempt as "Potential Unauthorized Entity Access"
4. NOT expose whether bank code or branch code was invalid (security)

**AC-3.2: Multi-Tenancy Isolation**

**Given** multiple banks and branches exist in the system

**When** users from different banks/branches attempt to login

**Then** the system shall:
1. Isolate users by bankCode + branchCode + username composite key
2. Prevent cross-bank/branch authentication
3. Ensure users with same username in different banks cannot access each other's accounts

---

### REQ-4: Security Audit Trail

#### User Story (US-4)
**As a** security officer  
**I want to** have all login attempts logged with timestamps and outcomes  
**So that** I can monitor for suspicious activity and maintain compliance

**Priority:** High  
**Estimation:** 2 story points

#### Acceptance Criteria

**AC-4.1: Audit Logging**

**Given** any login attempt occurs

**When** the attempt succeeds or fails

**Then** the system shall log:
1. Timestamp (in SGT/UTC+8)
2. Username
3. Bank Code
4. Branch Code
5. IP Address (if available)
6. Outcome (SUCCESS or FAILURE)
7. Failure reason (if applicable)

**AC-4.2: Audit Log Completeness**

**Given** login attempts are processed

**When** reviewing audit logs

**Then** the system shall:
1. Generate exactly one audit log entry per login attempt
2. Include all required fields
3. Use structured logging format
4. Retain logs for 7 years (compliance requirement)

---

### REQ-5: Input Validation

#### User Story (Implicit - System Requirement)
**As a** system  
**I want to** validate all user inputs  
**So that** invalid data is rejected before processing

**Priority:** High  
**Estimation:** 2 story points

#### Acceptance Criteria

**AC-5.1: Missing Required Fields**

**Given** a user submits a login request

**When** any required field is missing (bankCode, branchCode, username, password, currency)

**Then** the system shall:
1. Return HTTP 400 Bad Request
2. Include validation error message specifying which field is missing
3. NOT process the login attempt

**AC-5.2: Field Format Validation**

**Given** a user submits a login request

**When** fields do not match required formats

**Then** the system shall validate:
1. Bank Code: 3 digits, numeric
2. Branch Code: 4 digits, numeric
3. Username: 3-50 characters
4. Password: 8-100 characters
5. Currency: Must be SGD, USD, or EUR

---

### REQ-6: Authentication Service Integration

#### User Story (Implicit - System Requirement)
**As a** system  
**I want to** integrate with Authentication Service for token generation  
**So that** users receive valid JWT tokens after successful authentication

**Priority:** High  
**Estimation:** 3 story points

#### Acceptance Criteria

**AC-6.1: Token Generation**

**Given** user credentials are validated successfully

**When** calling the Authentication Service for token generation

**Then** the system shall:
1. Send user details (userId, bankCode, branchCode, currency, roles)
2. Receive JWT token from Authentication Service
3. Include the token in the login response
4. Handle Authentication Service failures gracefully (return HTTP 503)

**AC-6.2: Token Claims Consistency**

**Given** a JWT token is generated

**When** examining token claims

**Then** the token shall contain:
1. userId matching the authenticated user
2. bankCode matching the user's bank
3. branchCode matching the user's branch
4. currency matching the user's preferred currency
5. roles array with user permissions

---

### REQ-7: Password Security

#### User Story (Implicit - Security Requirement)
**As a** system  
**I want to** securely handle passwords  
**So that** user credentials are protected

**Priority:** Critical  
**Estimation:** 2 story points

#### Acceptance Criteria

**AC-7.1: Password Hashing**

**Given** passwords are stored in the database

**When** comparing user-provided passwords

**Then** the system shall:
1. Use BCrypt hashing algorithm
2. Use BCrypt strength of 10 (default)
3. NEVER log passwords (plain or hashed)
4. NEVER return passwords in responses

**AC-7.2: Password Confidentiality**

**Given** any system operation occurs

**When** generating logs, responses, or error messages

**Then** the system shall:
1. NEVER include passwords in logs
2. NEVER include passwords in API responses
3. NEVER include passwords in error messages
4. NEVER expose password hashes to clients

---
5. Return HTTP 200 with response containing:
   - userId
   - token (JWT)
   - username
   - bankCode
   - branchCode
   - currency
6. Log successful login event with timestamp (SGT/UTC+8)

---

### AC-2: Invalid Bank/Branch Code
**Given** a user submits login credentials

**When** the bank code or branch code does not exist in the system

**Then** the system shall:
1. Return HTTP 400 Bad Request
2. Include error message: "Invalid entity details"
3. Log the attempt as "Potential Unauthorized Entity Access"
4. NOT expose whether bank code or branch code was invalid (security)

---

### AC-3: Invalid Username
**Given** a user submits login credentials with valid bank/branch codes

**When** the username does not exist for that bank/branch combination

**Then** the system shall:
1. Return HTTP 401 Unauthorized
2. Include generic error message: "Invalid credentials"
3. Log failed login attempt with username and IP address
4. NOT expose that username doesn't exist (security)

---

### AC-4: Invalid Password
**Given** a user exists in the system

**When** the user submits an incorrect password

**Then** the system shall:
1. Return HTTP 401 Unauthorized
2. Include generic error message: "Invalid credentials"
3. Log failed login attempt with username and IP address
4. NOT expose that password was incorrect (security)

---

### AC-5: Missing Required Fields
**Given** a user submits a login request

**When** any required field is missing (bankCode, branchCode, username, password, currency)

**Then** the system shall:
1. Return HTTP 400 Bad Request
2. Include validation error message specifying which field is missing
3. NOT process the login attempt

---

### AC-6: Authentication Service Integration
**Given** user credentials are validated successfully

**When** calling the Authentication Service for token generation

**Then** the system shall:
1. Send user details (userId, bankCode, branchCode, currency, roles)
2. Receive JWT token from Authentication Service
3. Include the token in the login response
4. Handle Authentication Service failures gracefully (return HTTP 503)

---

### AC-7: Multi-Currency Support
**Given** users can have different preferred currencies

**When** a user logs in with currency "SGD", "USD"

**Then** the system shall:
1. Accept the currency value
2. Include currency in JWT token claims
3. Return currency in login response
4. Support all configured currencies (SGD, USD)

---

### AC-8: Audit Logging
**Given** any login attempt occurs

**When** the attempt succeeds or fails

**Then** the system shall log:
1. Timestamp (in SGT/UTC+8)
2. Username
3. Bank Code
4. Branch Code
5. IP Address (if available)
6. Outcome (SUCCESS or FAILURE)
7. Failure reason (if applicable)

---

### AC-9: Password Security
**Given** passwords are stored in the database

**When** comparing user-provided passwords

**Then** the system shall:
1. Use BCrypt hashing algorithm
2. Use BCrypt strength of 10 (default)
3. NEVER log passwords (plain or hashed)
4. NEVER return passwords in responses

---

### AC-10: Error Response Consistency
**Given** any error occurs during login

**When** returning error responses

**Then** the system shall:
1. Use consistent error response format:
   ```json
   {
     "timestamp": "2026-03-05T10:30:00",
     "status": 400,
     "error": "Bad Request",
     "message": "Validation failed",
     "path": "/api/v1/auth/login"
   }
   ```
2. NEVER expose internal error details
3. NEVER expose stack traces
4. Use appropriate HTTP status codes

---

## Correctness Properties

### CP-1: Authentication Integrity
**Property:** For any valid user U with credentials (bankCode, branchCode, username, password), the system shall authenticate U if and only if all credentials match exactly.

**Formal Definition:**
```
∀ user U, credentials C:
  authenticate(U, C) = true ⟺ 
    (C.bankCode = U.bankCode) ∧ 
    (C.branchCode = U.branchCode) ∧ 
    (C.username = U.username) ∧ 
    BCrypt.verify(C.password, U.passwordHash)
```

**Test Strategy:** Property-based testing with random valid and invalid credential combinations

---

### CP-2: Multi-Tenancy Isolation
**Property:** Users from different banks or branches shall never authenticate with each other's credentials, even if usernames and passwords match.

**Formal Definition:**
```
∀ user U1, user U2, credentials C:
  (U1.bankCode ≠ U2.bankCode ∨ U1.branchCode ≠ U2.branchCode) ⟹
    authenticate(U1, C) ∧ authenticate(U2, C) = false
```

**Test Strategy:** Create users with identical usernames/passwords in different banks/branches and verify isolation

---

### CP-3: Password Confidentiality
**Property:** Passwords shall never be exposed in logs, responses, or error messages in any form (plain text or hashed).

**Formal Definition:**
```
∀ operation O, output OUT:
  OUT = execute(O) ⟹ 
    ¬contains(OUT.logs, password) ∧ 
    ¬contains(OUT.response, password) ∧
    ¬contains(OUT.errors, password)
```

**Test Strategy:** Scan all logs, responses, and error messages for password patterns

---

### CP-4: Token Generation Consistency
**Property:** For a successful authentication, a JWT token shall always be generated with correct claims matching the authenticated user.

**Formal Definition:**
```
∀ user U, token T:
  authenticate(U) = success ⟹
    ∃ T: (T.userId = U.userId) ∧ 
         (T.bankCode = U.bankCode) ∧
         (T.branchCode = U.branchCode) ∧
         (T.currency = U.currency)
```

**Test Strategy:** Verify token claims match user attributes for all successful logins

---

### CP-5: Audit Completeness
**Property:** Every login attempt (successful or failed) shall generate exactly one audit log entry.

**Formal Definition:**
```
∀ login attempt A:
  execute(A) ⟹ ∃! log L: 
    (L.timestamp = A.timestamp) ∧
    (L.username = A.username) ∧
    (L.outcome ∈ {SUCCESS, FAILURE})
```

**Test Strategy:** Count audit log entries and verify one-to-one mapping with login attempts

---

### CP-6: Error Message Safety
**Property:** Error messages shall never reveal whether a username exists or whether the password was incorrect.

**Formal Definition:**
```
∀ invalid credentials C1, C2:
  (C1.username ∉ Users ∨ C1.password ≠ correct) ∧
  (C2.username ∉ Users ∨ C2.password ≠ correct) ⟹
    errorMessage(C1) = errorMessage(C2) = "Invalid credentials"
```

**Test Strategy:** Verify identical error messages for non-existent users vs. wrong passwords

---

### CP-7: Currency Preservation
**Property:** The currency specified during login shall be preserved in the JWT token and response without modification.

**Formal Definition:**
```
∀ login request R, response RESP, token T:
  R.currency ∈ {SGD, USD, EUR} ⟹
    (RESP.currency = R.currency) ∧ (T.claims.currency = R.currency)
```

**Test Strategy:** Verify currency value consistency across request, response, and token

---

### CP-8: BCrypt Verification Correctness
**Property:** BCrypt password verification shall return true if and only if the plain password matches the stored hash.

**Formal Definition:**
```
∀ password P, hash H:
  BCrypt.verify(P, H) = true ⟺ H = BCrypt.hash(P)
```

**Test Strategy:** Test with known password/hash pairs and verify verification results

---

## Non-Functional Requirements

### NFR-1: Performance
- Login request processing shall complete within 500ms (95th percentile)
- System shall support 100 concurrent login requests
- Database queries shall use indexed lookups on (bankCode, branchCode, username)

### NFR-2: Security
- All passwords shall be hashed using BCrypt with strength ≥ 10
- Passwords shall never be logged or exposed in any form
- Failed login attempts shall be logged for security monitoring
- Generic error messages shall not reveal system internals

### NFR-3: Availability
- Service shall have 99.9% uptime
- Graceful degradation when Authentication Service is unavailable
- Health check endpoint shall respond within 100ms

### NFR-4: Scalability
- Service shall be stateless to support horizontal scaling
- Database connection pooling shall be configured for optimal performance
- Support for multiple database replicas for read operations

### NFR-5: Maintainability
- Code shall follow Hexagonal Architecture pattern
- All business logic shall be in domain layer
- Infrastructure concerns shall be isolated in adapters
- Comprehensive unit and integration tests (>80% coverage)

### NFR-6: Compliance
- Audit logs shall be retained for 7 years
- Timestamps shall be in SGT (UTC+8) timezone
- All sensitive operations shall be logged
- GDPR compliance for user data handling

---

## Edge Cases & Error Scenarios

### Edge Case 1: Empty String Credentials
**Scenario:** User submits empty strings for required fields  
**Expected:** HTTP 400 with validation error

### Edge Case 2: SQL Injection Attempt
**Scenario:** User submits SQL injection patterns in username  
**Expected:** Parameterized queries prevent injection; treated as invalid username

### Edge Case 3: Very Long Input Strings
**Scenario:** User submits extremely long strings (>1000 chars)  
**Expected:** HTTP 400 with validation error (field length exceeded)

### Edge Case 4: Special Characters in Username
**Scenario:** User has special characters in username (e.g., "user+test@bank.com")  
**Expected:** System accepts and processes correctly

### Edge Case 5: Authentication Service Timeout
**Scenario:** Authentication Service doesn't respond within timeout  
**Expected:** HTTP 503 Service Unavailable with retry guidance

### Edge Case 6: Database Connection Failure
**Scenario:** Database is unavailable during login attempt  
**Expected:** HTTP 503 Service Unavailable; log critical error

### Edge Case 7: Concurrent Login Attempts
**Scenario:** Same user attempts login from multiple locations simultaneously  
**Expected:** All attempts processed independently; all succeed if credentials valid

### Edge Case 8: Case Sensitivity
**Scenario:** User submits username with different case (e.g., "TestUser" vs "testuser")  
**Expected:** Username comparison is case-sensitive; "TestUser" ≠ "testuser"

---

## Dependencies

### Internal Services
- **Authentication Service (Port 8081)**: JWT token generation
  - Endpoint: POST /api/v1/auth/generate-token
  - Required for: Token generation after successful authentication
  - Failure Impact: Login fails with HTTP 503

### External Dependencies
- **H2 Database (In-Memory)**: User credential storage
  - Used for: Development and testing
  - Production: Replace with PostgreSQL
  
### Configuration Dependencies
- BCrypt password encoder (strength: 10)
- Timezone: SGT (UTC+8)
- Supported currencies: SGD, USD, EUR

---

## Test Data

### Test Users (Development/Testing)
All test users use password: `password123`

| Username | Bank Code | Branch Code | Currency | Purpose |
|----------|-----------|-------------|----------|---------|
| testuser | 101 | 1119 | SGD | Default test user |
| 1119test1 | 101 | 1119 | SGD | Feature flag testing (ACL 1000) |
| 1119test2 | 101 | 1119 | USD | Feature flag testing (ACL 1001) |
| 1119test3 | 101 | 1119 | EUR | Feature flag testing (All ACLs) |
| adminuser | 102 | 2001 | SGD | Different bank testing |
| demouser | 103 | 3001 | USD | Different bank testing |

---

## Glossary

- **Bank Code**: 3-digit identifier for a banking institution (e.g., "101")
- **Branch Code**: 4-digit identifier for a bank branch (e.g., "1119")
- **Multi-Tenancy**: Supporting multiple banks/branches in single service instance
- **BCrypt**: Cryptographic hash function for password storage
- **JWT**: JSON Web Token for stateless authentication
- **SGT**: Singapore Time (UTC+8)
- **Audit Trail**: Chronological record of system activities for security/compliance


## Correctness Properties

These formal properties define the correctness criteria that must be validated through property-based testing.

### CP-1: Authentication Integrity (Related to REQ-1)

**Property:** For any valid user U with credentials (bankCode, branchCode, username, password), the system shall authenticate U if and only if all credentials match exactly.

**Formal Definition:**
```
∀ user U, credentials C:
  authenticate(U, C) = true ⟺ 
    (C.bankCode = U.bankCode) ∧ 
    (C.branchCode = U.branchCode) ∧ 
    (C.username = U.username) ∧ 
    BCrypt.verify(C.password, U.passwordHash)
```

**Test Strategy:** Property-based testing with random valid and invalid credential combinations

**Traceability:** REQ-1 (AC-1.1)

---

### CP-2: Multi-Tenancy Isolation (Related to REQ-3)

**Property:** Users from different banks or branches shall never authenticate with each other's credentials, even if usernames and passwords match.

**Formal Definition:**
```
∀ user U1, user U2, credentials C:
  (U1.bankCode ≠ U2.bankCode ∨ U1.branchCode ≠ U2.branchCode) ⟹
    authenticate(U1, C) ∧ authenticate(U2, C) = false
```

**Test Strategy:** Create users with identical usernames/passwords in different banks/branches and verify isolation

**Traceability:** REQ-3 (AC-3.2)

---

### CP-3: Password Confidentiality (Related to REQ-7)

**Property:** Passwords shall never be exposed in logs, responses, or error messages in any form (plain text or hashed).

**Formal Definition:**
```
∀ operation O, output OUT:
  OUT = execute(O) ⟹ 
    ¬contains(OUT.logs, password) ∧ 
    ¬contains(OUT.response, password) ∧
    ¬contains(OUT.errors, password)
```

**Test Strategy:** Scan all logs, responses, and error messages for password patterns

**Traceability:** REQ-7 (AC-7.2)

---

### CP-4: Token Generation Consistency (Related to REQ-6)

**Property:** For a successful authentication, a JWT token shall always be generated with correct claims matching the authenticated user.

**Formal Definition:**
```
∀ user U, token T:
  authenticate(U) = success ⟹
    ∃ T: (T.userId = U.userId) ∧ 
         (T.bankCode = U.bankCode) ∧
         (T.branchCode = U.branchCode) ∧
         (T.currency = U.currency)
```

**Test Strategy:** Verify token claims match user attributes for all successful logins

**Traceability:** REQ-6 (AC-6.2)

---

### CP-5: Audit Completeness (Related to REQ-4)

**Property:** Every login attempt (successful or failed) shall generate exactly one audit log entry.

**Formal Definition:**
```
∀ login attempt A:
  execute(A) ⟹ ∃! log L: 
    (L.timestamp = A.timestamp) ∧
    (L.username = A.username) ∧
    (L.outcome ∈ {SUCCESS, FAILURE})
```

**Test Strategy:** Count audit log entries and verify one-to-one mapping with login attempts

**Traceability:** REQ-4 (AC-4.2)

---

### CP-6: Error Message Safety (Related to REQ-2)

**Property:** Error messages shall never reveal whether a username exists or whether the password was incorrect.

**Formal Definition:**
```
∀ invalid credentials C1, C2:
  (C1.username ∉ Users ∨ C1.password ≠ correct) ∧
  (C2.username ∉ Users ∨ C2.password ≠ correct) ⟹
    errorMessage(C1) = errorMessage(C2) = "Invalid credentials"
```

**Test Strategy:** Verify identical error messages for non-existent users vs. wrong passwords

**Traceability:** REQ-2 (AC-2.3)

---

### CP-7: Currency Preservation (Related to REQ-1)

**Property:** The currency specified during login shall be preserved in the JWT token and response without modification.

**Formal Definition:**
```
∀ login request R, response RESP, token T:
  R.currency ∈ {SGD, USD, EUR} ⟹
    (RESP.currency = R.currency) ∧ (T.claims.currency = R.currency)
```

**Test Strategy:** Verify currency value consistency across request, response, and token

**Traceability:** REQ-1 (AC-1.2)

---

### CP-8: BCrypt Verification Correctness (Related to REQ-7)

**Property:** BCrypt password verification shall return true if and only if the plain password matches the stored hash.

**Formal Definition:**
```
∀ password P, hash H:
  BCrypt.verify(P, H) = true ⟺ H = BCrypt.hash(P)
```

**Test Strategy:** Test with known password/hash pairs and verify verification results

**Traceability:** REQ-7 (AC-7.1)

---

## Non-Functional Requirements

### NFR-1: Performance
**Related Requirements:** All functional requirements

- Login request processing shall complete within 500ms (95th percentile)
- System shall support 100 concurrent login requests
- Database queries shall use indexed lookups on (bankCode, branchCode, username)

### NFR-2: Security
**Related Requirements:** REQ-2, REQ-7

- All passwords shall be hashed using BCrypt with strength ≥ 10
- Passwords shall never be logged or exposed in any form
- Failed login attempts shall be logged for security monitoring
- Generic error messages shall not reveal system internals

### NFR-3: Availability
**Related Requirements:** REQ-6

- Service shall have 99.9% uptime
- Graceful degradation when Authentication Service is unavailable
- Health check endpoint shall respond within 100ms

### NFR-4: Scalability
**Related Requirements:** All functional requirements

- Service shall be stateless to support horizontal scaling
- Database connection pooling shall be configured for optimal performance
- Support for multiple database replicas for read operations

### NFR-5: Maintainability
**Related Requirements:** All functional requirements

- Code shall follow Hexagonal Architecture pattern
- All business logic shall be in domain layer
- Infrastructure concerns shall be isolated in adapters
- Comprehensive unit and integration tests (>80% coverage)

### NFR-6: Compliance
**Related Requirements:** REQ-4

- Audit logs shall be retained for 7 years
- Timestamps shall be in SGT (UTC+8) timezone
- All sensitive operations shall be logged
- GDPR compliance for user data handling

---

## Edge Cases & Error Scenarios

### Edge Case 1: Empty String Credentials
**Related Requirement:** REQ-5  
**Scenario:** User submits empty strings for required fields  
**Expected:** HTTP 400 with validation error

### Edge Case 2: SQL Injection Attempt
**Related Requirement:** REQ-5  
**Scenario:** User submits SQL injection patterns in username  
**Expected:** Parameterized queries prevent injection; treated as invalid username

### Edge Case 3: Very Long Input Strings
**Related Requirement:** REQ-5  
**Scenario:** User submits extremely long strings (>1000 chars)  
**Expected:** HTTP 400 with validation error (field length exceeded)

### Edge Case 4: Special Characters in Username
**Related Requirement:** REQ-1  
**Scenario:** User has special characters in username (e.g., "user+test@bank.com")  
**Expected:** System accepts and processes correctly

### Edge Case 5: Authentication Service Timeout
**Related Requirement:** REQ-6  
**Scenario:** Authentication Service doesn't respond within timeout  
**Expected:** HTTP 503 Service Unavailable with retry guidance

### Edge Case 6: Database Connection Failure
**Related Requirement:** All functional requirements  
**Scenario:** Database is unavailable during login attempt  
**Expected:** HTTP 503 Service Unavailable; log critical error

### Edge Case 7: Concurrent Login Attempts
**Related Requirement:** REQ-1  
**Scenario:** Same user attempts login from multiple locations simultaneously  
**Expected:** All attempts processed independently; all succeed if credentials valid

### Edge Case 8: Case Sensitivity
**Related Requirement:** REQ-1  
**Scenario:** User submits username with different case (e.g., "TestUser" vs "testuser")  
**Expected:** Username comparison is case-sensitive; "TestUser" ≠ "testuser"

---

## Dependencies

### Internal Services
- **Authentication Service (Port 8081)**: JWT token generation
  - Endpoint: POST /api/v1/auth/generate-token
  - Required for: Token generation after successful authentication (REQ-6)
  - Failure Impact: Login fails with HTTP 503

### External Dependencies
- **H2 Database (In-Memory)**: User credential storage
  - Used for: Development and testing
  - Production: Replace with PostgreSQL
  
### Configuration Dependencies
- BCrypt password encoder (strength: 10) - REQ-7
- Timezone: SGT (UTC+8) - REQ-4
- Supported currencies: SGD, USD, EUR - REQ-1

---

## Test Data

### Test Users (Development/Testing)
All test users use password: `password123`

| Username | Bank Code | Branch Code | Currency | Purpose | Related Requirement |
|----------|-----------|-------------|----------|---------|---------------------|
| testuser | 101 | 1119 | SGD | Default test user | REQ-1 |
| 1119test1 | 101 | 1119 | SGD | Feature flag testing (ACL 1000) | REQ-1, REQ-3 |
| 1119test2 | 101 | 1119 | USD | Feature flag testing (ACL 1001) | REQ-1, REQ-3 |
| 1119test3 | 101 | 1119 | EUR | Feature flag testing (All ACLs) | REQ-1, REQ-3 |
| adminuser | 102 | 2001 | SGD | Different bank testing | REQ-3 |
| demouser | 103 | 3001 | USD | Different bank testing | REQ-3 |

---

## Requirements Traceability Matrix

| Requirement ID | User Story | Acceptance Criteria | Correctness Property | Design Section | Tasks |
|----------------|------------|---------------------|---------------------|----------------|-------|
| REQ-1 | US-1 | AC-1.1, AC-1.2 | CP-1, CP-7 | Domain Service | Phase 2 |
| REQ-2 | US-2 | AC-2.1, AC-2.2, AC-2.3 | CP-6 | Exception Handler | Phase 3 |
| REQ-3 | US-3 | AC-3.1, AC-3.2 | CP-2 | Domain Service | Phase 2 |
| REQ-4 | US-4 | AC-4.1, AC-4.2 | CP-5 | Audit Adapter | Phase 4 |
| REQ-5 | Implicit | AC-5.1, AC-5.2 | - | DTOs, Validation | Phase 3 |
| REQ-6 | Implicit | AC-6.1, AC-6.2 | CP-4 | Auth Service Adapter | Phase 4 |
| REQ-7 | Implicit | AC-7.1, AC-7.2 | CP-3, CP-8 | Security Config | Phase 5 |

---

## Glossary

- **Bank Code**: 3-digit identifier for a banking institution (e.g., "101")
- **Branch Code**: 4-digit identifier for a bank branch (e.g., "1119")
- **Multi-Tenancy**: Supporting multiple banks/branches in single service instance
- **BCrypt**: Cryptographic hash function for password storage
- **JWT**: JSON Web Token for stateless authentication
- **SGT**: Singapore Time (UTC+8)
- **Audit Trail**: Chronological record of system activities for security/compliance
- **Hexagonal Architecture**: Architectural pattern separating domain logic from infrastructure
- **Property-Based Testing**: Testing approach using formal properties and random test data generation

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-03-05 | Development Team | Initial comprehensive requirements specification |

---

## Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Product Owner | | | |
| Technical Lead | | | |
| Security Officer | | | |
| QA Lead | | | |
