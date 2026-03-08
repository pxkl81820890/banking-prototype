@echo off
setlocal enabledelayedexpansion

REM Sync All Banking Services with Latest Shared Config
REM Run this before starting development each day

echo Syncing all banking services with latest shared config...
echo.

REM Configuration
set BASE_DIR=C:\projects\banking
set SERVICES=login-service authentication-service channel-configurations-service gateway-service user-service

REM Track results
set UPDATED=0
set SKIPPED=0
set ERRORS=0

for %%s in (%SERVICES%) do (
  set SERVICE_DIR=%BASE_DIR%\%%s
  
  if not exist "!SERVICE_DIR!" (
    echo ⚠️  %%s: Not found at !SERVICE_DIR!
    set /a ERRORS+=1
    goto :continue
  )
  
  echo 📦 %%s:
  cd /d "!SERVICE_DIR!"
  
  REM Check if it's a git repo
  if not exist ".git" (
    echo    ❌ Not a Git repository
    set /a ERRORS+=1
    goto :continue
  )
  
  REM Pull latest service changes
  echo    → Pulling latest changes...
  git pull >nul 2>&1
  if !errorlevel! equ 0 (
    echo    ✓ Service code updated
  ) else (
    echo    ⚠️  Failed to pull ^(may have uncommitted changes^)
  )
  
  REM Check if submodule exists
  if not exist ".gitmodules" (
    echo    ⚠️  No submodule configured ^(skipping^)
    set /a SKIPPED+=1
    goto :continue
  )
  
  REM Initialize submodule if needed
  if not exist ".kiro-shared\.git" (
    echo    → Initializing submodule...
    git submodule init
    git submodule update
  )
  
  REM Update submodule to latest
  echo    → Updating shared config...
  git submodule update --remote --merge >nul 2>&1
  if !errorlevel! equ 0 (
    echo    ✓ Shared config updated
    set /a UPDATED+=1
  ) else (
    echo    ❌ Failed to update submodule
    set /a ERRORS+=1
  )
  
  :continue
  echo.
)

REM Summary
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 📊 Summary:
echo    ✅ Updated: %UPDATED% service^(s^)
echo    ⏭️  Skipped: %SKIPPED% service^(s^)
if %ERRORS% gtr 0 (
  echo    ❌ Errors: %ERRORS% service^(s^)
)
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

if %UPDATED% gtr 0 (
  echo 💡 Tip: Review the changes and commit the submodule updates:
  echo    cd ^<service-name^>
  echo    git add .kiro-shared
  echo    git commit -m "Update shared Kiro config"
  echo    git push
)

echo.
echo ✅ All services synced! Ready to start development.
pause
