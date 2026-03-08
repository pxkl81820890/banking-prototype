# Corporate Security & Compliance Standards

## 1. Authentication & Identity
-**Protocol**: All inter-service communication must use **JWT (JSON Web Tokens)**.
-**Algorithm**: Tokens must be signed using **RS256** (Asymmetric). No HS256 (Symmetric) allowed.
-**Expiry**: Access tokens must have a Maximum TTL (Time-To-Live) of **60 minutes**.
-**MFA**: The 'LoginService' must not issue a "Full Access" token until the 'MFASecvice' returns a success callback.

## 2. Secrets Management (The "Anti-Leak" Rule)
-**Zero Hardcoding**: Never hardcode API keys, DB passwords, or JWT secrets in code.
-**Injection**: Use Spring '@Value(""...")' or 'Environment' variables for all sensitive config.
-**Kiro Guard**: If the Al attempts to write a plain-text secret, it must be flagged as a security violation.

## 3. Data Protection & Privacy (PDPA/GDPR)
-**Encryption at Rest**: Sensitive fields (NRIC, Phone Numbers) must be encrypted before saving to PostgreSQL.
-**Logging**: Never log PII (Personally Identifiable Information). Use masks like 'user_id: 123xx45'.
-**Passwords**: Must be hashed using **BCrypt** with a minimum strength of 12. Never store plain text.

## 4. Input Validation (The "Anti-Injection" Rule)
-**SOL Injection**: Use **Spring Data JPA** or **NamedParameterJdbcTemplate**. Never use string concatenation for SQL.
-**XSS/Scripting**: All inbound strings in the Driving Layer (Controllers) must be sanitized.
-**Validation**: Use 'jakarta.validation' annotations (e.g., '@NotBlank', '@Size', '@Email') on all DTOs.

## 5. Security Headers & CORS
-**CORS**: Only allow origins fromn '*.company.com'
-**Headers**: Implement 'X-Content-Type-Options: nosniff' and 'Content-Security-Policy'.