# Module Federation Troubleshooting Guide

## Current Issue: "Script error" at localhost:3000

This error typically occurs when the Host App cannot load remote modules from login-mfe or dashboard-mfe.

## Step-by-Step Resolution

### Step 1: Verify All Services Are Running

You need **5 services** running simultaneously:

1. **login-service** (Backend) - Port 8080
2. **authentication-service** (Backend) - Port 8081
3. **channel-configurations-service** (Backend) - Port 8082
4. **login-mfe** (Frontend) - Port 3001
5. **dashboard-mfe** (Frontend) - Port 3002

### Step 2: Start Services in Correct Order

#### Backend Services (Start First)
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

Wait for all backend services to fully start (look for "Started [ServiceName]Application" in logs).

#### Frontend MFEs (Start Second)
```bash
# Terminal 4 - Login MFE
cd login-mfe
npm start

# Terminal 5 - Dashboard MFE
cd dashboard-mfe
npm start
```

Wait for webpack to compile successfully. You should see:
- `webpack compiled successfully`
- `Project is running at http://localhost:3001/` (for login-mfe)
- `Project is running at http://localhost:3002/` (for dashboard-mfe)

#### Host App (Start Last)
```bash
# Terminal 6 - Host App
cd host-app
npm start
```

### Step 3: Verify Remote Entry Files Are Accessible

Before opening localhost:3000, verify these URLs are accessible:

1. Open browser and navigate to: `http://localhost:3001/remoteEntry.js`
   - Should download or display JavaScript code
   - If you get 404 or connection refused, login-mfe is not running properly

2. Open browser and navigate to: `http://localhost:3002/remoteEntry.js`
   - Should download or display JavaScript code
   - If you get 404 or connection refused, dashboard-mfe is not running properly

### Step 4: Clear Browser Cache

1. Open Chrome DevTools (F12)
2. Right-click the refresh button
3. Select "Empty Cache and Hard Reload"
4. Or use: Ctrl+Shift+Delete → Clear cached images and files

### Step 5: Access Host App

Now navigate to: `http://localhost:3000`

## Common Issues and Solutions

### Issue 1: "Script error" or "Uncaught runtime errors"

**Cause**: Remote modules cannot be loaded

**Solutions**:
- Verify login-mfe and dashboard-mfe are running
- Check remoteEntry.js files are accessible (Step 3)
- Clear browser cache
- Restart all frontend services

### Issue 2: CORS Errors

**Cause**: Cross-origin requests blocked

**Solutions**:
- Verify CORS headers in webpack configs (already configured)
- Check browser console for specific CORS errors
- Restart the MFE that's showing CORS errors

### Issue 3: Module Not Found

**Cause**: Exposed module name mismatch

**Solutions**:
- Verify webpack configs use correct names:
  - login-mfe exposes: `./LoginForm`
  - dashboard-mfe exposes: `./Dashboard`
  - host-app imports: `login_mfe/LoginForm` and `dashboard_mfe/Dashboard`

### Issue 4: Shared Dependencies Version Mismatch

**Cause**: React versions don't match

**Solutions**:
- All three apps use React 18.2.0 (already configured)
- If you see warnings, run `npm install` in each frontend folder

### Issue 5: Port Already in Use

**Cause**: Previous process still running on port

**Solutions**:
```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Or change port in webpack.config.js
```

## Debugging Checklist

When you see "Script error" at localhost:3000:

- [ ] All 3 backend services running (8080, 8081, 8082)
- [ ] login-mfe running on port 3001
- [ ] dashboard-mfe running on port 3002
- [ ] http://localhost:3001/remoteEntry.js is accessible
- [ ] http://localhost:3002/remoteEntry.js is accessible
- [ ] Browser cache cleared
- [ ] No CORS errors in browser console
- [ ] No 404 errors in browser console Network tab

## Expected Browser Console Output

When localhost:3000 loads successfully, you should see:
```
[webpack-dev-server] Server started: Hot Module Replacement enabled
[HMR] Waiting for update signal from WDS...
```

No errors related to:
- "Failed to fetch"
- "Script error"
- "Cannot read properties of undefined"
- "Module not found"

## Testing the Complete Flow

Once localhost:3000 loads without errors:

1. You should see the Login Form (from login-mfe)
2. Enter credentials:
   - Bank Code: 101
   - Branch Code: 1119
   - Username: testuser
   - Password: password123
   - Currency: SGD
3. Click "Login"
4. You should see the Dashboard (from dashboard-mfe)
5. Dashboard should show:
   - User information
   - "View Archived Images" button (if user has ACL 1000)
   - "View Reports" button (if user has ACL 1001)

## Test Users and Feature Flags

| User ID    | Password    | ACL IDs    | Features Enabled                    |
|------------|-------------|------------|-------------------------------------|
| testuser   | password123 | 1000       | Archive Enquiry only                |
| 1119test1  | password123 | 1000       | Archive Enquiry only                |
| 1119test2  | password123 | 1001       | Reports only                        |
| 1119test3  | password123 | 1000, 1001 | Both Archive Enquiry and Reports    |

## Still Having Issues?

If you're still seeing "Script error" after following all steps:

1. Stop ALL services (Ctrl+C in all terminals)
2. Delete node_modules and package-lock.json in all frontend folders
3. Run `npm install` in host-app, login-mfe, and dashboard-mfe
4. Restart services in the correct order (Step 2)
5. Check browser console for specific error messages
6. Check Network tab in DevTools for failed requests

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     localhost:3000                          │
│                      Host App                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Manages authentication state and navigation         │  │
│  │  Loads remote modules dynamically                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────┐      ┌─────────────────────────┐ │
│  │   login_mfe         │      │   dashboard_mfe         │ │
│  │   (Port 3001)       │      │   (Port 3002)           │ │
│  │   LoginForm         │      │   Dashboard             │ │
│  └─────────────────────┘      └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                          │
                          │ API Calls
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    Backend Services                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐ │
│  │ login-service│  │ auth-service │  │ channel-config   │ │
│  │  Port 8080   │  │  Port 8081   │  │  Port 8082       │ │
│  └──────────────┘  └──────────────┘  └──────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Key Points

1. **Host App (localhost:3000)** is the main entry point - NOT the individual MFEs
2. **login-mfe (localhost:3001)** and **dashboard-mfe (localhost:3002)** can run standalone for testing, but navigation only works through Host App
3. **Module Federation** loads remote modules at runtime, so all MFEs must be running before accessing Host App
4. **remoteEntry.js** files are the entry points for remote modules - they must be accessible
