# Frontend-Backend Integration Setup

Complete guide to connect the Login MFE to the Login Service.

## What's Been Configured

### Backend (Login Service)
✅ CORS enabled for frontend origins (port 3001, 3000)
✅ Login endpoint accessible from frontend
✅ Security configured to allow cross-origin requests

### Frontend (Login MFE)
✅ API endpoint configured: `http://localhost:8080/api/v1/auth/login`
✅ Form validation matching backend requirements
✅ Error handling for API responses

## Quick Start

### Step 1: Start Backend Services

**Terminal 1 - Authentication Service:**
```bash
cd authentication-service
mvn spring-boot:run
```
Wait for: "Started AuthenticationServiceApplication"

**Terminal 2 - Login Service:**
```bash
cd login-service
mvn spring-boot:run
```
Wait for: "Started LoginServiceApplication"

### Step 2: Start Frontend

**Terminal 3 - Login MFE:**
```bash
cd login-mfe
npm install  # First time only
npm start
```
Wait for: "webpack compiled successfully"

### Step 3: Test the Integration

1. Open browser: http://localhost:3001
2. Fill in the login form:
   - **Bank Code**: 101
   - **Branch Code**: 1119
   - **Username**: john.doe
   - **Password**: password123
   - **Currency**: SGD
3. Click "Login"
4. Check browser console and network tab

## Expected Flow

```
Browser (3001) → Login MFE → HTTP POST → Login Service (8080)
                                              ↓
                                    Validate credentials
                                              ↓
                                    Call Auth Service (8081)
                                              ↓
                                    Generate JWT token
                                              ↓
                                    Return token to MFE
                                              ↓
                                    Display success message
```

## Troubleshooting

### CORS Errors

**Symptom**: Browser console shows "CORS policy" error

**Solution**:
1. Verify Login Service is running on port 8080
2. Check CorsConfig.java includes your frontend origin
3. Restart Login Service after CORS changes

### Connection Refused

**Symptom**: "Failed to fetch" or "ERR_CONNECTION_REFUSED"

**Solution**:
1. Verify all services are running:
   ```bash
   curl http://localhost:8080/actuator/health
   curl http://localhost:8081/actuator/health
   ```
2. Check no other applications are using ports 8080, 8081, 3001

### 401 Unauthorized

**Symptom**: Login returns 401 status

**Solution**:
1. Check username/password are correct
2. Verify bank code and branch code exist in database
3. Check Login Service logs for authentication errors

### 404 Not Found

**Symptom**: Login returns 404 status

**Solution**:
1. Verify bank/branch/username combination exists
2. Check Login Service logs for "InvalidEntityException"
3. Ensure database is populated with test data

### 400 Bad Request - Currency Mismatch

**Symptom**: Login returns 400 with currency mismatch message

**Solution**:
1. Verify user's currency in database matches selected currency
2. Check Login Service logs for "CurrencyMismatchException"

## Testing Without Frontend

Test the backend directly:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "bankCode": "101",
    "branchCode": "1119",
    "username": "john.doe",
    "password": "password123",
    "currency": "SGD"
  }'
```

Expected response:
```json
{
  "success": true,
  "userId": "user-uuid-123",
  "message": "Login successful",
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## Browser Developer Tools

### Network Tab
- Check request URL: `http://localhost:8080/api/v1/auth/login`
- Check request method: POST
- Check request headers: Content-Type: application/json
- Check request payload matches form data
- Check response status: 200 (success) or error code
- Check response body for token or error message

### Console Tab
- Check for JavaScript errors
- Check for CORS errors
- Check for network errors
- View console.log messages from LoginForm component

## Configuration Files

### Backend CORS Configuration
File: `login-service/src/main/java/com/banking/loginservice/infrastructure/config/CorsConfig.java`

Allowed origins:
- http://localhost:3001 (Login MFE)
- http://localhost:3000 (Alternative React port)
- http://127.0.0.1:3001
- http://127.0.0.1:3000

### Frontend API Configuration
File: `login-mfe/src/components/LoginForm.jsx`

API endpoint: `http://localhost:8080/api/v1/auth/login`

## Next Steps

1. **Add User Data**: Populate database with test users
2. **Token Storage**: Store JWT token in localStorage or sessionStorage
3. **Redirect**: Navigate to dashboard after successful login
4. **Error Messages**: Enhance error handling with specific messages
5. **Loading States**: Add loading spinner during API calls

## Production Considerations

### Backend
- Configure CORS for production frontend URL
- Enable HTTPS
- Add rate limiting
- Implement proper logging
- Set up monitoring

### Frontend
- Use environment variables for API URLs
- Enable production build optimizations
- Add error tracking (e.g., Sentry)
- Implement token refresh logic
- Add session timeout handling

## Related Documentation

- [Login MFE README](login-mfe/README.md)
- [Login Service README](login-service/README.md)
- [Sequence Diagrams](sequence-diagrams/banking-services-flow.md)
- [Swagger Setup](SWAGGER-SETUP.md)
