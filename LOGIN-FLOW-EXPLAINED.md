# Complete Login Flow Explanation

## Overview

Your banking platform uses a **microservices architecture** with **micro-frontends (MFEs)** orchestrated through **Module Federation**. The login flow involves multiple services working together to authenticate users, generate JWT tokens, and display personalized features based on access control.

## Architecture Components

### Frontend (Micro-Frontends)
1. **Host App** (localhost:3000) - Main orchestrator
2. **Login MFE** (localhost:3001) - Login form component
3. **Dashboard MFE** (localhost:3002) - Dashboard component

### Backend (Microservices)
1. **Login Service** (localhost:8080) - User authentication
2. **Authentication Service** (localhost:8081) - JWT token generation
3. **Channel Configurations Service** (localhost:8082) - Feature flags with ACL

## Step-by-Step Login Flow

### Phase 1: User Opens Application

```
User Browser
    ↓
http://localhost:3000 (Host App)
    ↓
Host App loads and checks localStorage for existing session
    ↓
No session found → Display Login Form (from login-mfe)
```

**What Happens:**
1. User navigates to `http://localhost:3000`
2. Host App's `App.jsx` checks `localStorage` for:
   - `authToken`
   - `userId`
   - `username`
   - `bankCode`
   - `branchCode`
   - `currency`
3. If no token found → `isLoggedIn = false`
4. Host App uses **Module Federation** to lazy load `LoginForm` from login-mfe
5. LoginForm component appears on screen

**Code Location:**
- `host-app/src/App.jsx` - Session check logic
- `host-app/webpack.config.js` - Module Federation config

---

### Phase 2: User Enters Credentials

```
User fills form:
    ├─ Bank Code: 101
    ├─ Branch Code: 1119
    ├─ Username: testuser
    ├─ Password: password123
    └─ Currency: SGD
    ↓
User clicks "Login" button
    ↓
LoginForm validates input (client-side)
```

**What Happens:**
1. User enters credentials in the login form
2. Client-side validation checks:
   - Bank Code: Must be 3 digits
   - Branch Code: Must be 4 digits
   - Username: At least 3 characters
   - Password: At least 8 characters
   - Currency: Required
3. If validation fails → Show error messages
4. If validation passes → Proceed to API call

**Code Location:**
- `login-mfe/src/components/LoginForm.jsx` - Form validation

---

### Phase 3: Login API Call

```
LoginForm (login-mfe)
    ↓
POST http://localhost:8080/api/v1/auth/login
    ↓
Request Body:
{
  "bankCode": "101",
  "branchCode": "1119",
  "username": "testuser",
  "password": "password123",
  "currency": "SGD"
}
```

**What Happens:**
1. LoginForm makes HTTP POST request to login-service
2. Request includes all form data as JSON
3. CORS headers allow cross-origin request from localhost:3001

**Code Location:**
- `login-mfe/src/components/LoginForm.jsx` - `handleSubmit()` function
- `login-service/src/main/java/com/banking/loginservice/infrastructure/config/WebConfig.java` - CORS config

---

### Phase 4: Login Service - User Validation

```
Login Service (Port 8080)
    ↓
LoginController receives request
    ↓
LoginDomainService.login()
    ↓
1. Find user in H2 database
   SELECT * FROM USERS 
   WHERE USER_ID = 'testuser' 
   AND BANK_CODE = '101' 
   AND BRANCH_CODE = '1119'
    ↓
2. Verify password using BCrypt
   BCryptPasswordEncoder.matches(
     "password123", 
     "$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG"
   )
    ↓
3. If valid → Proceed to token generation
   If invalid → Return error
```

**What Happens:**
1. **LoginController** receives the request
2. **LoginDomainService** validates credentials:
   - Queries H2 database for user with matching bankCode, branchCode, and userId
   - Uses BCrypt to compare password hash
   - Password: `password123`
   - Stored Hash: `$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG`
