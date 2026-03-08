#!/bin/bash

# Sync All Banking Services with Latest Shared Config
# Run this before starting development each day

set -e  # Exit on error

# Configuration
BASE_DIR="$HOME/projects/banking"  # Adjust to your path
SERVICES=(
  "login-service"
  "authentication-service"
  "channel-configurations-service"
  "gateway-service"
  "user-service"
)

echo "🔄 Syncing all banking services with latest shared config..."
echo ""

# Track results
UPDATED=0
SKIPPED=0
ERRORS=0

for service in "${SERVICES[@]}"; do
  SERVICE_DIR="$BASE_DIR/$service"
  
  if [ ! -d "$SERVICE_DIR" ]; then
    echo "⚠️  $service: Not found at $SERVICE_DIR"
    ((ERRORS++))
    continue
  fi
  
  echo "📦 $service:"
  cd "$SERVICE_DIR"
  
  # Check if it's a git repo
  if [ ! -d ".git" ]; then
    echo "   ❌ Not a Git repository"
    ((ERRORS++))
    continue
  fi
  
  # Pull latest service changes
  echo "   → Pulling latest changes..."
  if git pull --quiet; then
    echo "   ✓ Service code updated"
  else
    echo "   ⚠️  Failed to pull (may have uncommitted changes)"
  fi
  
  # Check if submodule exists
  if [ ! -f ".gitmodules" ]; then
    echo "   ⚠️  No submodule configured (skipping)"
    ((SKIPPED++))
    continue
  fi
  
  # Initialize submodule if needed
  if [ ! -d ".kiro-shared/.git" ]; then
    echo "   → Initializing submodule..."
    git submodule init
    git submodule update
  fi
  
  # Update submodule to latest
  echo "   → Updating shared config..."
  BEFORE=$(cd .kiro-shared && git rev-parse HEAD)
  
  if git submodule update --remote --merge; then
    AFTER=$(cd .kiro-shared && git rev-parse HEAD)
    
    if [ "$BEFORE" != "$AFTER" ]; then
      echo "   ✓ Shared config updated ($(cd .kiro-shared && git log --oneline -1))"
      ((UPDATED++))
      
      # Optionally auto-commit the submodule update
      # Uncomment these lines if you want automatic commits
      # git add .kiro-shared
      # git commit -m "Update shared Kiro config to latest version"
      # echo "   ✓ Committed submodule update"
    else
      echo "   ✓ Already up to date"
    fi
  else
    echo "   ❌ Failed to update submodule"
    ((ERRORS++))
  fi
  
  echo ""
done

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Summary:"
echo "   ✅ Updated: $UPDATED service(s)"
echo "   ⏭️  Skipped: $SKIPPED service(s)"
if [ $ERRORS -gt 0 ]; then
  echo "   ❌ Errors: $ERRORS service(s)"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ $UPDATED -gt 0 ]; then
  echo "💡 Tip: Review the changes and commit the submodule updates:"
  echo "   cd <service-name>"
  echo "   git add .kiro-shared"
  echo "   git commit -m 'Update shared Kiro config'"
  echo "   git push"
fi

echo ""
echo "✅ All services synced! Ready to start development."
