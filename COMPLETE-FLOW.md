# Complete Banking Platform Flow Documentation

This document details the complete end-to-end flow of the banking platform, from user login to dashboard display.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Browser (localhost:3000)                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Host Application                       │  │
│  │              (Orchestrator & State Manager)               │  │
│  │                                                           │  │
│  │  ┌─────────────────┐         ┌─────────────────┐        │  │
│  │  │   Login MFE     │         │  Dashboard MFE  │        │  │
│  │  │  (Port 3001)    │         │   (Port 3002)   │        │  │
│  │  │  Remote Module  │         │  Remote Module  │        │  │
│  │  └─────────────────┘         └─────────────────┘        │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP/REST API Calls
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Backend Services                           │
│  ┌──────────────────┐              ┌──────────────────┐        │
│  │  Login Service   │──────────────▶│  Auth Service    │        │
│  │  (Port 8080)     │  WebClient   │  (Port 8081)     │        │
│  │                  │              │                  │        │
│  │ - Validates user │              │ - Generates JWT  │        │
│  │ - Checks entity  │              │ - RS256 signing  │        │
│  │ - Verifies pwd   │              │ - Token payload  │        │
│  └──────────────────┘              └──────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

## Complete User Flow

### Phase 1: Application Initialization

**Step 1: User Opens Browser**
```
User navigates to: http://localhost:3000
```

**Step 2: Host App Loads**
```javascript
// host-app/src/App.jsx
const App = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  
  // Check for existing session
  useEffect(() => {
    const token = localStorage.getItem('authToken');
    if (token) {
      // Restore session
      setIsLoggedIn(true);
    }
  }, []);
```

**What happens:**
1. Host App checks localStorage for existing session
2. If no session found, `isLoggedIn = false`
3. Host App decides to load Login MFE

**Step 3: Host App Loads Login MFE via Module Federation**
```javascript
// host-app/webpack.config.js
remotes: {
  login_mfe: 'login_mfe@http://localhost:3001/remoteEntry.js',
}

// host-app/src/App.jsx
const LoginForm = lazy(() => import('login_mfe/LoginForm'));
```

**What happens:**
1. Webpack fetches `http://localhost:3001/remoteEntry.js`
2. Loads the Login MFE component dynamically
3. Renders LoginForm in the Host App

---

### Phase 2: User Login Process

**Step 4: User Sees Login Form**
```
┌─────────────────────────────────┐
│      Banking Login              │
│                                 │
│  Bank Code:     [___]           │
│  Branch Code:   [____]          │
│  Username:      [________]      │
│  Password:      [********]      │
│  Currency:      [SGD ▼]         │
│                                 │
│         [Login Button]          │
└─────────────────────────────────┘
```

**Step 5: User Fills Form and Clicks Login**
```javascript
// login-mfe/src/components/LoginForm.jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  
  // Validate form
  if (!validateForm()) return;
  
  // Call backend API
  const response = await fetch('http://localhost:8080/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      bankCode: '101',
      branchCode: '1119',
      username: 'testuser',
      password: 'password123',
      currency: 'SGD'
    })
  });
```

**What happens:**
1. Form validation runs (bank code 3 digits, branch code 4 digits, etc.)
2. If valid, makes HTTP POST to Login Service
3. Shows loading state

---

### Phase 3: Backend Authentication

**Step 6: Login Service Receives Request**
```java
// LoginController.java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    log.info("Login attempt for user: {}", request.username());
    
    LoginResult result = loginUseCase.login(
        request.bankCode(),
        request.branchCode(),
        request.username(),
        request.password(),
        request.currency()
    );
```

**What happens:**
1. LoginController receives the request
2. Delegates to LoginUseCase (domain layer)
3. Logs the login attempt

**Step 7: Login Domain Service Validates User**
```java
// LoginDomainService.java
@Override
public LoginResult login(String bankCode, String branchCode, 
                        String username, String password, String currency) {
    
    // 1. Validate entity exists
    if (!isValidEntity(bankCode, branchCode)) {
        throw new InvalidEntityException(bankCode, branchCode);
    }
    
    // 2. Find user
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new InvalidCredentialsException());
    
    // 3. Verify password
    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new InvalidCredentialsException();
    }
    
    // 4. Check currency match
    if (!user.getCurrency().equals(currency)) {
        throw new CurrencyMismatchException();
    }
```

