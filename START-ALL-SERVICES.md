# Quick Start Guide - All Services

## Prerequisites

Before starting, ensure you have:
- Java 17+ installed
- Maven installed
- Node.js 16+ and npm installed
- All dependencies installed (`npm install` in each frontend folder)

## Starting All Services (Windows)

You need to open **6 separate terminals** (Command Prompt, PowerShell, or Git Bash).

### Terminal 1: Authentication Service
```bash
cd authentication-service
mvn spring-boot:run
```
Wait for: `Started AuthenticationServiceApplication`

### Terminal 2: Login Service
```bash
cd login-service
mvn spring-boot:run
```
Wait for: `Started LoginServiceApplication`

### Terminal 3: Channel Configurations Service
```bash
cd channel-configurations-service
mvn spring-boot:run
```
Wait for: `Started ChannelConfigurationsServiceApplication`

### Terminal 4: Login MFE
```bash
cd login-mfe
npm start
```
Wait for: `webpack compiled successfully`

### Terminal 5: Dashboard MFE
```bash
cd dashboard-mfe
npm start
```
Wait for: `webpack compiled successfully`

### Terminal 6: Host App
```bash
cd host-app
npm start
```
Wait for: `webpack compiled successfully`

## Verification Steps

### 1. Check Backend Services

Open browser and verify these endpoints return data:

- http://localhost:8080/actuator/health (Login Service)
- http://localhost:8081/actuator/health (Authentication Service)
- http://localhost:8082/actuator/health (Channel Configurations Service)

### 2. Check Frontend Remote Entries

Verify these URLs are accessible (should download JavaScript):

- http://localhost:3001/remoteEntry.js (Login MFE)
- http://localhost:3002/remoteEntry.js (Dashboard MFE)

### 3. Access Host App

Open browser and navigate to:
- http://localhost:3000

You should see the Login Form (no errors in console).

## Testing the Complete Flow

### Step 1: Login
1. Go to http://localhost:3000
2. Enter credentials:
   - Bank Code: `101`
   - Branch Code: `1119`
   - Username: `testuser`
   - Password: `password123`
   - Currency: `SGD`
3. Click "Login"

### Step 2: Verify Dashboard
After successful login, you should see:
- Dashboard with user information
- "View Archived Images" button (testuser has ACL 1000)
- No "View Reports" button (testuser doesn't have ACL 1001)

### Step 3: Test Different Users

Try logging in with different users to see different feature flags:

| Username   | Password    | Features Visible                    |
|------------|-------------|-------------------------------------|
| testuser   | password123 | Archive Enquiry only                |
| 1119test1  | password123 | Archive Enquiry only                |
| 1119test2  | password123 | Reports only                        |
| 1119test3  | password123 | Both Archive Enquiry and Reports    |

## Troubleshooting

### Issue: "Script error" at localhost:3000

**Solution**: 
1. Verify all 6 services are running
2. Check http://localhost:3001/remoteEntry.js is accessible
3. Check http://localhost:3002/remoteEntry.js is accessible
4. Clear browser cache (Ctrl+Shift+Delete)
5. Hard refresh (Ctrl+F5)

See `MODULE-FEDERATION-TROUBLESHOOTING.md` for detailed troubleshooting.

### Issue: Port already in use

**Solution**:
```bash
# Find process using port (e.g., 3000)
netstat -ano | findstr :3000

# Kill process by PID
taskkill /PID <PID> /F
```

### Issue: Backend service won't start

**Solution**:
1. Check if port is already in use
2. Verify Java and Maven are installed: `java -version` and `mvn -version`
3. Clean and rebuild: `mvn clean install`

### Issue: Frontend won't compile

**Solution**:
1. Delete node_modules and package-lock.json
2. Run `npm install`
3. Try `npm start` again

## Stopping All Services

Press `Ctrl+C` in each terminal to stop the services.

## Service Ports Reference

| Service                          | Port | URL                          |
|----------------------------------|------|------------------------------|
| Authentication Service           | 8081 | http://localhost:8081        |
| Login Service                    | 8080 | http://localhost:8080        |
| Channel Configurations Service   | 8082 | http://localhost:8082        |
| Login MFE                        | 3001 | http://localhost:3001        |
| Dashboard MFE                    | 3002 | http://localhost:3002        |
| Host App (Main Entry Point)      | 3000 | http://localhost:3000        |

## Important Notes

1. **Always use localhost:3000** as the main entry point
2. **Do NOT use localhost:3001 directly** - it's for standalone testing only
3. **Start backend services first**, then frontend services
4. **Wait for each service to fully start** before starting the next one
5. **Clear browser cache** if you see Module Federation errors

## API Documentation

Once services are running, access Swagger UI:

- Login Service: http://localhost:8080/swagger-ui.html
- Authentication Service: http://localhost:8081/swagger-ui.html
- Channel Configurations Service: http://localhost:8082/swagger-ui.html

## H2 Database Consoles

Access H2 consoles for testing:

- Login Service: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:logindb`
  - Username: `sa`
  - Password: (leave empty)

- Channel Configurations Service: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:channelconfigdb`
  - Username: `sa`
  - Password: (leave empty)
