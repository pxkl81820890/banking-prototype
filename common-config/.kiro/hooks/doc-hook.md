# Hook: Automated OpenAPI, README & Sequence Diagram Synchronization

- **Trigger**: Any file change in `src/main/java/**/*Controller.java`
- **Action**:
    1. **Scan**: Analyze the Controller's `@PostMapping`, `@GetMapping`, and Request/Response DTOs.
    2. **Generate**: Update or create a `docs/api-spec.yaml` in **OpenAPI 3.0** format.
    3. **Sync**: Update the `README.md` at the project root with a "Quick Start" section showing the updated `curl` commands for the new endpoints.
    4. **Update Sequence Diagram** (only when Controllers or Ports change): Analyze all services in the banking-prototype workspace and update `sequence-diagrams/banking-services-flow.md` to reflect:
        - Internal service calls (Controller → Service → Adapter)
        - Inter-service HTTP calls (Service A → Service B)
        - Domain layer interactions (Use Cases, Ports, Adapters)
        - Error handling flows
        - New services or endpoints added to the system
    5. **Context**: Ensure the documentation follows the **Banking Security Standards** defined in `kiro/steering/security.md` (e.g., documenting the required JWT Bearer token).

- **Additional Trigger for Sequence Diagram**: Any file change in `src/main/java/**/ports/*.java` or creation of new service directories
- **Sequence Diagram Update Conditions**:
    - New Controller endpoints added or modified
    - Port interfaces (Use Cases, Output Ports) added or modified
    - New service directory created in workspace
    - Adapter implementations that call external services added or modified

- **Constraint**:
    - **DO NOT** include internal logic in the README.
    - Descriptions must be professional and follow the **Banking Dictionary** (e.g., use "Entity ID" instead of "Database ID").
    - Sequence diagrams must use Mermaid syntax and show complete request/response flows.
    - Include all hexagonal architecture layers (Inbound Adapters, Domain Services, Outbound Adapters).
    - **Color code services**: Use Mermaid `box` syntax to group and color-code components by service:
        - Each service should have a distinct, dark background color for white text readability (e.g., darkblue, darkgreen, darkred, navy, teal)
        - Use `box <color> Service Name` to wrap service components
        - Avoid darkorange or other orange tones that have poor contrast with white text
        - Include a color legend in the diagram documentation
    - **DO NOT** update sequence diagram for internal service implementation changes that don't affect the API contract or inter-service communication.