**What happens:**
1. Validates bank code and branch code exist
2. Finds user in database
3. Verifies password using BCrypt
4. Checks currency matches user's currency

**Step 8: Login Service Calls Authentication Service**
```java
// AuthenticationServiceAdapter.java
@Override
public String generateToken(String userId, String bankCode, 
                           String branchCode, String currency) {
    
    TokenRequest request = new TokenRequest(userId, bankCode, branchCode, currency);
    
    TokenResponse response = webClient
        .post()
        .uri("/api/v1/auth/generate-token")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(TokenResponse.class)
        .block();
    
    return response.token();
}
```

**What happens:**
1. Creates token request with user details
2. Makes HTTP POST to Authentication Service
3. Waits for JWT token response

**Step 9: Authentication Service Generates JWT**
```java
// TokenGenerationService.java
@Override
public GeneratedToken generateToken(TokenPayload payload) {
    
    // Load RSA private key
    PrivateKey privateKey = keyLoader.loadPrivateKey();
    
    // Build JWT with claims
    String token = Jwts.builder()
        .subject(payload.userId())
        .claim("bankCode", payload.bankCode())
        .claim("branchCode", payload.branchCode())
        .claim("currency", payload.currency())
        .issuedAt(new Date())
        .expiration(expirationDate)
        .issuer(issuer)
        .signWith(privateKey, Jwts.SIG.RS256)
        .compact();
    
    return new GeneratedToken(token, expirationDate);
}
```

**What happens:**
1. Loads RSA private key from PEM file
2. Creates JWT with user claims (userId, bankCode, branchCode, currency)
3. Signs token with RS256 algorithm
4. Returns token with expiration time

**Step 10: Login Service Returns Response**
```java
// LoginController.java
LoginResponse response = new LoginResponse(
    result.success(),      // true
    result.userId(),       // "user-123"
    result.message(),      // "Login successful"
    result.token()         // "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
);

return ResponseEntity.ok(response);
```

**Response JSON:**
```json
{
  "success": true,
  "userId": "user-123",
  "message": "Login successful",
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLTEyMyIsImJhbmtDb2RlIjoiMTAxIiwiYnJhbmNoQ29kZSI6IjExMTkiLCJjdXJyZW5jeSI6IlNHRCIsImlhdCI6MTcwOTI4MDAwMCwiZXhwIjoxNzA5MjgzNjAwLCJpc3MiOiJiYW5raW5nLWF1dGgtc2VydmljZSJ9.signature"
}
```

---

### Phase 4: Frontend Receives Response

**Step 11: Login MFE Receives Success Response**
```javascript
// login-mfe/src/components/LoginForm.jsx
const data = await response.json();

if (response.ok && data.success) {
  // Store user data in localStorage
  localStorage.setItem('authToken', data.token);
  localStorage.setItem('userId', data.userId);
  localStorage.setItem('username', formData.username);
  localStorage.setItem('bankCode', formData.bankCode);
  localStorage.setItem('branchCode', formData.branchCode);
  localStorage.setItem('currency', formData.currency);
  
  // Call success callback
  if (onLoginSuccess) {
    onLoginSuccess({
      ...data,
      username: formData.username,
      bankCode: formData.bankCode,
      branchCode: formData.branchCode,
      currency: formData.currency,
    });
  }
}
```

**What happens:**
1. Stores JWT token in localStorage
2. Stores user details for dashboard
3. Calls `onLoginSuccess` callback provided by Host App

**Step 12: Host App Receives Login Success**
```javascript
// host-app/src/App.jsx
const handleLoginSuccess = (loginResponse) => {
  console.log('Login successful:', loginResponse);
  
  // Store authentication data
  localStorage.setItem('authToken', loginResponse.token);
  localStorage.setItem('userId', loginResponse.userId);
  localStorage.setItem('username', loginResponse.username);
  localStorage.setItem('bankCode', loginResponse.bankCode);
  localStorage.setItem('branchCode', loginResponse.branchCode);
  localStorage.setItem('currency', loginResponse.currency);
  
  // Create user object
  const user = {
    userId: loginResponse.userId,
    username: loginResponse.username,
    bankCode: loginResponse.bankCode,
    branchCode: loginResponse.branchCode,
    currency: loginResponse.currency,
  };
  
  // Update state
  setToken(loginResponse.token);
  setUserData(user);
  setIsLoggedIn(true);  // ← This triggers navigation to Dashboard
};
```