3. If credentials are invalid:
   - Returns `401 Unauthorized`
   - Error message: "Invalid credentials"
4. If credentials are valid:
   - Proceeds to call authentication-service

**Code Location:**
- `login-service/src/main/java/com/banking/loginservice/infrastructure/adapters/in/LoginController.java`
- `login-service/src/main/java/com/banking/loginservice/domain/service/LoginDomainService.java`
- `login-service/src/main/resources/data.sql` - Test users

---

### Phase 5: JWT Token Generation

```
Login Service
    ↓
Calls Authentication Service
    ↓
POST http://localhost:8081/api/v1/auth/generate-token
    ↓
Request Body:
{
  "userId": "testuser",
  "bankCode": "101",
  "branchCode": "1119",
  "currency": "SGD"
}
    ↓
Authentication Service
    ↓
TokenGenerationService.generateToken()
    ↓
1. Load RSA private key from resources/keys/private_key.pem
2. Create JWT with claims:
   - sub: "testuser"
   - bankCode: "101"
   - branchCode: "1119"
   - currency: "SGD"
   - iat: current timestamp
   - exp: current timestamp + 1 hour
3. Sign JWT with RSA-256 algorithm
4. Return signed token
```

**What Happens:**
1. **Login Service** calls **Authentication Service** via HTTP
2. **AuthenticationServiceAdapter** makes the request
3. **Authentication Service** receives request at **AuthController**
4. **TokenGenerationService** generates JWT:
   - Uses **RSA-256** algorithm (asymmetric encryption)
   - Private key signs the token
   - Public key can verify the token (for other services)
   - Token expires in 1 hour
5. Returns JWT token to login-service

**JWT Token Structure:**
```
Header:
{
  "alg": "RS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "testuser",
  "bankCode": "101",
  "branchCode": "1119",
  "currency": "SGD",
  "iat": 1709388000,
  "exp": 1709391600
}

Signature:
RSASHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  privateKey
)
```

**Code Location:**
- `login-service/src/main/java/com/banking/loginservice/infrastructure/adapters/out/AuthenticationServiceAdapter.java`
- `authentication-service/src/main/java/com/banking/authservice/infrastructure/adapters/in/AuthController.java`
- `authentication-service/src/main/java/com/banking/authservice/domain/service/TokenGenerationService.java`
- `authentication-service/src/main/java/com/banking/authservice/infrastructure/security/JwtTokenProvider.java`

---

### Phase 6: Login Response

```
Authentication Service
    ↓
Returns JWT token to Login Service
    ↓
Login Service
    ↓
Returns response to LoginForm
    ↓
Response Body:
{
  "success": true,
  "message": "Login successful",
  "userId": "testuser",
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**What Happens:**
1. Login Service receives JWT from authentication-service
2. Login Service constructs response with:
   - `success: true`
   - `message: "Login successful"`
   - `userId: "testuser"`
   - `token: "eyJ..."`
3. Returns `200 OK` to LoginForm

**Code Location:**
- `login-service/src/main/java/com/banking/loginservice/domain/service/LoginDomainService.java`

---

### Phase 7: Frontend Session Management

```
LoginForm receives response
    ↓
Stores data in localStorage:
    ├─ authToken: "eyJ..."
    ├─ userId: "testuser"
    ├─ username: "testuser"
    ├─ bankCode: "101"
    ├─ branchCode: "1119"
    └─ currency: "SGD"
    ↓
Calls onLoginSuccess() callback
    ↓
Host App receives callback
    ↓
Updates state:
    ├─ isLoggedIn = true
    ├─ userData = { userId, username, bankCode, branchCode, currency }
    └─ token = "eyJ..."
    ↓
React re-renders
    ↓
