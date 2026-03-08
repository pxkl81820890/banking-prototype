# Which Sync Approach Should I Use?

## Quick Answer

**For production teams:** Use **Git Submodule** + **sync-all-services script**

**For quick prototyping:** Use **copy-kiro-config script**

---

## Detailed Comparison

### Option 1: Git Submodule (Recommended) ✅

**Setup:**
```bash
# One-time setup per service
cd login-service
git submodule add https://github.com/your-org/banking-shared-kiro-config.git .kiro-shared
ln -s ../.kiro-shared/.kiro/steering .kiro/steering
git add .gitmodules .kiro-shared .kiro/steering
git commit -m "Add shared Kiro config submodule"
```

**Daily usage:**
```bash
# Option A: Manual (per service)
cd login-service
git submodule update --remote

# Option B: Automated (all services)
./sync-all-services.sh  # or .bat on Windows
```

**Pros:**
- ✅ **Version controlled** - Git tracks which version each service uses
- ✅ **Team coordination** - Everyone sees submodule updates in pull requests
- ✅ **Rollback capability** - Can pin to specific commit if needed
- ✅ **Industry standard** - Used by major projects (Linux kernel, etc.)
- ✅ **Automatic detection** - Git shows when submodule is out of date

**Cons:**
- ⚠️ **Learning curve** - Team needs to understand Git submodules
- ⚠️ **Symlinks on Windows** - May require Developer Mode or admin rights
- ⚠️ **Extra commands** - Need to run `git submodule update`

**When to use:**
- Production environments
- Teams of 3+ developers
- When you need version control of standards
- When you want Git to track which version each service uses

---

### Option 2: Copy Script (Simpler) ⚠️

**Setup:**
```bash
# One-time: Create the script (already provided)
chmod +x copy-kiro-config.sh
```

**Daily usage:**
```bash
# Before starting development
./copy-kiro-config.sh

# After updating shared config
./copy-kiro-config.sh
```

**Pros:**
- ✅ **Simple** - Easy to understand, just copies files
- ✅ **No symlinks** - Works on all Windows versions
- ✅ **No Git submodules** - No need to learn submodule commands
- ✅ **Works everywhere** - No special permissions needed

