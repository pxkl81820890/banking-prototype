# Banking Platform Host Application

The Host application that orchestrates the Login and Dashboard micro-frontends using Webpack 5 Module Federation.

## Overview

This is the main container application that:
- Loads the Login MFE (port 3001)
- Loads the Dashboard MFE (port 3002)
- Manages authentication state
- Handles navigation between MFEs
- Persists user session in localStorage

## Architecture

```
┌─────────────────────────────────────────┐
│         Host App (Port 3000)            │
│  ┌───────────────────────────────────┐  │
│  │   Authentication State Manager    │  │
│  └───────────────────────────────────┘  │
│              │                           │
│    ┌─────────┴─────────┐                │
│    │                   │                 │
│    ▼                   ▼                 │
│  ┌──────────┐    ┌──────────┐           │
│  │ Login    │    │Dashboard │           │
│  │ MFE      │    │ MFE      │           │
│  │(3001)    │    │(3002)    │           │
│  └──────────┘    └──────────┘           │
└─────────────────────────────────────────┘
```

## Prerequisites

Before running the Host app, ensure these MFEs are running:

1. **Login MFE** on port 3001
2. **Dashboard MFE** on port 3002

## Installation

```bash
cd host-app
npm install
```

## Running the Application

### Step 1: Start the Remote MFEs

**Terminal 1 - Login MFE:**
```bash
cd login-mfe
npm start
```

**Terminal 2 - Dashboard MFE:**
```bash
cd dashboard-mfe
npm start
```

### Step 2: Start the Host App

**Terminal 3 - Host App:**
```bash
cd host-app
npm start
```

### Step 3: Access the Application

Open your browser and navigate to: **http://localhost:3000**

## User Flow

1. **Initial Load**: User sees the Login MFE
2. **Login**: User enters credentials and submits
3. **Authentication**: Login MFE calls backend API
4. **Success**: Host app receives login data and stores in localStorage
5. **Navigation**: Host app switches to Dashboard MFE
6. **Dashboard**: User sees dashboard with their information
7. **Logout**: User clicks logout, Host app clears state and returns to Login

## State Management

The Host app manages the following state:

### Authentication State
- `isLoggedIn`: Boolean indicating if user is authenticated
- `userData`: Object containing user information
- `token`: JWT authentication token

### LocalStorage Keys
- `authToken`: JWT token
- `userId`: User ID
- `username`: Username
- `bankCode`: Bank code
- `branchCode`: Branch code
- `currency`: Currency code

## Module Federation Configuration

### Remotes
```javascript
remotes: {
  login_mfe: 'login_mfe@http://localhost:3001/remoteEntry.js',
  dashboard_mfe: 'dashboard_mfe@http://localhost:3002/remoteEntry.js',
}
```

### Shared Dependencies
- React (singleton, not eager)
- React-DOM (singleton, not eager)

## Features

### Session Persistence
- User sessions persist across page refreshes
- Automatic restoration of authentication state on reload

### Error Handling
- Graceful fallback UI during MFE loading
- Loading spinners for better UX

### Logout Flow
- Clears all localStorage data
- Resets application state
- Returns to login screen

## Development

### Hot Module Replacement
The dev server supports HMR for fast development:
```bash
npm start
```

### Build for Production
```bash
npm run build
```

## Troubleshooting

### MFE Not Loading
**Problem**: "Uncaught Error: Shared module is not available for eager consumption"

**Solution**: 
1. Ensure all MFEs are running on their correct ports
2. Check that bootstrap pattern is used in all MFEs
3. Verify shared dependencies are configured correctly

### Login Not Working
**Problem**: Login succeeds but dashboard doesn't show

**Solution**:
1. Check browser console for errors
2. Verify localStorage has `authToken` and `userId`
3. Ensure Dashboard MFE is running on port 3002

### CORS Errors
**Problem**: CORS errors when calling backend APIs

**Solution**:
1. Ensure backend services have CORS configured
2. Check that WebConfig allows origins: http://localhost:3000, http://localhost:3001
3. Verify backend services are running

## Port Configuration

| Application | Port | URL |
|------------|------|-----|
| Host App | 3000 | http://localhost:3000 |
| Login MFE | 3001 | http://localhost:3001 |
| Dashboard MFE | 3002 | http://localhost:3002 |
| Login Service | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |

## Testing the Complete Flow

1. Start all services (2 MFEs + Host + 2 Backend services)
2. Open http://localhost:3000
3. Login with test credentials:
   - Bank Code: 101
   - Branch Code: 1119
   - Username: testuser
   - Password: password123
   - Currency: SGD
4. Verify dashboard appears with user information
5. Click "View Archived Cheques" (placeholder alert)
6. Click "Logout" to return to login

## Next Steps

- Add routing library (React Router) for better navigation
- Implement Cheques Viewer MFE
- Add error boundaries for better error handling
- Implement token refresh mechanism
- Add loading states for API calls
