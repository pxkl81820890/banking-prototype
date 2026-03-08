@echo off
echo ========================================
echo Banking Platform Setup Verification
echo ========================================
echo.

echo Checking if services are running...
echo.

echo [1/6] Checking Authentication Service (8081)...
curl -s http://localhost:8081/actuator/health > nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Authentication Service is running
) else (
    echo [FAIL] Authentication Service is NOT running
)

echo.
echo [2/6] Checking Login Service (8080)...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Login Service is running
) else (
    echo [FAIL] Login Service is NOT running
)

echo.
echo [3/6] Checking Channel Configurations Service (8082)...
curl -s http://localhost:8082/actuator/health > nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Channel Configurations Service is running
) else (
    echo [FAIL] Channel Configurations Service is NOT running
)

echo.
echo [4/6] Checking Login MFE remoteEntry.js (3001)...
curl -s http://localhost:3001/remoteEntry.js > nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Login MFE remoteEntry.js is accessible
) else (
    echo [FAIL] Login MFE remoteEntry.js is NOT accessible
    echo        Make sure login-mfe is running: cd login-mfe ^&^& npm start
)

echo.
echo [5/6] Checking Dashboard MFE remoteEntry.js (3002)...
curl -s http://localhost:3002/remoteEntry.js > nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Dashboard MFE remoteEntry.js is accessible
) else (
    echo [FAIL] Dashboard MFE remoteEntry.js is NOT accessible
    echo        Make sure dashboard-mfe is running: cd dashboard-mfe ^&^& npm start
)

echo.
echo [6/6] Checking Host App (3000)...
curl -s http://localhost:3000 > nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Host App is running
) else (
    echo [FAIL] Host App is NOT running
    echo        Make sure host-app is running: cd host-app ^&^& npm start
)

echo.
echo ========================================
echo Verification Complete
echo ========================================
echo.
echo If all checks passed, open: http://localhost:3000
echo If any checks failed, start those services first.
echo.
echo See FIX-SCRIPT-ERROR.md for detailed troubleshooting.
echo.
pause