Displays Dashboard (from dashboard-mfe)
```

**What Happens:**
1. **LoginForm** stores authentication data in `localStorage`
2. **LoginForm** calls `onLoginSuccess()` prop with response data
3. **Host App** receives callback and updates React state
4. React detects state change: `isLoggedIn` changed from `false` to `true`
5. Host App conditionally renders Dashboard instead of LoginForm
6. Module Federation lazy loads Dashboard component from dashboard-mfe

**Code Location:**
- `login-mfe/src/components/LoginForm.jsx` - `handleSubmit()` success handler
- `host-app/src/App.jsx` - `handleLoginSuccess()` function

---

### Phase 8: Dashboard Loads

```
Host App
    ↓
Lazy loads Dashboard from dashboard-mfe
    ↓
Dashboard component mounts
    ↓
useEffect() hook triggers
    ↓
Fetches feature flags
```

**What Happens:**
1. Host App uses Module Federation to load Dashboard component
2. Dashboard receives props:
   - `user`: { userId, username, bankCode, branchCode, currency }
   - `token`: JWT token
   - `onLogout`: Logout handler function
3. Dashboard component mounts and renders
4. `useEffect()` hook runs on mount

**Code Location:**
- `host-app/src/App.jsx` - Dashboard rendering
- `dashboard-mfe/src/components/Dashboard.jsx` - Component mount

---

### Phase 9: Feature Flags API Call

```
Dashboard (dashboard-mfe)
    ↓
GET http://localhost:8082/api/v1/feature-flags
    ↓
Request Headers:
{
  "USER_ID": "testuser"
}
    ↓
Channel Configurations Service
    ↓
FeatureFlagService.getFeatureFlagsForUser("testuser")
    ↓
1. Query MASTER table for all feature flags
2. For each feature flag:
   - If IS_ACL_ENABLED = false → enabled for all users
   - If IS_ACL_ENABLED = true → check ACL_CONFIG table
3. Query ACL_CONFIG for user's ACL IDs
4. Match feature flag ACL_ID with user's ACL IDs
5. Return enabled/disabled status for each feature
```

**What Happens:**
1. **Dashboard** makes GET request to channel-configurations-service
2. Passes `USER_ID` in request header
3. **FeatureFlagController** receives request
4. **FeatureFlagService** queries H2 database:

**SQL Logic:**
```sql
-- Get all feature flags
SELECT * FROM MASTER;

-- Get user's ACL IDs
SELECT ACL_ID FROM ACL_CONFIG WHERE USER_ID = 'testuser';

-- For testuser, returns ACL_ID = 1000

-- Match feature flags:
-- isArchiveEnquiryEnabled (ACL 1000) → User has ACL 1000 → ENABLED
-- isReportsEnabled (ACL 1001) → User doesn't have ACL 1001 → DISABLED
```

**Response:**
```json
{
  "userId": "testuser",
  "featureFlags": {
    "isArchiveEnquiryEnabled": true,
    "isReportsEnabled": false
  }
}
```

**Code Location:**
- `dashboard-mfe/src/components/Dashboard.jsx` - `fetchFeatureFlags()` function
- `channel-configurations-service/src/main/java/com/banking/channelconfig/infrastructure/adapters/in/FeatureFlagController.java`
- `channel-configurations-service/src/main/java/com/banking/channelconfig/domain/service/FeatureFlagService.java`
- `channel-configurations-service/src/main/resources/data.sql` - Feature flags and ACL data

---

### Phase 10: Dashboard Renders Features

```
Dashboard receives feature flags response
    ↓
Updates state:
    ├─ featureFlags = { isArchiveEnquiryEnabled: true, isReportsEnabled: false }
    └─ loading = false
    ↓
React re-renders
    ↓
Conditionally displays features:
    ├─ Account Summary (always visible)
    ├─ Archived Images button (isArchiveEnquiryEnabled = true) ✅
    └─ Reports button (isReportsEnabled = false) ❌
