# Fix "Script error" Issue - Step by Step

## What Changed

I've updated the webpack configurations and added an ErrorBoundary to better handle and display errors.

### Changes Made:
1. Updated shared module configuration in all webpack configs (added `strictVersion: false`)
2. Added ErrorBoundary component to catch and display errors gracefully
3. Removed unused React import from App.jsx

## Steps to Fix

### Step 1: Stop All Running Services

Press `Ctrl+C` in all terminal windows to stop:
- host-app (port 3000)
- login-mfe (port 3001)
- dashboard-mfe (port 3002)

### Step 2: Clear Browser Cache Completely

1. Open Chrome
2. Press `Ctrl+Shift+Delete`
3. Select "All time" for time range
4. Check "Cached images and files"
5. Click "Clear data"
6. Close ALL browser windows

### Step 3: Restart Services in Correct Order

**Important: Wait for each service to fully compile before starting the next one**

#### Terminal 1: Login MFE
```bash
cd login-mfe
npm start
```
Wait for: `webpack compiled successfully`

#### Terminal 2: Dashboard MFE
```bash
cd dashboard-mfe
npm start
```
Wait for: `webpack compiled successfully`

#### Terminal 3: Host App
```bash
cd host-app
npm start
```
Wait for: `webpack compiled successfully`

### Step 4: Verify Remote Entry Files

Open a NEW browser window and check these URLs:

1. http://localhost:3001/remoteEntry.js
   - Should show JavaScript code (not 404)
   
2. http://localhost:3002/remoteEntry.js
   - Should show JavaScript code (not 404)

If either shows 404:
- That MFE didn't compile properly
- Check the terminal for errors
- Try stopping and restarting that MFE

### Step 5: Access Host App

Open: http://localhost:3000

You should now see either:
- The Login Form (success!)
- A better error message from ErrorBoundary (if still failing)

## If You Still See Errors

### Option 1: Check the ErrorBoundary Message

The new ErrorBoundary will show you:
- What went wrong
- Checklist of things to verify
- Detailed error information (click "Error Details")

### Option 2: Clean Reinstall

If the error persists, do a clean reinstall:

```bash
# In login-mfe folder
cd login-mfe
rm -rf node_modules package-lock.json
npm install
npm start

# In dashboard-mfe folder (new terminal)
cd dashboard-mfe
rm -rf node_modules package-lock.json
npm install
npm start

# In host-app folder (new terminal)
cd host-app
rm -rf node_modules package-lock.json
npm install
npm start
```

### Option 3: Check for Port Conflicts

Make sure no other processes are using ports 3000, 3001, or 3002:

```bash
# Check what's using the ports
netstat -ano | findstr :3000
netstat -ano | findstr :3001
netstat -ano | findstr :3002

# Kill any conflicting processes
taskkill /PID <PID> /F
```

### Option 4: Check Browser Console

Open DevTools (F12) and look for specific errors:

1. **Network Tab**: Look for failed requests (red)
   - remoteEntry.js files should be 200 OK
   - If 404: MFE is not running
   - If CORS error: Restart the MFE

2. **Console Tab**: Look for actual error messages
   - "Failed to fetch": MFE is not accessible
   - "Module not found": Exposed module name mismatch
   - Other errors: Check the error details

## What the ErrorBoundary Does

The ErrorBoundary component now:
- Catches errors from remote modules
- Shows a user-friendly error message
- Provides a checklist of things to verify
- Shows detailed error information for debugging
- Offers a "Reload Page" button

## Testing After Fix

Once localhost:3000 loads without errors:

1. You should see the Login Form
2. Enter test credentials:
   - Bank Code: 101
   - Branch Code: 1119
   - Username: testuser
   - Password: password123
   - Currency: SGD
3. Click "Login"
4. You should see the Dashboard

## Common Causes of "Script error"

1. **MFEs not running** - Most common cause
   - Solution: Verify both MFEs are running and compiled

2. **Browser cache** - Old cached files
   - Solution: Clear cache completely and close all browser windows

3. **Port conflicts** - Another process using the ports
   - Solution: Kill conflicting processes or change ports

4. **React version mismatch** - Different React versions
   - Solution: Already fixed with strictVersion: false

5. **Network issues** - Firewall or antivirus blocking
   - Solution: Check firewall settings, allow localhost connections

## Webpack Config Changes

### Before:
```javascript
shared: {
  react: {
    singleton: true,
    requiredVersion: false,
    eager: false,
  },
}
```

### After:
```javascript
shared: {
  react: {
    singleton: true,
    requiredVersion: '^18.2.0',
    strictVersion: false,  // Added this
    eager: false,
  },
}
```

This change makes Module Federation more lenient about React version matching.

## Still Not Working?

If you've tried everything and it's still not working:

1. Take a screenshot of:
   - Browser console errors
   - Network tab showing failed requests
   - Terminal output from all three MFEs

2. Check if you can access the MFEs standalone:
   - http://localhost:3001 - Should show login form
   - http://localhost:3002 - Should show dashboard with mock data

3. Verify all backend services are running:
   - http://localhost:8080/actuator/health
   - http://localhost:8081/actuator/health
   - http://localhost:8082/actuator/health

## Success Indicators

You'll know it's working when:
- ✅ No "Script error" in browser
- ✅ Login Form appears at localhost:3000
- ✅ No errors in browser console
- ✅ remoteEntry.js files load successfully (Network tab)
- ✅ Login works and navigates to Dashboard
- ✅ Dashboard shows user information and features
