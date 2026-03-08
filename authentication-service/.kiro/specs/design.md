# Technical Design: Authentication Service - JWT Token Provider

## 1. Technology Stack

- **Framework**: Spring Boot 3.4.x
- **Security**: Spring Security 6
- **JWT Library**: `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (JJWT 0.12.x)
- **Algorithm**: RS256 (RSA Signature with SHA-256)
- **Key Format**: PEM-encoded RSA key pair (2048-bit minimum)

---

## 2. Hexagonal Architecture Layers

### Domain Layer (Core)
- **TokenPayload**: Value object containing userId, bankCode, branchCode, currency
- **GeneratedToken**: Value object containing the JWT token string and metadata
- **TokenGenerationUseCase**: Inbound port interface for token generation
- **TokenProviderOutputPort**: Outbound port interface for JWT signing operations

### Infrastructure Layer (Adapters)
- **Inbound Adapter**: `AuthController` - REST endpoint for token generation
- **Outbound Adapter**: `JwtTokenProvider` - Implements TokenProviderOutputPort using JJWT library

### Application Layer
- **TokenGenerationService**: Domain service implementing TokenGenerationUseCase

---

## 3. JWT Claims Structure

```json
{
  "sub": "user-uuid-123",
  "bnk": "101",
  "brn": "1119",
  "cur": "SGD",
  "iat": 1640000000,
  "exp": 1640003600
}
```

**Claim Definitions**:
- `sub` (Subject): User UUID - standard JWT claim for user identification
- `bnk` (Bank Code): Custom claim for multi-entity banking context
- `brn` (Branch Code): Custom claim for branch-level authorization
- `cur` (Currency): Custom claim for currency-specific operations
- `iat` (Issued At): Standard JWT claim for token generation timestamp
- `exp` (Expiration): Standard JWT claim for token expiration timestamp

---

## 4. Key Management Design

### Key Pair Generation
- **Algorithm**: RSA 2048-bit
- **Format**: PEM-encoded PKCS#8 (private key) and X.509 (public key)
- **Storage**: File system (configurable paths)
- **Naming Convention**:
    - Private Key: `private_key.pem`
    - Public Key: `public_key.pem`

### Key Loading Strategy
- Keys are loaded at application startup via `@PostConstruct`
- Private key is used for signing tokens
- Public key can be exposed via endpoint for token verification by other services
- Application fails fast if keys are missing or invalid

---

## 5. API Contract

### POST /api/v1/auth/generate-token

**Request Body (JSON)**:
```json
{
  "userId": "user-uuid-123",
  "bankCode": "101",
  "branchCode": "1119",
  "currency": "SGD"
}
```

**Success Response (200 OK)**:
```json
{
  "success": true,
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Token generated successfully",
  "expiresIn": 3600
}
```

**Error Response (400 Bad Request)**:
```json
{
  "success": false,
  "token": null,
  "message": "Invalid request: userId is required",
  "expiresIn": 0
}
```

---

## 6. Configuration Properties

```yaml
app:
  jwt:
    private-key-path: classpath:keys/private_key.pem
    public-key-path: classpath:keys/public_key.pem
    expiration: 3600  # 1 hour in seconds
    issuer: banking-auth-service
```

---

## 7. Domain Model

### TokenPayload (Value Object)
```java
public record TokenPayload(
    String userId,
    String bankCode,
    String branchCode,
    String currency
) {
    // Compact constructor with validation
}
```

### GeneratedToken (Value Object)
```java
public record GeneratedToken(
    String token,
    long expiresIn,
    String message
) {}
```

---

## 8. Port Interfaces

### Inbound Port (Driving)
```java
public interface TokenGenerationUseCase {
    GeneratedToken generateToken(TokenPayload payload);
}
```

### Outbound Port (Driven)
```java
public interface TokenProviderOutputPort {
    String signToken(TokenPayload payload, long expirationSeconds);
}
```

---

## 9. Security Considerations

1. **Private Key Protection**: Private key must be secured and never exposed via API
2. **Token Expiration**: Tokens have limited lifetime to reduce security risk
3. **Stateless Design**: No server-side session storage required
4. **Algorithm Choice**: RS256 allows public key distribution for token verification
5. **Key Rotation**: Design supports key rotation by updating key files and restarting service

---

## 10. Testing Strategy

- **Unit Tests**: Mock TokenProviderOutputPort to test TokenGenerationService logic
- **Integration Tests**: Test actual JWT generation and validation with real keys
- **Security Tests**: Verify token signature, expiration, and claim integrity
