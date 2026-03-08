# Hook: Autonomous Test Generation (TDD Enforcement)

- **Trigger**: Any file creation or update in `src/main/java/**/*Service.java`
- **Action**:
    1. **Locate**: Find the corresponding test file in `src/test/java/`. If it doesn't exist, create it.
    2. **Analyze**: Identify all public methods and business logic branches in the service class.
    3. **Generate**: Using **JUnit 5** and **Mockito**, write unit tests for:
        - The **Success Path** (Happy Path).
        - **Edge Cases** (Null inputs, empty strings).
        - **Error States** (Throwing Domain-Specific Exceptions).
    4. **Compliance**: Ensure the test uses the `MockitoExtension` and follows the **Hexagonal Purity** rules in `steering/testing.md` (No Spring Context loading).

- **Goal**: Maintain 100% method coverage for the Core Layer automatically.
