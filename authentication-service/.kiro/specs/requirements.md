# Authentication Service - JWT Token Provider (EARS Format)

## 1. JWT Token Generation (The "Success Path")

- **RE-01**: **When** the system receives a valid token generation request containing:
    - `userId` (User UUID)
    - `bankCode` (3 digits, e.g., "101")
    - `branchCode` (4 digits, e.g., "1119")
    - `currency` (e.g., "SGD")
- **The AuthenticationService shall**:
    1. **Validate Input**: Ensure all required fields are present and non-empty.
    2. **Generate JWT**: Create a stateless JWT token signed with **RS256** algorithm using a private key.
    3. **Include Claims**: Embed the following claims in the JWT payload:
        - `sub` (Subject): User UUID
        - `bnk` (Bank Code): 3-digit bank code
        - `brn` (Branch Code): 4-digit branch code
        - `cur` (Currency): Currency code
        - `iat` (Issued At): Token generation timestamp
        - `exp` (Expiration): Token expiration timestamp (configurable, default 1 hour)
    4. **Response**: Return a `200 OK` with the signed JWT token string.

---

## 2. Token Expiration Configuration

- **RE-02**: **When** generating a JWT token,
- **The AuthenticationService shall**:
    1. Set the token expiration time based on the configured `app.jwt.expiration` property (default: 3600 seconds / 1 hour).
    2. Include the `exp` claim in the JWT payload with the calculated expiration timestamp.

---

## 3. Validation & Error Handling

- **RE-03**: **When** the token generation request is missing required fields or contains invalid data,
- **The AuthenticationService shall**:
    1. Return a `400 Bad Request` with an error message indicating which field is invalid.
    2. Log the validation failure for monitoring purposes.

---

## 4. Key Management

- **RE-04**: **While** the system is configured with RS256 key pair,
- **The AuthenticationService shall**:
    1. Load the private key from the configured location (`app.jwt.private-key-path`).
    2. Use the private key to sign all generated JWT tokens.
    3. Fail to start if the private key is missing or invalid.

---

## 5. Stateless Token Design

- **RE-05**: **The AuthenticationService shall**:
    1. Generate stateless JWT tokens that contain all necessary user context in the claims.
    2. **NOT** store any session state on the server side.
    3. Allow token validation to be performed independently by any service with access to the public key.
