# Multi-Repo Shared Kiro Configuration Setup

## Scenario

You have **separate Git repositories** for each microservice:
- `login-service` (separate repo)
- `authentication-service` (separate repo)
- `channel-configurations-service` (separate repo)
- `gateway-service` (separate repo)
- `user-service` (separate repo)

Each service needs to read from **shared Kiro config** (steering files and hooks) during development.

---

## Solution: Git Submodule

Use a **Git submodule** to include the shared config repository inside each service repository.

---

## Step-by-Step Setup

### Step 1: Create Shared Config Repository

```bash
# Create a new repository for shared Kiro config
mkdir banking-shared-kiro-config
cd banking-shared-kiro-config
git init

# Create directory structure
mkdir -p .kiro/steering
mkdir -p .kiro/hooks

# Add your shared steering files
cat > .kiro/steering/architecture-standards.md << 'EOF'
# Architecture Standards

## Mandatory Patterns
- All services MUST use Hexagonal Architecture
- Domain logic MUST be isolated from infrastructure
- All services MUST expose OpenAPI documentation

## Package Structure
```
com.banking.{service}/
  ├── domain/
  │   ├── model/
  │   ├── ports/
  │   └── service/
  └── infrastructure/
      ├── adapters/
      ├── config/
      └── security/
```
EOF

cat > .kiro/steering/spring-boot-standards.md << 'EOF'
# Spring Boot Standards

## Dependencies
- Use Spring Boot 3.x
- Use Java 17+
- Include spring-boot-starter-validation

## REST API Standards
- Base path: /api/v1/{resource}
- Use proper HTTP methods (GET, POST, PUT, DELETE)
- Return proper status codes
- Use @Valid for request validation

## Configuration
- Use YAML for application.yml
- Externalize all configuration
- Use profiles: dev, test, prod
EOF

cat > .kiro/steering/security-standards.md << 'EOF'
# Security Standards

## Authentication
- Use JWT tokens for authentication
- Token expiration: 15 minutes (access), 7 days (refresh)
- Store tokens securely (httpOnly cookies)

## Authorization
- Use role-based access control (RBAC)
- Validate permissions on every endpoint
- Principle of least privilege

## Data Protection
- Encrypt sensitive data at rest
- Use HTTPS for all communication
- Sanitize all user inputs
- Use parameterized queries (prevent SQL injection)
EOF

cat > .kiro/steering/testing-standards.md << 'EOF'
# Testing Standards

## Unit Tests
- Test all domain logic
- Use JUnit 5
- Aim for 80%+ code coverage
- Mock external dependencies

## Integration Tests
- Test API endpoints
- Use @SpringBootTest
- Test database interactions
- Test external service integrations

## Property-Based Tests
- Test correctness properties
- Use jqwik or QuickTheories
- Focus on invariants and edge cases
EOF

# Add a README
cat > README.md << 'EOF'
# Banking Shared Kiro Configuration

This repository contains shared Kiro steering files and hooks used across all banking microservices.

## Contents

- `.kiro/steering/` - Organization-wide coding standards
- `.kiro/hooks/` - Organization-wide automation hooks

## Usage

This repository is included as a Git submodule in each microservice repository.

## Updating Standards

1. Make changes to steering files in this repository
2. Commit and push changes
3. Each service will pull the latest version when they update their submodule

## Services Using This Config

- login-service
- authentication-service
- channel-configurations-service
- gateway-service
- user-service
EOF

# Commit and push
git add .
git commit -m "Initial shared Kiro configuration"

# Add remote (replace with your actual Git URL)
git remote add origin https://github.com/your-org/banking-shared-kiro-config.git
git push -u origin main
```

---

### Step 2: Add Submodule to Each Service

Run these commands in **each service repository**:

#### For login-service:

```bash
cd login-service

# Add submodule
git submodule add https://github.com/your-org/banking-shared-kiro-config.git .kiro-shared

# Create symlinks to make Kiro find the files
# Windows (PowerShell - run as Administrator)
New-Item -ItemType SymbolicLink -Path ".kiro\steering" -Target ".kiro-shared\.kiro\steering"
New-Item -ItemType SymbolicLink -Path ".kiro\hooks" -Target ".kiro-shared\.kiro\hooks"

# Unix/Mac/Linux
ln -s ../.kiro-shared/.kiro/steering .kiro/steering
ln -s ../.kiro-shared/.kiro/hooks .kiro/hooks

# Commit
git add .gitmodules .kiro-shared .kiro/steering .kiro/hooks
git commit -m "Add shared Kiro config submodule"
git push
```

#### For authentication-service:

```bash
cd authentication-service

# Add submodule
git submodule add https://github.com/your-org/banking-shared-kiro-config.git .kiro-shared

# Create symlinks
# Windows (PowerShell - run as Administrator)
New-Item -ItemType SymbolicLink -Path ".kiro\steering" -Target ".kiro-shared\.kiro\steering"
New-Item -ItemType SymbolicLink -Path ".kiro\hooks" -Target ".kiro-shared\.kiro\hooks"

# Unix/Mac/Linux
ln -s ../.kiro-shared/.kiro/steering .kiro/steering
ln -s ../.kiro-shared/.kiro/hooks .kiro/hooks

# Commit
git add .gitmodules .kiro-shared .kiro/steering .kiro/hooks
git commit -m "Add shared Kiro config submodule"
git push
```

#### Repeat for all other services:
- channel-configurations-service
- gateway-service
- user-service

---

### Step 3: Directory Structure After Setup

Each service repository will look like this:

```
login-service/                          # Service repository
├── .kiro-shared/                       # Git submodule (shared config)
│   ├── .git                            # Submodule Git metadata
│   ├── .kiro/
│   │   ├── steering/
│   │   │   ├── architecture-standards.md
│   │   │   ├── spring-boot-standards.md
│   │   │   ├── security-standards.md
│   │   │   └── testing-standards.md
│   │   └── hooks/
│   │       └── lint-on-save.json
│   └── README.md
│
├── .kiro/
│   ├── steering/                       # Symlink → .kiro-shared/.kiro/steering
│   ├── hooks/                          # Symlink → .kiro-shared/.kiro/hooks
│   └── specs/                          # Service-specific specs
│       ├── .config.kiro
│       ├── requirements.md
│       ├── design.md
│       └── tasks.md
│
├── src/
├── pom.xml
└── README.md
```

---

## How It Works

### When You Clone a Service Repository

```bash
# Clone the service
git clone https://github.com/your-org/login-service.git
cd login-service

# Initialize and update submodules
git submodule init
git submodule update

# Now .kiro-shared/ contains the shared config
# And .kiro/steering/ and .kiro/hooks/ are symlinks to it
```

### When You Start Development

```bash
# Pull latest changes from service repo
git pull

# Update submodule to latest shared config
git submodule update --remote

# Now you have the latest shared standards
# Kiro will read from .kiro/steering/ (which points to .kiro-shared/.kiro/steering/)
```

### When Shared Config Changes

```bash
# Someone updated the shared config repository
# You need to pull the latest version

cd login-service

# Update submodule to latest
git submodule update --remote

# Commit the submodule update
git add .kiro-shared
git commit -m "Update shared Kiro config to latest version"
git push
```

---

## Automated Sync Script

Create a script to sync shared config across all services automatically.

### sync-kiro-config.sh (Unix/Mac/Linux)

```bash
#!/bin/bash

# List of service repositories
SERVICES=(
  "login-service"
  "authentication-service"
  "channel-configurations-service"
  "gateway-service"
  "user-service"
)

# Base directory where all service repos are cloned
BASE_DIR="$HOME/projects/banking"

echo "Syncing shared Kiro config across all services..."

for service in "${SERVICES[@]}"; do
  SERVICE_DIR="$BASE_DIR/$service"
  
  if [ -d "$SERVICE_DIR" ]; then
    echo ""
    echo "📦 Updating $service..."
    cd "$SERVICE_DIR"
    
    # Pull latest service changes
    git pull
    
    # Update submodule to latest
    git submodule update --remote
    
    # Check if there are changes
    if git diff --quiet .kiro-shared; then
      echo "✓ $service already up to date"
    else
      echo "✓ $service updated with latest shared config"
      
      # Optionally auto-commit the submodule update
      # git add .kiro-shared
      # git commit -m "Update shared Kiro config to latest version"
      # git push
    fi
  else
    echo "⚠️  $service not found at $SERVICE_DIR"
  fi
done

echo ""
echo "✅ All services synced!"
```

### sync-kiro-config.bat (Windows)

```batch
@echo off
setlocal enabledelayedexpansion

REM List of service repositories
set SERVICES=login-service authentication-service channel-configurations-service gateway-service user-service

REM Base directory where all service repos are cloned
set BASE_DIR=C:\projects\banking

echo Syncing shared Kiro config across all services...

for %%s in (%SERVICES%) do (
  set SERVICE_DIR=%BASE_DIR%\%%s
  
  if exist "!SERVICE_DIR!" (
    echo.
    echo Updating %%s...
    cd /d "!SERVICE_DIR!"
    
    REM Pull latest service changes
    git pull
    
    REM Update submodule to latest
    git submodule update --remote
    
    echo ✓ %%s updated
  ) else (
    echo ⚠️  %%s not found at !SERVICE_DIR!
  )
)

echo.
echo ✅ All services synced!
pause
```

### Usage

```bash
# Make executable (Unix/Mac/Linux)
chmod +x sync-kiro-config.sh

# Run before starting development
./sync-kiro-config.sh

# Or on Windows
sync-kiro-config.bat
```

---

## Alternative: Copy Script (Simpler but Manual)