**Cons:**
- ❌ **Not version controlled** - Git doesn't track which version
- ❌ **Manual coordination** - Team must remember to run script
- ❌ **No rollback** - Always uses latest (can't pin to specific version)
- ❌ **File duplication** - Copies files into each service repo
- ❌ **Merge conflicts** - If someone edits copied files locally

**When to use:**
- Quick prototyping
- Solo developer
- Windows environment with symlink issues
- When simplicity is more important than version control

---

## Recommended Workflow

### For Production Teams

Use **Git Submodule + Automated Sync Script**:

```bash
# 1. One-time setup (per service)
cd login-service
git submodule add <shared-config-url> .kiro-shared
ln -s ../.kiro-shared/.kiro/steering .kiro/steering
git commit -m "Add shared Kiro config"

# 2. Daily workflow
./sync-all-services.sh  # Syncs all services at once

# 3. When shared config changes
# (Script automatically pulls latest)

# 4. Commit submodule updates
cd login-service
git add .kiro-shared
git commit -m "Update shared Kiro config"
git push
```

**Benefits:**
- One command syncs all services
- Git tracks which version each service uses
- Team sees updates in pull requests
- Can rollback if needed

---

## When to Run Sync Scripts

### Git Submodule Approach

**Run `sync-all-services.sh` (or `.bat`):**

1. ✅ **Every morning** before starting work
   ```bash
   ./sync-all-services.sh
   ```

2. ✅ **After someone updates shared config**
   - You'll see a notification in Slack/email
   - Run the script to get latest

3. ✅ **After pulling service changes**
   - If someone else updated the submodule
   - Script will sync to their version

4. ✅ **When switching branches**
   - Different branches may use different config versions
   - Script ensures you have the right version

**Don't need to run:**
- ❌ After every commit (only when submodule changes)
- ❌ Multiple times per day (once in morning is enough)

---

### Copy Script Approach

**Run `copy-kiro-config.sh`:**

1. ✅ **Every morning** before starting work
   ```bash
   ./copy-kiro-config.sh
   ```

2. ✅ **After YOU update shared config**
   ```bash
   # Update shared config repo
   cd banking-shared-kiro-config
   git pull
   git add .
   git commit -m "Update security standards"
   git push
   
   # Copy to all services
   cd ..
   ./copy-kiro-config.sh
   ```

3. ✅ **After SOMEONE ELSE updates shared config**
   - Check Slack/email for notifications
   - Run script to get latest

4. ✅ **When you notice outdated standards**
   - Kiro generates code that doesn't match latest standards
   - Run script to sync

**Don't need to run:**
- ❌ After every service commit
- ❌ When working on code (only when starting/switching services)

---

## Automation Ideas

### Auto-sync on Terminal Startup

Add to your `.bashrc` or `.zshrc`:

```bash
# Auto-sync Kiro config when opening terminal in a service directory
if [ -f ".gitmodules" ] && [ -d ".kiro-shared" ]; then
  echo "🔄 Syncing shared Kiro config..."
  git submodule update --remote --merge > /dev/null 2>&1
  echo "✅ Shared config synced!"
fi
```

### Pre-commit Hook

Create `.git/hooks/pre-commit` in each service:

```bash
#!/bin/bash
# Auto-sync submodule before committing
git submodule update --remote --merge
git add .kiro-shared
```

### VS Code Task

Add to `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Sync Kiro Config",
      "type": "shell",
      "command": "git submodule update --remote --merge",
      "problemMatcher": [],
      "group": "build"
    }
  ]
}
```

Run with: `Ctrl+Shift+P` → `Tasks: Run Task` → `Sync Kiro Config`

---

## Troubleshooting

### "I forgot to run the sync script"

**Symptom:** Kiro generates code that doesn't follow latest standards

**Solution:**
```bash
# Git Submodule
./sync-all-services.sh

# Copy Script
./copy-kiro-config.sh
```

### "My service uses old standards but others use new"

**Symptom:** Inconsistent code across services

**Solution:**
```bash
# Check which version each service uses
cd login-service && git submodule status
cd authentication-service && git submodule status

# Sync all to latest
./sync-all-services.sh
```

### "I updated shared config but services don't see it"

**Symptom:** Changed `architecture-standards.md` but Kiro still uses old version

**Solution:**
```bash
# Make sure you pushed the shared config changes
cd banking-shared-kiro-config
git push

# Sync all services
cd ..
./sync-all-services.sh  # or copy-kiro-config.sh
```

---

## Summary

| Feature | Git Submodule | Copy Script |
|---------|---------------|-------------|
| **Version control** | ✅ Yes | ❌ No |
| **Team coordination** | ✅ Automatic | ⚠️ Manual |
| **Rollback** | ✅ Yes | ❌ No |
| **Simplicity** | ⚠️ Moderate | ✅ Simple |
| **Windows compatibility** | ⚠️ Needs symlinks | ✅ Works everywhere |
| **When to sync** | Daily + after updates | Daily + after updates |
| **Recommended for** | Production teams | Quick prototyping |

**My recommendation:** Use **Git Submodule** with the **sync-all-services script** for the best balance of automation and control.

---

## Quick Start

### Git Submodule Approach

```bash
# 1. Setup (once per service)
cd login-service
git submodule add <url> .kiro-shared
ln -s ../.kiro-shared/.kiro/steering .kiro/steering

# 2. Daily usage
./sync-all-services.sh

# 3. That's it!
```

### Copy Script Approach

```bash
# 1. Setup (once)
chmod +x copy-kiro-config.sh

# 2. Daily usage
./copy-kiro-config.sh

# 3. That's it!
```

Both approaches work! Choose based on your team's needs.
