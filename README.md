# Banking Prototype

Multi-entity banking platform with microservices architecture and micro-frontend.

## 🚀 Quick Start

**Want to get started immediately?** See the [Quick Start Guide](QUICK-START.md) for step-by-step instructions to run the entire platform in 5 minutes.

## Services

### Frontend
- **Host App** (port 3000): Main container application orchestrating all MFEs
- **Login MFE** (port 3001): React micro-frontend for login using Webpack 5 Module Federation
- **Dashboard MFE** (port 3002): React micro-frontend for post-login dashboard with cheque viewer access

### Backend
- **Login Service** (port 8080): User authentication with multi-entity context
- **Authentication Service** (port 8081): JWT token generation with RS256
- **Channel Configurations Service** (port 8082): Feature flag service with ACL support

## Quick Start

### Start Frontend
```bash
# Terminal 1 - Login MFE
cd login-mfe
npm install
npm start

# Terminal 2 - Dashboard MFE
cd dashboard-mfe
npm install
npm start

# Terminal 3 - Host App (Main Application)
cd host-app
npm install
npm start
```
Access Main Application at: **http://localhost:3000**
(Login MFE: http://localhost:3001, Dashboard MFE: http://localhost:3002)

### Start Backend Services
```bash
# Terminal 1 - Authentication Service
cd authentication-service
mvn spring-boot:run

# Terminal 2 - Login Service  
cd login-service
mvn spring-boot:run

# Terminal 3 - Channel Configurations Service
cd channel-configurations-service
mvn spring-boot:run
```

## Architecture

- **Frontend**: React 18 with Webpack 5 Module Federation
- **Backend**: Spring Boot 3.4 with Hexagonal Architecture
- **Security**: BCrypt password hashing, RS256 JWT tokens
- **Communication**: REST APIs, WebClient for inter-service calls

## Testing the Integration

### Test Users (H2 Database)

The login-service uses H2 in-memory database with pre-loaded test users:
- **Username**: testuser, 1119test1, 1119test2, 1119test3, adminuser, demouser
- **Password**: password123 (for all users)
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:logindb`, Username: `sa`, Password: empty)

For complete test user details, see [Login Service Test Users](login-service/TEST-USERS.md)

### Test Complete Flow via Host App
1. Open http://localhost:3000 (Host App)
2. You'll see the Login MFE
3. Fill in the login form:
   - Bank Code: 101
   - Branch Code: 1119
   - Username: testuser
   - Password: password123
   - Currency: SGD
4. Click Login
5. Dashboard MFE will appear with your user information
6. Click "View Archived Cheques" (placeholder)
7. Click "Logout" to return to login

### Test Login via Frontend Directly
1. Open http://localhost:3001
2. Fill in the login form:
   - Bank Code: 101
   - Branch Code: 1119
   - Username: testuser
   - Password: password123
   - Currency: SGD
3. Click Login

### Test Authentication Service directly:
```bash
curl -X POST http://localhost:8081/api/v1/auth/generate-token \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "bankCode": "101",
    "branchCode": "1119",
    "currency": "SGD"
  }'
```

### Test Login Service API:
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

## Documentation

- **[Quick Start Guide](QUICK-START.md)** - Get the entire platform running in 5 minutes
- **[Login Service Test Users](login-service/TEST-USERS.md)** - Test user credentials and H2 database access
- [Host App README](host-app/README.md) - Main container application setup and usage
- [Frontend-Backend Integration Guide](FRONTEND-BACKEND-SETUP.md) - Complete setup guide
- [Login MFE README](login-mfe/README.md) - Login micro-frontend setup and usage
- [Dashboard MFE README](dashboard-mfe/README.md) - Dashboard micro-frontend setup and usage
- [Login Service README](login-service/README.md) - Backend authentication API
- [Authentication Service README](authentication-service/README.md) - JWT token generation
- [Swagger Setup Guide](SWAGGER-SETUP.md) - API documentation setup
- [Sequence Diagrams](sequence-diagrams/banking-services-flow.md) - System flow diagrams

## API Documentation

- Login Service Swagger: http://localhost:8080/swagger-ui.html
- Authentication Service Swagger: http://localhost:8081/swagger-ui.html
- Channel Configurations Service Swagger: http://localhost:8082/swagger-ui.html

## Feature Flags & ACL

The Channel Configurations Service provides feature flag management with ACL (Access Control List) support:

### Testing Feature Flags
```bash
# Get feature flags for user 1119test1
curl -X GET http://localhost:8082/api/v1/feature-flags -H "USER_ID: 1119test1"
```

### H2 Database Console
Access the H2 console at: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:channeldb`
- Username: `sa`
- Password: (leave empty)

For detailed testing instructions, see [Channel Configurations Testing Guide](channel-configurations-service/TESTING-GUIDE.md)

## Module Federation

### Host App (Container)
- **Port**: 3000
- **Consumes**: Login MFE, Dashboard MFE
- **URL**: http://localhost:3000

### Login MFE (Remote)
- **Module**: `./LoginForm`
- **Remote Entry**: http://localhost:3001/remoteEntry.js

### Dashboard MFE (Remote)
- **Module**: `./Dashboard`
- **Remote Entry**: http://localhost:3002/remoteEntry.js

The Host app dynamically loads and orchestrates the Login and Dashboard MFEs, managing authentication state and navigation between them.