If symlinks don't work on Windows or you prefer a simpler approach:

### copy-kiro-config.sh

```bash
#!/bin/bash

# Shared config repository
SHARED_REPO="https://github.com/your-org/banking-shared-kiro-config.git"
TEMP_DIR="/tmp/banking-shared-kiro-config"

# Clone or update shared config
if [ -d "$TEMP_DIR" ]; then
  cd "$TEMP_DIR"
  git pull
else
  git clone "$SHARED_REPO" "$TEMP_DIR"
fi

# List of service repositories
SERVICES=(
  "login-service"
  "authentication-service"
  "channel-configurations-service"
  "gateway-service"
  "user-service"
)

BASE_DIR="$HOME/projects/banking"

echo "Copying shared Kiro config to all services..."

for service in "${SERVICES[@]}"; do
  SERVICE_DIR="$BASE_DIR/$service"
  
  if [ -d "$SERVICE_DIR" ]; then
    echo "Copying to $service..."
    
    # Create directories
    mkdir -p "$SERVICE_DIR/.kiro/steering"
    mkdir -p "$SERVICE_DIR/.kiro/hooks"
    
    # Copy files
    cp -r "$TEMP_DIR/.kiro/steering/"* "$SERVICE_DIR/.kiro/steering/"
    cp -r "$TEMP_DIR/.kiro/hooks/"* "$SERVICE_DIR/.kiro/hooks/"
    
    echo "✓ $service updated"
  fi
done

echo "✅ All services updated!"
```

---

## Best Practices

### 1. Update Submodule Regularly

```bash
# Before starting work each day
cd login-service
git submodule update --remote
```

### 2. Pin Submodule to Specific Commit (Optional)

```bash
# If you want stability, pin to a specific commit
cd .kiro-shared
git checkout <commit-hash>
cd ..
git add .kiro-shared
git commit -m "Pin shared config to stable version"
```

### 3. Use Pre-Commit Hook to Sync

Create `.git/hooks/pre-commit` in each service:

```bash
#!/bin/bash

# Update submodule before committing
git submodule update --remote --merge

# Add submodule changes if any
git add .kiro-shared
```

### 4. Document in Service README

Add to each service's README.md:

```markdown
## Shared Kiro Configuration

This service uses shared Kiro configuration from the `banking-shared-kiro-config` repository.

### Setup

```bash
# Clone with submodules
git clone --recurse-submodules https://github.com/your-org/login-service.git

# Or if already cloned
git submodule init
git submodule update
```

### Update Shared Config

```bash
# Pull latest shared standards
git submodule update --remote

# Commit the update
git add .kiro-shared
git commit -m "Update shared Kiro config"
git push
```
```

---

## Troubleshooting

### Issue: Submodule Not Initialized

**Symptom:** `.kiro-shared/` is empty

**Solution:**
```bash
git submodule init
git submodule update
```

### Issue: Symlinks Not Working on Windows

**Symptom:** `.kiro/steering/` shows as a file, not a directory

**Solution 1:** Enable Developer Mode in Windows Settings
- Settings → Update & Security → For Developers → Developer Mode

**Solution 2:** Use copy script instead of symlinks
- Run `copy-kiro-config.bat` before development

### Issue: Submodule Out of Date

**Symptom:** Kiro uses old standards

**Solution:**
```bash
cd login-service
git submodule update --remote
```

### Issue: Merge Conflicts in Submodule

**Symptom:** Git shows conflicts in `.kiro-shared/`

**Solution:**
```bash
cd .kiro-shared
git fetch origin
git reset --hard origin/main
cd ..
git add .kiro-shared
git commit -m "Reset submodule to latest"
```

---

## Comparison: Submodule vs Copy Script

| Feature | Git Submodule | Copy Script |
|---------|---------------|-------------|
| **Automatic sync** | ✅ Yes (with git commands) | ❌ Manual |
| **Version control** | ✅ Tracks specific commit | ❌ No tracking |
| **Ease of setup** | ⚠️ Moderate | ✅ Simple |
| **Windows compatibility** | ⚠️ Requires symlinks or Developer Mode | ✅ Works everywhere |
| **Team collaboration** | ✅ Everyone gets same version | ⚠️ Manual coordination |
| **Recommended for** | Production teams | Quick prototyping |

**Recommendation:** Use **Git Submodule** for production. It's the industry standard for sharing code/config across repos.

---

## Summary

For **separate repositories**, use **Git Submodules**:

1. Create `banking-shared-kiro-config` repository with shared steering/hooks
2. Add as submodule to each service: `git submodule add <url> .kiro-shared`
3. Create symlinks: `.kiro/steering` → `.kiro-shared/.kiro/steering`
4. Update regularly: `git submodule update --remote`
5. Kiro reads from `.kiro/steering/` (which points to shared config)

This ensures all services use the same standards, and updates propagate automatically!
