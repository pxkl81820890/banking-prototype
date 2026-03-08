# Hook: Real-Time Security & Compliance Audit

- **Trigger**: Any file save in `**/*.{java,yml,properties,xml}`
- **Action**:
    1. **Secret Scan**: Check for high-entropy strings, hardcoded API keys, or plain-text passwords.
    2. **Protocol Check**: Ensure no insecure protocols (e.g., `http://`, `ws://`) are used for internal calls.
    3. **Pattern Audit**: Flag any use of unsafe Java patterns (e.g., `Thread.sleep()`, `System.out`, or `MD5` hashing).
    4. **Constraint**: If a violation is found, append a comment directly above the offending line: `// !! SECURITY ALERT: [Description] !!` and notify the developer in the Kiro terminal.
    
- **Reference**: Always align with the standards in `.kiro/steering/security.md`.
