# Authentication Service

JWT token generation service for multi-entity banking platform using RS256 algorithm.

## Overview

The Authentication Service generates stateless JWT tokens with custom claims for bank, branch, and currency context. Tokens are signed using RS256 (RSA with SHA-256) for secure, asymmetric encryption.

## Architecture

Built using Hexagonal Architecture (Ports & Adapters):
- **Domain Layer**: Token generation orchestration
- **Inbound Adapters**: REST API controllers
- **Outbound Adapters**: JWT signing with JJWT library

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- RSA key pair (generated automatically or manually)

### Running the Service

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8081`

## API Endpoints

### POST /api/v1/auth/generate-token

Generates a JWT token with multi-entity banking claims.

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-uuid-123",
    "bankCode": "101",
    "branchCode": "1119",
    "currency": "SGD"
  }'
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Token generated successfully",
  "expiresIn": 3600
}
```

**Error Responses:**

- **400 Bad Request** - Missing or invalid fields
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "userId is required",
  "path": "/api/v1/auth/generate-token"
}
```

- **500 Internal Server Error** - Token generation failed
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to load private key",
  "path": "/api/v1/auth/generate-token"
}
```

## JWT Token Structure

Generated tokens include the following claims:

```json
{
  "sub": "user-uuid-123",
  "bnk": "101",
  "brn": "1119",
  "cur": "SGD",
  "iat": 1640000000,
  "exp": 1640003600,
  "iss": "banking-auth-service"
}
```

- `sub`: User identifier (Subject)
- `bnk`: Bank code (custom claim)
- `brn`: Branch code (custom claim)
- `cur`: Currency code (custom claim)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp
- `iss`: Issuer

## Configuration

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8081

app:
  jwt:
    private-key-path: classpath:keys/private_key.pem
    public-key-path: classpath:keys/public_key.pem
    expiration: 3600  # 1 hour
    issuer: banking-auth-service
```

## Key Management

### Generate RSA Key Pair

```bash
# Generate private key
openssl genrsa -out private_key.pem 2048

# Generate public key
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Move to resources
mv private_key.pem src/main/resources/keys/
mv public_key.pem src/main/resources/keys/
```

**Important**: Add `src/main/resources/keys/` to `.gitignore` to prevent committing private keys.

## Security

- RS256 algorithm (asymmetric encryption)
- Private key secured and never exposed via API
- Stateless token design (no server-side session storage)
- Configurable token expiration (default: 1 hour)
- Public key can be distributed for token verification

## API Documentation

Full OpenAPI 3.0 specification available at: `docs/api-spec.yaml`

### Interactive Swagger UI

After starting the service, access the interactive API documentation:

**Swagger UI**: http://localhost:8081/swagger-ui.html

**OpenAPI JSON**: http://localhost:8081/api-docs

You can test token generation directly from the Swagger UI interface.

### Alternative Viewing Options

- Import `docs/api-spec.yaml` into [Swagger Editor](https://editor.swagger.io/)
- Use Postman: Import → Upload Files → Select `docs/api-spec.yaml`

## Dependencies

- Spring Boot 3.4.x
- JJWT 0.12.x (jjwt-api, jjwt-impl, jjwt-jackson)
- Bouncy Castle for key parsing

## Related Services

- **Login Service** (port 8080): User authentication and login
