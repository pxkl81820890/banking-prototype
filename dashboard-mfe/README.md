# Dashboard Micro-Frontend (MFE)

A React-based dashboard micro-frontend using Webpack 5 Module Federation. This MFE displays after successful login and provides access to banking features.

## Features

- **User Information Display**: Shows logged-in user details (username, bank code, branch code, currency)
- **Archived Cheques Button**: Navigate to view archived cheque images
- **Logout Button**: Clear session and return to login
- **Account Summary**: Display user account details
- **Quick Actions**: Placeholder buttons for common banking operations

## Architecture

- **Type**: Remote MFE (exposed via Module Federation)
- **Port**: 3002
- **Exposed Module**: `./Dashboard` → `./src/components/Dashboard.jsx`
- **Shared Dependencies**: React, React-DOM (singletons)

## Installation

```bash
cd dashboard-mfe
npm install
```

## Running Locally

```bash
npm start
```

The dashboard will be available at: http://localhost:3002

## Module Federation Configuration

This MFE exposes the Dashboard component that can be consumed by a Host application:

```javascript
// In Host application webpack.config.js
remotes: {
  dashboard_mfe: 'dashboard_mfe@http://localhost:3002/remoteEntry.js',
}
```

## Component Props

The Dashboard component accepts the following props:

```javascript
<Dashboard 
  user={{
    username: string,
    userId: string,
    bankCode: string,
    branchCode: string,
    currency: string
  }}
  token={string}
  onLogout={function}
  onViewCheques={function}
/>
```

### Props Description

- `user`: Object containing user information from login
- `token`: JWT authentication token
- `onLogout`: Callback function when user clicks logout
- `onViewCheques`: Callback function when user clicks "View Archived Cheques"

## Integration with Login MFE

After successful login, the Host application should:

1. Store the JWT token and user info
2. Navigate to the Dashboard MFE
3. Pass user data and callbacks as props

Example:

```javascript
import Dashboard from 'dashboard_mfe/Dashboard';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userData, setUserData] = useState(null);

  const handleLoginSuccess = (user, token) => {
    localStorage.setItem('authToken', token);
    localStorage.setItem('userId', user.userId);
    setUserData(user);
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    setUserData(null);
    setIsLoggedIn(false);
  };

  const handleViewCheques = () => {
    // Navigate to cheques MFE
    console.log('Navigate to cheques viewer');
  };

  return (
    <div>
      {!isLoggedIn ? (
        <LoginForm onLoginSuccess={handleLoginSuccess} />
      ) : (
        <Dashboard 
          user={userData}
          token={localStorage.getItem('authToken')}
          onLogout={handleLogout}
          onViewCheques={handleViewCheques}
        />
      )}
    </div>
  );
}
```

## Styling

The dashboard uses a modern gradient design with:
- Purple gradient background (#667eea to #764ba2)
- Card-based layout with hover effects
- Responsive design for mobile and desktop
- Clean, professional banking aesthetic

## Development

To test the dashboard standalone:

```bash
npm start
```

Visit http://localhost:3002 to see the dashboard with mock data.

## Build for Production

```bash
npm run build
```

The production build will be in the `dist` folder.

## Next Steps

- Integrate with Host application
- Connect to Cheques Viewer MFE
- Add real-time account data
- Implement additional quick actions