```

**What Happens:**
1. Dashboard receives API response
2. Extracts `featureFlags` object from response
3. Updates React state with feature flags
4. React re-renders with new state
5. Conditional rendering logic:
   ```jsx
   {featureFlags.isArchiveEnquiryEnabled && (
     <button>View Archived Images</button>
   )}
   
   {featureFlags.isReportsEnabled && (
     <button>View Reports</button>
   )}
   ```
6. User sees personalized dashboard based on their ACL

**Code Location:**
- `dashboard-mfe/src/components/Dashboard.jsx` - Conditional rendering logic

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER BROWSER                            │
│                                                                 │
│  1. Navigate to http://localhost:3000                          │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Host App (Port 3000)                        │ │
│  │  - Check localStorage for session                        │ │
│  │  - No session → Load LoginForm via Module Federation    │ │
│  └──────────────────────────────────────────────────────────┘ │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │           Login MFE (Port 3001)                          │ │
│  │  2. User enters credentials                              │ │
│  │  3. Validate input                                       │ │
│  │  4. POST /api/v1/auth/login                             │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND SERVICES                             │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │         Login Service (Port 8080)                        │ │
│  │  5. Receive login request                                │ │
│  │  6. Query H2 database for user                          │ │
│  │  7. Verify password with BCrypt                         │ │
│  │  8. Call authentication-service                         │ │
│  └──────────────────────────────────────────────────────────┘ │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │      Authentication Service (Port 8081)                  │ │
│  │  9. Receive token generation request                     │ │
│  │  10. Load RSA private key                               │ │
│  │  11. Generate JWT with user claims                      │ │
│  │  12. Sign token with RSA-256                            │ │
│  │  13. Return JWT to login-service                        │ │
│  └──────────────────────────────────────────────────────────┘ │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │         Login Service (Port 8080)                        │ │
│  │  14. Return success response with JWT                    │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                         USER BROWSER                            │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │           Login MFE (Port 3001)                          │ │
│  │  15. Receive response                                    │ │
│  │  16. Store token and user data in localStorage          │ │
│  │  17. Call onLoginSuccess() callback                     │ │
│  └──────────────────────────────────────────────────────────┘ │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Host App (Port 3000)                        │ │
│  │  18. Update state: isLoggedIn = true                    │ │
│  │  19. React re-renders                                    │ │
│  │  20. Load Dashboard via Module Federation               │ │
│  └──────────────────────────────────────────────────────────┘ │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │         Dashboard MFE (Port 3002)                        │ │
│  │  21. Component mounts                                    │ │
│  │  22. GET /api/v1/feature-flags                          │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND SERVICES                             │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │   Channel Configurations Service (Port 8082)             │ │
│  │  23. Receive feature flags request                       │ │
│  │  24. Query MASTER table for feature flags               │ │
│  │  25. Query ACL_CONFIG for user's ACLs                   │ │
│  │  26. Match ACLs and determine enabled features          │ │
│  │  27. Return feature flags response                       │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                         USER BROWSER                            │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │         Dashboard MFE (Port 3002)                        │ │
│  │  28. Receive feature flags                               │ │
│  │  29. Update state with feature flags                     │ │
│  │  30. React re-renders                                    │ │
│  │  31. Display personalized dashboard                      │ │
│  │      - Account Summary                                   │ │
│  │      - View Archived Images (if enabled)                │ │
│  │      - View Reports (if enabled)                        │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Technologies Used

### Frontend
- **React 18** - UI library
- **Webpack 5 Module Federation** - Micro-frontend orchestration
- **Babel** - JavaScript transpilation
- **CSS** - Styling

### Backend
- **Spring Boot 3.4.1** - Framework
- **Spring Data JPA** - Database access
- **H2 Database** - In-memory database
- **BCrypt** - Password hashing
- **JWT (JSON Web Tokens)** - Authentication
- **RSA-256** - Token signing algorithm

### Architecture Patterns
- **Microservices** - Independent, scalable services
- **Micro-Frontends** - Independent, deployable UI components
- **Hexagonal Architecture** - Clean separation of concerns
- **ACL (Access Control List)** - Feature-level permissions

---

## Security Features

### Password Security
- Passwords stored as BCrypt hashes (never plain text)
- BCrypt strength: 10 rounds
- Hash example: `$2a$10$SEsCf0Ai/swlgONnmnS.7eBiLcg68dSHQj5aL2qelvQ4MO8zjI3yG`

### Token Security
- JWT signed with RSA-256 (asymmetric encryption)
- Private key signs tokens (only authentication-service has it)
- Public key verifies tokens (other services can verify)
- Token expires in 1 hour
- Token stored in localStorage (client-side)

### CORS Security
- Each service explicitly allows specific origins
- Only localhost:3000, 3001, 3002 are allowed
- Prevents unauthorized cross-origin requests

### ACL Security
- Feature access controlled at database level
- Users can only access features they have ACL for
- Public features (IS_ACL_ENABLED=false) available to all
- Private features (IS_ACL_ENABLED=true) require ACL match

---

## Data Flow Summary

1. **User Input** → LoginForm
2. **LoginForm** → Login Service (HTTP POST)
3. **Login Service** → H2 Database (Query user)
4. **Login Service** → Authentication Service (Generate token)
5. **Authentication Service** → Login Service (Return JWT)
6. **Login Service** → LoginForm (Return response)
7. **LoginForm** → localStorage (Store session)
8. **LoginForm** → Host App (Callback)
9. **Host App** → Dashboard (Module Federation)
10. **Dashboard** → Channel Configurations Service (Fetch features)
11. **Channel Configurations Service** → H2 Database (Query ACLs)
12. **Channel Configurations Service** → Dashboard (Return features)
13. **Dashboard** → User (Display personalized UI)

---

## Session Persistence

### On Login
```javascript
localStorage.setItem('authToken', token);
localStorage.setItem('userId', userId);
localStorage.setItem('username', username);
localStorage.setItem('bankCode', bankCode);
localStorage.setItem('branchCode', branchCode);
localStorage.setItem('currency', currency);
```

### On Page Refresh
```javascript
const storedToken = localStorage.getItem('authToken');
if (storedToken) {
  // User is still logged in
  setIsLoggedIn(true);
  // Load user data from localStorage
}
```

### On Logout
```javascript
localStorage.removeItem('authToken');
localStorage.removeItem('userId');
// ... remove all session data
setIsLoggedIn(false);
```

---

## Testing the Flow

### Test User Credentials
```
Bank Code: 101
Branch Code: 1119
Username: testuser
Password: password123
Currency: SGD
```

### Expected Results
1. Login successful
2. JWT token generated
3. Dashboard displays
4. "View Archived Images" button visible (testuser has ACL 1000)
5. "View Reports" button NOT visible (testuser doesn't have ACL 1001)

### Different Users, Different Features
- **testuser**: Archive Enquiry only
- **1119test2**: Reports only
- **1119test3**: Both features

---

## Troubleshooting

### Login fails with "Invalid credentials"
- Check if user exists in H2 database
- Verify password hash matches
- Check BCrypt comparison logic

### JWT token not generated
- Check if authentication-service is running
- Verify RSA keys exist in resources/keys/
- Check authentication-service logs

### Dashboard doesn't load
- Check if dashboard-mfe is running on port 3002
- Verify Module Federation configuration
- Check browser console for errors

### Feature flags not loading
- Check if channel-configurations-service is running
- Verify H2 database has data
- Check CORS configuration

---

## Summary

Your login flow is a **modern, secure, microservices-based authentication system** with:

✅ **Separation of Concerns** - Each service has a single responsibility  
✅ **Scalability** - Services can scale independently  
✅ **Security** - BCrypt passwords, JWT tokens, RSA signing  
✅ **Flexibility** - Micro-frontends can be deployed independently  
✅ **Personalization** - ACL-based feature access  
✅ **Maintainability** - Clean architecture, well-organized code  

The flow demonstrates enterprise-grade patterns and best practices for building distributed systems!
