# Shared Kiro Configuration Guide

## Overview

This guide explains how to share Kiro steering files and hooks across all microservices in the banking application.

## Directory Structure

```
banking-app/                           # Root of monorepo
├── .kiro/                             # SHARED Kiro config (all services)
│   ├── steering/                      # Organization-wide standards
│   │   ├── architecture-standards.md  # Hexagonal Architecture rules
│   │   ├── spring-boot-standards.md   # Spring Boot conventions
│   │   ├── security-standards.md      # Security requirements
│   │   ├── java-coding-standards.md   # Java best practices
│   │   └── testing-standards.md       # Testing requirements
│   │
│   └── hooks/                         # Organization-wide hooks
│       ├── lint-on-save.json          # Auto-lint on file save
│       └── test-before-commit.json    # Run tests before commit
│
├── login-service/
│   ├── .kiro/
│   │   └── specs/                     # Service-specific specs
│   │       ├── .config.kiro
│   │       ├── requirements.md
│   │       ├── design.md
│   │       └── tasks.md
│   └── src/
│
├── authentication-service/
│   ├── .kiro/
│   │   └── specs/
│   │       ├── .config.kiro
│   │       ├── requirements.md
│   │       ├── design.md
│   │       └── tasks.md
│   └── src/
│
├── channel-configurations-service/
│   ├── .kiro/
│   │   └── specs/
│   │       ├── .config.kiro
│   │       ├── requirements.md
│   │       ├── design.md
│   │       └── tasks.md
│   └── src/
│
├── gateway-service/
│   ├── .kiro/
│   │   └── specs/
│   └── src/
│
└── user-service/
    ├── .kiro/
    │   └── specs/
    └── src/
```

---

## How Kiro Reads Configuration

Kiro follows a **hierarchical search** when looking for steering files and hooks:

### Search Order (Highest to Lowest Priority)

1. **Service-level** (most specific)
   - `{service}/.kiro/steering/`
   - `{service}/.kiro/hooks/`

2. **Root-level** (shared across all services)
   - `.kiro/steering/`
   - `.kiro/hooks/`

3. **User-level** (global, across all projects)
   - `~/.kiro/steering/`
   - `~/.kiro/hooks/`

### Precedence Rules

- **Service-specific files override shared files**
- If `login-service/.kiro/steering/security-standards.md` exists, it overrides `.kiro/steering/security-standards.md`
- If a file only exists at root level, all services use it

---

## What Goes Where?

### Root `.kiro/steering/` (Shared Standards)

**Purpose:** Organization-wide standards that apply to ALL services

**Files:**
- `architecture-standards.md` - Hexagonal Architecture, package structure
- `spring-boot-standards.md` - Spring Boot conventions, REST API standards
- `security-standards.md` - Authentication, authorization, encryption
- `java-coding-standards.md` - Naming conventions, code style
- `testing-standards.md` - Unit tests, integration tests, property-based tests
- `database-standards.md` - Schema design, migration strategy
- `api-standards.md` - OpenAPI/Swagger, versioning, error responses

### Root `.kiro/hooks/` (Shared Hooks)

**Purpose:** Organization-wide automation that applies to ALL services

**Files:**
- `lint-on-save.json` - Auto-lint Java files on save
- `test-before-commit.json` - Run tests before committing
- `format-on-save.json` - Auto-format code on save

### Service `.kiro/specs/` (Service-Specific)

**Purpose:** Feature specifications unique to each service

**Files:**
- `.config.kiro` - Workflow configuration
- `requirements.md` - What to build for this service
- `design.md` - How to build it for this service
- `tasks.md` - Implementation tasks for this service

### Service `.kiro/steering/` (Service-Specific Overrides - Optional)

**Purpose:** Service-specific standards that override shared standards

**Example:**
- `login-service/.kiro/steering/authentication-specific.md` - Login-specific rules
- `gateway-service/.kiro/steering/routing-standards.md` - Gateway-specific routing rules

---

## Setup Instructions

### Step 1: Create Shared Config Directory

```bash
# At root of banking-app
mkdir -p .kiro/steering
mkdir -p .kiro/hooks
```

### Step 2: Move Common Files to Root

```bash
# Move architecture standards (if they exist in a service)
mv login-service/.kiro/steering/architecture-standards.md .kiro/steering/
mv login-service/.kiro/steering/spring-boot-standards.md .kiro/steering/
mv login-service/.kiro/steering/security-standards.md .kiro/steering/

# Move common hooks (if they exist)
mv login-service/.kiro/hooks/lint-on-save.json .kiro/hooks/
```

### Step 3: Keep Service-Specific Specs

```bash
# Each service keeps its own specs
# login-service/.kiro/specs/
# authentication-service/.kiro/specs/
# etc.
```

### Step 4: Verify Structure

```bash
# Check root config
ls -la .kiro/steering/
ls -la .kiro/hooks/

# Check service-specific specs
ls -la login-service/.kiro/specs/
ls -la authentication-service/.kiro/specs/
```

---

## How to Use

### When Creating a New Service

1. **Create service directory:**
```bash
mkdir new-service
cd new-service
```

2. **Create service-specific specs directory:**
```bash
mkdir -p .kiro/specs
```

3. **Kiro automatically uses root-level steering and hooks:**
   - No need to copy files
   - Kiro searches parent directories automatically

4. **Create spec for the new service:**
```bash
# Ask Kiro to create a spec
# Kiro will read shared steering files from root .kiro/steering/
```

### When Updating Shared Standards

1. **Edit root steering file:**
```bash
# Edit shared standard
code .kiro/steering/architecture-standards.md
```

