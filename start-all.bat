@echo off
echo ========================================
echo Starting All Banking Platform Services
echo ========================================
echo.
echo This will open 6 terminal windows.
echo Please wait for each service to start completely.
echo.
echo Press any key to continue...
pause > nul

echo.
echo Starting Backend Services...
echo.

start "Authentication Service (8081)" cmd /k "cd authentication-service && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

start "Login Service (8080)" cmd /k "cd login-service && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

start "Channel Configurations Service (8082)" cmd /k "cd channel-configurations-service && mvn spring-boot:run"
timeout /t 10 /nobreak > nul

echo.
echo Starting Frontend Services...
echo.

start "Login MFE (3001)" cmd /k "cd login-mfe && npm start"
timeout /t 5 /nobreak > nul

start "Dashboard MFE (3002)" cmd /k "cd dashboard-mfe && npm start"
timeout /t 5 /nobreak > nul

start "Host App (3000)" cmd /k "cd host-app && npm start"

echo.
echo ========================================
echo All services are starting!
echo ========================================
echo.
echo Wait for all services to fully start, then:
echo 1. Verify remoteEntry.js files are accessible:
echo    - http://localhost:3001/remoteEntry.js
echo    - http://localhost:3002/remoteEntry.js
echo.
echo 2. Open your browser and navigate to:
echo    http://localhost:3000
echo.
echo 3. Login with:
echo    Bank Code: 101
echo    Branch Code: 1119
echo    Username: testuser
echo    Password: password123
echo    Currency: SGD
echo.
echo Press any key to exit this window...
pause > nul
