# Quick Start Guide - Banking Platform

Complete guide to get the entire banking platform running in 5 minutes.

## Prerequisites

- Node.js 18+ and npm
- Java 21
- Maven 3.8+
- PostgreSQL (optional, for full functionality)

## Step-by-Step Setup

### 1. Start Backend Services

**Terminal 1 - Authentication Service:**
```bash
cd authentication-service
mvn spring-boot:run
```
Wait for: `Started AuthenticationServiceApplication`

**Terminal 2 - Login Service:**
```bash
cd login-service
mvn spring-boot:run
```
Wait for: `Started LoginServiceApplication`

### 2. Start Frontend MFEs

**Terminal 3 - Login MFE:**
```bash
cd login-mfe
npm install  # First time only
npm start
```
Wait for: `webpack compiled successfully`

**Terminal 4 - Dashboard MFE:**
```bash
cd dashboard-mfe
npm install  # First time only
npm start
```
Wait for: `webpack compiled successfully`

**Terminal 5 - Host App:**
```bash
cd host-app
npm install  # First time only
npm start
```
Wait for: `webpack compiled successfully`

### 3. Access the Application

Open your browser and go to: **http://localhost:3000**

## Test Login

Use these test credentials:

```
Bank Code: 101
Branch Code: 1119
Username: testuser
Password: password123
Currency: SGD
```

Click **Login** → You'll be redirected to the Dashboard!

## What You'll See

1. **Login Page** (Login MFE)
   - Clean form with validation
   - Bank code, branch code, username, password, currency fields

2. **Dashboard** (Dashboard MFE after login)
   - Welcome message with username
   - Account summary with user details
   - "View Archived Cheques" button
   - "Logout" button
   - Quick actions section

## Port Reference

| Service | Port | URL |
|---------|------|-----|
| Host App | 3000 | http://localhost:3000 |
| Login MFE | 3001 | http://localhost:3001 |
| Dashboard MFE | 3002 | http://localhost:3002 |
| Login Service | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |

## API Documentation

- Login Service: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:8081/swagger-ui.html

## Troubleshooting

### Backend Not Starting
```bash
# Check if ports are in use
netstat -ano | findstr :8080
netstat -ano | findstr :8081

# Kill process if needed (Windows)
taskkill /PID <PID> /F
```

### Frontend Not Loading
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
npm start
```

### CORS Errors
- Ensure all services are running
- Check browser console for specific error
- Verify backend WebConfig allows http://localhost:3000

### Login Not Working
1. Check backend logs for errors
2. Verify database is running (if using real DB)
3. Test API directly with curl:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "bankCode": "101",
    "branchCode": "1119",
    "username": "testuser",
    "password": "password123",
    "currency": "SGD"
  }'
```

### Dashboard Not Showing After Login
1. Open browser DevTools (F12)
2. Check Console for errors
3. Check Application → Local Storage for:
   - authToken
   - userId
   - username
   - bankCode
   - branchCode
   - currency
4. If missing, login again

## Stopping Services

Press `Ctrl+C` in each terminal to stop the services.

## Next Steps

- Explore the Swagger UI for API documentation
- Check the sequence diagrams in `sequence-diagrams/`
- Read individual service READMEs for more details
- Try the logout functionality
- Explore the codebase structure

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                  Browser (Port 3000)                │
│  ┌───────────────────────────────────────────────┐  │
│  │            Host Application                   │  │
│  │  ┌──────────────┐      ┌──────────────┐      │  │
│  │  │  Login MFE   │      │Dashboard MFE │      │  │
│  │  │  (Port 3001) │      │ (Port 3002)  │      │  │
│  │  └──────────────┘      └──────────────┘      │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                        │
                        │ HTTP/REST
                        ▼
┌─────────────────────────────────────────────────────┐
│              Backend Services                       │
│  ┌──────────────┐      ┌──────────────┐            │
│  │Login Service │─────▶│Auth Service  │            │
│  │ (Port 8080)  │      │ (Port 8081)  │            │
│  └──────────────┘      └──────────────┘            │
└─────────────────────────────────────────────────────┘
```

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the detailed READMEs in each service folder
3. Check the SWAGGER-SETUP.md for API documentation issues
4. Review FRONTEND-BACKEND-SETUP.md for integration issues