2. **All services automatically use the updated standard:**
   - Next time Kiro generates code for any service, it reads the updated file
   - No need to sync or copy

### When Adding Service-Specific Rules

1. **Create service-specific steering file:**
```bash
mkdir -p login-service/.kiro/steering
code login-service/.kiro/steering/login-specific.md
```

2. **This file only applies to login-service:**
   - Other services continue using root-level standards
   - login-service uses both root + service-specific files

---

## Example: Adding a New Shared Standard

Let's say you want to add a new "Error Handling Standard" that applies to all services.

### Step 1: Create the File

```bash
# Create at root level
code .kiro/steering/error-handling-standards.md
```

### Step 2: Add Content

```markdown
# Error Handling Standards

## Exception Hierarchy
- All custom exceptions MUST extend RuntimeException
- Use specific exception types (not generic Exception)

## Error Response Format
```json
{
  "timestamp": "2026-03-04T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/api/v1/login"
}
```

## Global Exception Handler
- Every service MUST have a GlobalExceptionHandler
- Use @ControllerAdvice annotation
- Handle all custom exceptions
```

### Step 3: Use in Any Service

```bash
# When working on login-service
cd login-service

# Ask Kiro to create a spec
# Kiro automatically reads .kiro/steering/error-handling-standards.md
# and applies it to the design
```

---

## Example: Service-Specific Override

Let's say the gateway-service needs different routing standards.

### Step 1: Create Service-Specific File

```bash
mkdir -p gateway-service/.kiro/steering
code gateway-service/.kiro/steering/routing-standards.md
```

### Step 2: Add Content

```markdown
# Gateway Routing Standards

## Route Prefix
- All routes MUST start with /api/v1/
- Service name MUST be included: /api/v1/login/**, /api/v1/auth/**

## Load Balancing
- Use round-robin by default
- Enable circuit breaker for all routes
```

### Step 3: Gateway Uses Both

When working on gateway-service, Kiro reads:
1. Root `.kiro/steering/architecture-standards.md` (shared)
2. Root `.kiro/steering/spring-boot-standards.md` (shared)
3. Root `.kiro/steering/security-standards.md` (shared)
4. `gateway-service/.kiro/steering/routing-standards.md` (service-specific)

---

## Benefits

### 1. Consistency Across Services
- All services follow the same architecture patterns
- Same coding standards everywhere
- Same security practices

### 2. Single Source of Truth
- Update one file → all services use it
- No need to sync or copy files manually
- No risk of services getting out of sync

### 3. Service-Specific Flexibility
- Services can override shared standards when needed
- Service-specific rules don't affect other services

### 4. Easy Onboarding
- New developers see standards in one place
- New services automatically inherit standards
- No need to remember to copy files

### 5. Version Control
- All standards are in Git
- Track changes to standards over time
- Review standard changes in PRs

---

## Common Patterns

### Pattern 1: All Services Use Same Standards

```
.kiro/steering/
├── architecture-standards.md
├── spring-boot-standards.md
└── security-standards.md

login-service/.kiro/specs/
authentication-service/.kiro/specs/
channel-configurations-service/.kiro/specs/
```

**Result:** All services follow the same standards.

### Pattern 2: One Service Needs Special Rules

```
.kiro/steering/
├── architecture-standards.md
├── spring-boot-standards.md
└── security-standards.md

gateway-service/.kiro/steering/
└── routing-standards.md        # Gateway-specific

gateway-service/.kiro/specs/
```

**Result:** Gateway uses shared standards + routing-standards.md

### Pattern 3: Service Overrides Shared Standard

```
.kiro/steering/
├── architecture-standards.md
└── security-standards.md

legacy-service/.kiro/steering/
└── security-standards.md       # Different security rules

legacy-service/.kiro/specs/
```

**Result:** Legacy service uses its own security-standards.md instead of the shared one.

---

## Troubleshooting

### Issue: Kiro Not Reading Shared Files

**Symptom:** Kiro generates code that doesn't follow shared standards

**Solution:**
1. Check file location: `ls -la .kiro/steering/`
2. Check file name matches exactly (case-sensitive)
3. Make sure you're running Kiro from the service directory or root

### Issue: Service-Specific File Not Overriding

**Symptom:** Service still uses shared standard instead of service-specific

**Solution:**
1. Check file name matches exactly (must be same name to override)
2. Check file location: `{service}/.kiro/steering/`
3. Restart Kiro or clear cache

### Issue: Changes to Shared File Not Applied

**Symptom:** Updated shared standard but Kiro still uses old version

**Solution:**
1. Make sure you saved the file
2. Restart Kiro session
3. Check you're editing the right file (root vs service-specific)

---

## Best Practices

### 1. Keep Shared Standards Generic
- Don't put service-specific rules in shared files
- Focus on organization-wide patterns

### 2. Document Why Standards Exist
- Add comments explaining the reasoning
- Link to relevant documentation

### 3. Version Control Everything
- Commit all steering files to Git
- Review changes in PRs
- Use meaningful commit messages

### 4. Review Standards Regularly
- Update standards as technology evolves
- Remove outdated practices
- Get team feedback

### 5. Use Service-Specific Sparingly
- Only override when truly necessary
- Document why the override is needed
- Consider if the shared standard should change instead

---

## Summary

- **Root `.kiro/`** = Shared across all services (organization-wide)
- **Service `.kiro/specs/`** = Service-specific feature specs
- **Service `.kiro/steering/`** = Service-specific overrides (optional)
- **Kiro automatically searches parent directories** for shared config
- **Service-specific files override shared files** when names match
- **No manual syncing needed** - Kiro reads files directly

This approach ensures consistency while allowing flexibility where needed!
