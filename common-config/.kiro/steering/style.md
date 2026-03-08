# Java Coding & Hexagonal Style Standards

## 1. Hexagonal Purity (The "No-Go" Zones)
- **Domain Layer**: Must have ZERO imports from `org.springframework.*` or `jakarta.persistence.*`.
- **Domain Models**: Use **Java Records** for all DTOs and Domain Models to ensure immutability.
- **Exceptions**: Core logic must throw **Domain-Specific Exceptions** (e.g., `UserNotFoundException`), NOT HTTP or Persistence exceptions.

## 2. Spring & Dependency Injection
- **Injection**: Use **Constructor Injection** exclusively. Do **NOT** use `@Autowired` on fields.
- **Naming**:
    - Inbound Ports must end in `UseCase` (e.g., `LoginUseCase`).
    - Outbound Ports must end in `OutputPort` (e.g., `UserOutputPort`).
    - Adapters must end in `Adapter` (e.g., `PostgresUserAdapter`).

## 3. Java 17+ Best Practices
- **Switch**: Use **Switch Expressions** for multi-case logic.
- **Strings**: Use **Text Blocks** (`"""..."""`) for SQL queries or long JSON samples in tests.
- **Logic**: Prefer `Optional<T>` over returning `null`.

## 4. Documentation & Logging
- **Logging**: Use **SLF4J** `log.info()` or `log.error()`. Never use `System.out.println`.
- **Self-Documenting**: Method names should be descriptive (e.g., `processSuccessfulLogin` instead of `procLog`).

## 5. Boilerplate Reduction (Lombok)
- **Entities/Models**: Use `@Getter`, `@Setter`, and `@ToString`.
- **Constructors**: Use `@RequiredArgsConstructor` for final fields (this handles **Constructor Injection** for Spring automatically).
- **Builders**: Use `@Builder` for complex Domain models to ensure "fluid" object creation.
- **Logging**: Use `@Slf4j` for all classes requiring logging.
- **Constraint**: Do **NOT** use `@Data` on JPA Entities (it can cause circular dependency issues with `hashCode`/`equals`); use specific annotations instead.
