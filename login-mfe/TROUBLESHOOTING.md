# Login MFE Troubleshooting Guide

## Blank Page Issue

If you see a blank page at http://localhost:3001, follow these steps:

### Step 1: Check Browser Console

1. Open browser Developer Tools (F12)
2. Go to Console tab
3. Look for any JavaScript errors

Common errors and solutions:

**Error: "Cannot find module './components/LoginForm'"**
- Solution: File extension issue, already fixed in latest code

**Error: "React is not defined"**
- Solution: React not installed properly
```bash
cd login-mfe
npm install react react-dom --save
```

**Error: "Unexpected token '<'"**
- Solution: Babel not configured properly, .babelrc file added

### Step 2: Check Network Tab

1. Open Developer Tools (F12)
2. Go to Network tab
3. Refresh the page
4. Check if `bundle.js` or `main.js` is loading
5. Check if there are any 404 errors

### Step 3: Check Terminal Output

Look for webpack compilation errors in the terminal where you ran `npm start`.

**Success message should show:**
```
webpack compiled successfully
```

**If you see errors:**
- Module not found errors → Check file paths
- Syntax errors → Check JavaScript/JSX syntax
- Babel errors → Check .babelrc configuration

### Step 4: Restart Development Server

```bash
# Stop the server (Ctrl+C)
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
npm start
```

### Step 5: Check Port Availability

Ensure port 3001 is not being used by another application:

**Windows:**
```bash
netstat -ano | findstr :3001
```

**Mac/Linux:**
```bash
lsof -i :3001
```

If port is in use, either:
1. Stop the other application
2. Change port in webpack.config.js

### Step 6: Verify File Structure

Ensure these files exist:
```
login-mfe/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── LoginForm.jsx
│   │   └── LoginForm.css
│   └── index.js
├── .babelrc
├── package.json
└── webpack.config.js
```

### Step 7: Check index.html

Verify `public/index.html` has a root div:
```html
<div id="root"></div>
```

### Step 8: Manual Test

Create a simple test to verify React is working:

Edit `src/index.js` temporarily:
```javascript
import React from 'react';
import ReactDOM from 'react-dom/client';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<h1>Test - React is working!</h1>);
```

If you see "Test - React is working!", then React is configured correctly and the issue is with the LoginForm component.

### Step 9: Check LoginForm Component

Verify `src/components/LoginForm.jsx` has proper export:
```javascript
export default LoginForm;
```

Should be at the end of the file.

### Step 10: Clear Browser Cache

1. Open Developer Tools (F12)
2. Right-click the refresh button
3. Select "Empty Cache and Hard Reload"

## Still Not Working?

### Complete Clean Reinstall

```bash
cd login-mfe

# Remove all generated files
rm -rf node_modules
rm -rf dist
rm package-lock.json

# Reinstall dependencies
npm install

# Start fresh
npm start
```

### Check Node Version

Ensure you're using Node.js 16 or higher:
```bash
node --version
```

If version is too old, update Node.js.

### Verify Package.json Scripts

Ensure `package.json` has:
```json
{
  "scripts": {
    "start": "webpack serve --mode development"
  }
}
```

### Check for Missing Dependencies

Install all required dependencies:
```bash
npm install --save react react-dom
npm install --save-dev @babel/core @babel/preset-react babel-loader \
  css-loader style-loader html-webpack-plugin \
  webpack webpack-cli webpack-dev-server
```

## Common Issues

### Issue: White screen, no errors in console

**Cause**: React component not rendering
**Solution**: Check if `document.getElementById('root')` returns null

### Issue: "Module not found" errors

**Cause**: Incorrect file paths or missing files
**Solution**: Verify all import statements match actual file locations

### Issue: Webpack compilation errors

**Cause**: Configuration issues
**Solution**: Compare your webpack.config.js with the provided template

### Issue: Port already in use

**Cause**: Another process using port 3001
**Solution**: Kill the process or change the port in webpack.config.js

## Getting Help

If none of these solutions work:

1. Check the terminal output for specific error messages
2. Check browser console for JavaScript errors
3. Verify all files match the provided templates
4. Try the manual test (Step 8) to isolate the issue

## Success Checklist

✅ Terminal shows "webpack compiled successfully"
✅ Browser shows no errors in console
✅ Network tab shows bundle.js loaded successfully
✅ Page displays "Login Micro-Frontend (Standalone)" heading
✅ Login form is visible with all fields
