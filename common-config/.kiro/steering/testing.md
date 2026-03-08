# Microservice Quality & Testing Standards

## 1. Test-Driven Development (TDD)
- **Rule**: Every new business requirement in `requirements.md` must have a corresponding test.
- **Rule**: Kiro must generate the test **before** or **alongside** the implementation.

## 2. Unit Testing (The "Core" Layer)
- **Framework**: JUnit 5 (Jupiter) and Mockito.
- **Purity**: Domain tests must **NOT** load the Spring Context. Use `MockitoExtension`.
- **Target**: 100% coverage on all "Domain Service" and "Value Object" logic.

## 3. Integration Testing (The "Adapters")
- **Database**: Use **Testcontainers** (Postgres) for Repository tests. No H2 (to ensure feature parity).
- **Web**: Use `MockMvc` for Driving Adapters (Controllers) to verify HTTP status codes and JSON mapping.
- **Profiles**: Run integration tests using the `test` profile.

## 4. Mocking Strategy
- **Isolation**: Always mock Outbound Ports (e.g., `UserOutputPort`) when testing the Domain Service.
- **Verification**: Ensure that `verify()` is used to confirm that external calls (like saving to a DB) actually occur.

## 5. Definition of Done (DoD)
- **Green Build**: `mvn test` must pass with zero failures.
- **Documentation**: All test methods must be descriptive (e.g., `should_ReturnUnauthorized_When_PasswordIsInvalid`).