**What happens:**
1. Host App receives login data
2. Stores everything in localStorage (for session persistence)
3. Updates React state
4. `setIsLoggedIn(true)` triggers re-render

---

### Phase 5: Navigation to Dashboard

**Step 13: Host App Switches to Dashboard MFE**
```javascript
// host-app/src/App.jsx
return (
  <div className="app-container">
    <Suspense fallback={<LoadingSpinner />}>
      {!isLoggedIn ? (
        <LoginForm onLoginSuccess={handleLoginSuccess} />
      ) : (
        <Dashboard 
          user={userData}
          token={token}
          onLogout={handleLogout}
          onViewCheques={handleViewCheques}
        />
      )}
    </Suspense>
  </div>
);
```

**What happens:**
1. React re-renders due to state change
2. Condition `!isLoggedIn` is now `false`
3. Host App unmounts Login MFE
4. Host App loads Dashboard MFE

**Step 14: Host App Loads Dashboard MFE**
```javascript
// host-app/webpack.config.js
remotes: {
  dashboard_mfe: 'dashboard_mfe@http://localhost:3002/remoteEntry.js',
}

// host-app/src/App.jsx
const Dashboard = lazy(() => import('dashboard_mfe/Dashboard'));
```

**What happens:**
1. Webpack fetches `http://localhost:3002/remoteEntry.js`
2. Loads Dashboard component dynamically
3. Passes user data and callbacks as props

**Step 15: Dashboard MFE Renders**
```javascript
// dashboard-mfe/src/components/Dashboard.jsx
const Dashboard = ({ user, token, onLogout, onViewCheques }) => {
  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>Banking Dashboard</h1>
        <div className="user-info">
          <span>Welcome, {user?.username}</span>
          <button onClick={handleLogout}>Logout</button>
        </div>
      </header>
      
      <main className="dashboard-main">
        <div className="dashboard-card">
          <h2>Account Summary</h2>
          <div className="account-details">
            <div>User ID: {user?.userId}</div>
            <div>Bank Code: {user?.bankCode}</div>
            <div>Branch Code: {user?.branchCode}</div>
            <div>Currency: {user?.currency}</div>
          </div>
        </div>
        
        <div className="dashboard-card">
          <h2>Archived Cheques</h2>
          <button onClick={handleViewCheques}>
            View Archived Cheques
          </button>
        </div>
      </main>
    </div>
  );
};
```

