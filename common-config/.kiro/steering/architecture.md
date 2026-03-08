# Hexagonal Architecture Guidelines

## 1. Package Structure

Every microservice must follow this strictly:
- `com.company.service.domain`: Entities and Core Logic (No Framework code).
- `com.company.service.application.ports.in`: Inbound interfaces (Use Cases).
- `com.company.service.application.ports.out`: Outbound interfaces (Repositories).
- `com.company.service.infrastructure.adapters.in`: Controllers and REST handlers.
- `com.company.service.infrastructure.adapters.out`: Persistence and External Clients.

---

## 2. Constraints

- **Domain Purity**: Domain classes must **NOT** import `jakarta.persistence` or Spring annotations.
- **Mapping**: Use **MapStruct** or DTOs to map between Infrastructure and Domain models.
- **Testing**: Test Domain logic using pure JUnit (no Spring Context).
