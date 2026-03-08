# Corporate Microservice Tech Stack Standards

## 1. Core Frameworks
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.4.x
- **Build Tool**: Maven 3.9+
- **Security**: Spring Security 6 with OAuth2/JWT support.
- **Lombok**: Required for all boilerplate reduction.
- **Dependency**: `org.projectlombok:lombok` (provided/annotationProcessor).

## 2. Data & Persistence
- **Primary Database**: PostgreSQL 15+
- **ORM**: Spring Data JPA with Hibernate.
- **Migration**: Flyway (Required for all schema changes).
- **Caching**: Redis (Specifically for Session/Token storage).

## 3. Communication Patterns
- **API Style**: RESTful (Level 3 Maturity / HATEOAS preferred).
- **Serialization**: JSON (Jackson).
- **Internal Messaging**: Kafka (For Event-Driven updates between services).

## 4. Quality & Testing
- **Unit Testing**: JUnit 5 (Jupiter).
- **Mocking**: Mockito 5+.
- **Assertions**: AssertJ.
- **API Documentation**: SpringDoc OpenAPI (Swagger UI).

## 5. Deployment & DevOps
- **Containerization**: Docker (Multi-stage builds).
- **Orchestration**: Kubernetes-ready (Include Health/Liveness probes).
- **Logging**: SLF4J with Logback (JSON Format for ELK Stack).

## 6. Constraints
- **Discovery**: Do **NOT** use Spring Cloud Netflix Eureka (Prefer Kubernetes Native Discovery).
- **Injection**: Do **NOT** use Field-level `@Autowired` (Use Constructor Injection).
- **Models**: All DTOs must be implemented as **Java Records**.

## 7. Architectural Pattern: Hexagonal
- **Core Strategy**: Separation of Business Logic from Infrastructure.
- **Dependency Rule**: Dependencies must point **inwards** toward the Domain.
- **Port Types**:
    - **Driving (Inbound)**: API Controllers / REST.
    - **Driven (Outbound)**: Repositories / Persistence / External APIs.
