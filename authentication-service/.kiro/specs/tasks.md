# Implementation Tasks: Authentication Service - JWT Token Provider

## Phase 1: Project Setup & Dependencies
- [x] **Task 1.1**: Add JJWT dependencies to `pom.xml` (jjwt-api, jjwt-impl, jjwt-jackson version 0.12.x).
- [x] **Task 1.2**: Add Bouncy Castle dependency for key parsing support.
- [x] **Task 1.3**: Configure `application.yml` with JWT properties (private-key-path, public-key-path, expiration, issuer).

## Phase 2: Key Pair Generation
- [x] **Task 2.1**: Generate RSA 2048-bit key pair using OpenSSL or Java KeyPairGenerator.
- [x] **Task 2.2**: Store keys in `src/main/resources/keys/` directory.
- [ ] **Task 2.3**: Add keys directory to `.gitignore` to prevent committing private keys.

## Phase 3: Domain Layer (The Core)
- [x] **Task 3.1**: Create `TokenPayload` value object in `domain.model` (Pure Java Record with validation).
- [x] **Task 3.2**: Create `GeneratedToken` value object in `domain.model` (Pure Java Record).
- [x] **Task 3.3**: Define Inbound Port `TokenGenerationUseCase` interface in `domain.ports`.
- [x] **Task 3.4**: Define Outbound Port `TokenProviderOutputPort` interface in `domain.ports`.
- [x] **Task 3.5**: Implement `TokenGenerationService` in `domain.service`.

## Phase 4: Infrastructure Layer (Outbound Adapter)
- [x] **Task 4.1**: Create `JwtTokenProvider` in `infrastructure.security`.
- [x] **Task 4.2**: Create `KeyLoader` utility class in `infrastructure.security`.

## Phase 5: Infrastructure Layer (Inbound Adapter)
- [x] **Task 5.1**: Update `AuthController` in `infrastructure.adapters.in`.
- [x] **Task 5.2**: Create request/response DTOs as Java Records.
- [x] **Task 5.3**: Implement validation for request DTO.

## Phase 6: Configuration & Security
- [x] **Task 6.1**: Create `JwtConfigProperties` class with `@ConfigurationProperties`.
- [x] **Task 6.2**: Disable Spring Security default authentication for token generation endpoint.

## Phase 7: Testing & Verification
- [x] **Task 7.1**: Write JUnit 5 unit tests for `TokenGenerationService`.
- [ ] **Task 7.2**: Write integration tests for `JwtTokenProvider`.
- [ ] **Task 7.3**: Write integration tests for `AuthController`.

## Phase 8: Documentation & Deployment
- [ ] **Task 8.1**: Create README with key generation instructions.
- [ ] **Task 8.2**: Document JWT claims structure and usage.
- [ ] **Task 8.3**: Add endpoint to expose public key for token verification (GET `/api/v1/auth/public-key`).