**User sees:**
```
┌─────────────────────────────────────────────────┐
│  Banking Dashboard          Welcome, testuser   │
│                                    [Logout]     │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌─────────────────┐  ┌─────────────────┐     │
│  │ Account Summary │  │ Archived Cheques│     │
│  │                 │  │                 │     │
│  │ User ID: user-123│  │ View and manage│     │
│  │ Bank: 101       │  │ your archived  │     │
│  │ Branch: 1119    │  │ cheque images  │     │
│  │ Currency: SGD   │  │                 │     │
│  │                 │  │ [View Archived  │     │
│  │                 │  │     Cheques]    │     │
│  └─────────────────┘  └─────────────────┘     │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

### Phase 6: Session Persistence

**Step 16: User Refreshes Page**
```javascript
// host-app/src/App.jsx
useEffect(() => {
  const storedToken = localStorage.getItem('authToken');
  const storedUserId = localStorage.getItem('userId');
  const storedUsername = localStorage.getItem('username');
  const storedBankCode = localStorage.getItem('bankCode');
  const storedBranchCode = localStorage.getItem('branchCode');
  const storedCurrency = localStorage.getItem('currency');

  if (storedToken && storedUserId) {
    setToken(storedToken);
    setUserData({
      userId: storedUserId,
      username: storedUsername,
      bankCode: storedBankCode,
      branchCode: storedBranchCode,
      currency: storedCurrency,
    });
    setIsLoggedIn(true);
  }
  setLoading(false);
}, []);
```

**What happens:**
1. On page load, Host App checks localStorage
2. If token exists, restores user session
3. User stays logged in and sees Dashboard
4. No need to login again

---

### Phase 7: Logout Flow

**Step 17: User Clicks Logout**
```javascript
// dashboard-mfe/src/components/Dashboard.jsx
const handleLogout = () => {
  // Clear token from localStorage
  localStorage.removeItem('authToken');
  localStorage.removeItem('userId');
  
  // Call parent callback
  if (onLogout) {
    onLogout();
  }
};
```

**Step 18: Host App Handles Logout**
```javascript
// host-app/src/App.jsx
const handleLogout = () => {
  console.log('Logging out...');
  
  // Clear all stored data
  localStorage.removeItem('authToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.removeItem('bankCode');
  localStorage.removeItem('branchCode');
  localStorage.removeItem('currency');
  
  // Reset state
  setToken(null);
  setUserData(null);
  setIsLoggedIn(false);  // ← Triggers navigation back to Login
};
```

**What happens:**
1. Dashboard calls `onLogout` callback
2. Host App clears all localStorage data
3. Host App resets state to logged out
4. React re-renders and shows Login MFE again
5. User is back at login screen

---

## Data Flow Summary

### Login Flow
```
User Input → Login MFE → Login Service → Auth Service
                ↓              ↓              ↓
         Validation    Entity Check    JWT Generation
                ↓              ↓              ↓
         Form Valid    Password OK    Token Created
                ↓              ↓              ↓
            Submit ← Response ← Token Returned
                ↓
         Store in localStorage
                ↓
         Callback to Host App
                ↓
         Host App Updates State
                ↓
         Navigate to Dashboard
```

### Session Persistence
```
Page Refresh → Host App Checks localStorage
                      ↓
              Token Found? → Yes → Restore Session → Show Dashboard
                      ↓
                     No → Show Login
```

### Logout Flow
```
User Clicks Logout → Dashboard Calls onLogout
                            ↓
                    Host App Clears Data
                            ↓
                    Host App Resets State
                            ↓
                    Navigate to Login
```

---

## Technology Stack

### Frontend
- **React 18**: UI library
- **Webpack 5**: Module bundler with Module Federation
- **Module Federation**: Micro-frontend architecture
- **localStorage**: Session persistence

### Backend
- **Spring Boot 3.4**: Framework
- **Spring Security**: Password hashing (BCrypt)
- **Spring WebFlux**: WebClient for inter-service calls
- **JJWT**: JWT token generation
- **RS256**: Asymmetric signing algorithm
- **Hexagonal Architecture**: Clean separation of concerns

---

## Security Features

1. **Password Hashing**: BCrypt with salt
2. **JWT Tokens**: Signed with RS256 (RSA asymmetric)
3. **Token Expiration**: 1 hour validity
4. **CORS Protection**: Configured origins only
5. **HTTPS Ready**: Production deployment ready
6. **No Password Storage**: Only hashed passwords in DB

---

## Port Reference

| Component | Port | Purpose |
|-----------|------|---------|
| Host App | 3000 | Main application entry point |
| Login MFE | 3001 | Login micro-frontend |
| Dashboard MFE | 3002 | Dashboard micro-frontend |
| Login Service | 8080 | Authentication backend |
| Auth Service | 8081 | JWT token generation |

---

## Key Files Reference

### Frontend
- `host-app/src/App.jsx` - Main orchestrator
- `login-mfe/src/components/LoginForm.jsx` - Login UI
- `dashboard-mfe/src/components/Dashboard.jsx` - Dashboard UI

### Backend
- `LoginController.java` - Login API endpoint
- `LoginDomainService.java` - Business logic
- `AuthenticationServiceAdapter.java` - Inter-service communication
- `TokenGenerationService.java` - JWT creation
- `JwtTokenProvider.java` - Token utilities

### Configuration
- `host-app/webpack.config.js` - Module Federation setup
- `login-service/src/main/resources/application.yml` - Service config
- `WebConfig.java` - CORS configuration
- `SecurityConfig.java` - Security rules

---

## Error Handling

### Frontend Errors
- Form validation errors (inline)
- Network errors (toast/alert)
- Loading states (spinners)

### Backend Errors
- `InvalidEntityException` → 400 Bad Request
- `InvalidCredentialsException` → 401 Unauthorized
- `CurrencyMismatchException` → 400 Bad Request
- Generic errors → 500 Internal Server Error

All errors are logged and returned with meaningful messages.

---

## Future Enhancements

1. **Token Refresh**: Automatic token renewal
2. **Remember Me**: Extended session option
3. **Multi-Factor Auth**: OTP/SMS verification
4. **Cheques Viewer MFE**: View archived cheques
5. **Routing**: React Router for better navigation
6. **State Management**: Redux/Context for complex state
7. **Error Boundaries**: Better error handling
8. **Analytics**: User behavior tracking
9. **Performance**: Code splitting, lazy loading
10. **Testing**: Unit, integration, E2E tests
