@echo off
echo ========================================
echo Stopping All Banking Platform Services
echo ========================================
echo.

echo Stopping Backend Services (Spring Boot on ports 8080, 8081, 8082)...
echo.

REM Stop Authentication Service (port 8081)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    echo Stopping Authentication Service (PID: %%a)
    taskkill /F /PID %%a > nul 2>&1
)

REM Stop Login Service (port 8080)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Stopping Login Service (PID: %%a)
    taskkill /F /PID %%a > nul 2>&1
)

REM Stop Channel Configurations Service (port 8082)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8082 ^| findstr LISTENING') do (
    echo Stopping Channel Configurations Service (PID: %%a)
    taskkill /F /PID %%a > nul 2>&1
)

echo.
echo Stopping Frontend Services (Node.js on ports 3000, 3001, 3002)...
echo.

REM Stop Host App (port 3000)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3000 ^| findstr LISTENING') do (
    echo Stopping Host App (PID: %%a)
    taskkill /F /PID %%a > nul 2>&1
)

REM Stop Login MFE (port 3001)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3001 ^| findstr LISTENING') do (
    echo Stopping Login MFE (PID: %%a)
    taskkill /F /PID %%a > nul 2>&1
)

REM Stop Dashboard MFE (port 3002)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3002 ^| findstr LISTENING') do (
    echo Stopping Dashboard MFE (PID: %%a)
    taskkill /F /PID %%a > nul 2>&1
)

echo.
echo Cleaning up any remaining Node.js and Java processes...
echo.

REM Kill any remaining cmd windows that were started by start-all.bat
taskkill /FI "WindowTitle eq Authentication Service (8081)*" /F > nul 2>&1
taskkill /FI "WindowTitle eq Login Service (8080)*" /F > nul 2>&1
taskkill /FI "WindowTitle eq Channel Configurations Service (8082)*" /F > nul 2>&1
taskkill /FI "WindowTitle eq Login MFE (3001)*" /F > nul 2>&1
taskkill /FI "WindowTitle eq Dashboard MFE (3002)*" /F > nul 2>&1
taskkill /FI "WindowTitle eq Host App (3000)*" /F > nul 2>&1

echo.
echo ========================================
echo All services stopped successfully!
echo ========================================
echo.
echo You can verify by checking:
echo - No processes listening on ports 8080, 8081, 8082, 3000, 3001, 3002
echo - Run: netstat -ano ^| findstr "8080 8081 8082 3000 3001 3002"
echo.
echo Press any key to exit...
pause > nul
