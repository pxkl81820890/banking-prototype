# Login Micro-Frontend (MFE)

React-based login micro-frontend using Webpack 5 Module Federation.

## Overview

This is a **Remote** application that exposes a login form component for consumption by Host applications. It provides a complete login interface for the multi-entity banking platform.

## Architecture

- **Type**: Remote Micro-Frontend
- **Exposed Module**: `./LoginForm`
- **Port**: 3001
- **Module Federation Name**: `loginMfe`

## Features

- Login form with validation for:
  - Bank Code (3 digits)
  - Branch Code (4 digits)
  - Username (min 3 characters)
  - Password (min 8 characters)
  - Currency (dropdown: SGD/USD)
- Real-time field validation
- Integration with Login Service API (port 8080)
- Shared React dependencies (singleton pattern)
- Standalone mode for development

## Quick Start

### Prerequisites
- Node.js 16+
- npm or yarn

### Installation

```bash
cd login-mfe
npm install
```

### Development

Run in standalone mode:
```bash
npm start
```

Access at: http://localhost:3001

### Build for Production

```bash
npm run build
```

## Module Federation Configuration

### Exposed Modules

```javascript
exposes: {
  './LoginForm': './src/components/LoginForm'
}
```

### Shared Dependencies

```javascript
shared: {
  react: { singleton: true, requiredVersion: '^18.2.0' },
  'react-dom': { singleton: true, requiredVersion: '^18.2.0' }
}
```

## Usage in Host Application

### 1. Configure Module Federation in Host

```javascript
// webpack.config.js in Host app
new ModuleFederationPlugin({
  name: 'hostApp',
  remotes: {
    loginMfe: 'loginMfe@http://localhost:3001/remoteEntry.js',
  },
  shared: {
    react: { singleton: true },
    'react-dom': { singleton: true },
  },
})
```

### 2. Import and Use LoginForm

```javascript
import React, { lazy, Suspense } from 'react';

const LoginForm = lazy(() => import('loginMfe/LoginForm'));

function App() {
  const handleLoginSuccess = (data) => {
    console.log('Login successful:', data);
    // Store token, redirect, etc.
  };

  return (
    <Suspense fallback={<div>Loading...</div>}>
      <LoginForm onLoginSuccess={handleLoginSuccess} />
    </Suspense>
  );
}
```

## Component API

### LoginForm Props

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| onLoginSuccess | function | No | Callback function called on successful login with response data |

### Response Data Structure

```javascript
{
  success: true,
  userId: "user-uuid-123",
  message: "Login successful",
  token: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## Form Validation Rules

- **Bank Code**: Must be exactly 3 digits
- **Branch Code**: Must be exactly 4 digits
- **Username**: Minimum 3 characters
- **Password**: Minimum 8 characters
- **Currency**: Must select SGD or USD

## API Integration

Connects to Login Service at: `http://localhost:8080/api/v1/auth/login`

Request format:
```json
{
  "bankCode": "101",
  "branchCode": "1119",
  "username": "john.doe",
  "password": "securePassword123",
  "currency": "SGD"
}
```

## Development

### Project Structure

```
login-mfe/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── LoginForm.jsx
│   │   └── LoginForm.css
│   └── index.js
├── package.json
├── webpack.config.js
└── README.md
```

### Standalone Testing

The MFE can run independently for development and testing. Access http://localhost:3001 to see the login form in standalone mode.

## CORS Configuration

Ensure the Login Service (port 8080) has CORS enabled to accept requests from the MFE origin (http://localhost:3001).

## Troubleshooting

**Module not loading in Host?**
- Verify the remote URL is correct
- Check that both Host and Remote are running
- Ensure React versions match

**CORS errors?**
- Configure CORS in the Login Service
- Check browser console for specific errors

**Validation not working?**
- Check browser console for JavaScript errors
- Verify form field names match the component

## Related Services

- **Login Service** (port 8080): Backend authentication API
- **Authentication Service** (port 8081): JWT token generation

## Technology Stack

- React 18.2
- Webpack 5 (Module Federation)
- CSS3
- Fetch API for HTTP requests
