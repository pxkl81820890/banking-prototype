import { useState, useEffect, Suspense, lazy } from 'react';
import './App.css';
import ErrorBoundary from './ErrorBoundary';

// Lazy load the remote components
const LoginForm = lazy(() => import('login_mfe/LoginForm'));
const Dashboard = lazy(() => import('dashboard_mfe/Dashboard'));

const App = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userData, setUserData] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // Check if user is already logged in on mount
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

  const handleLoginSuccess = (loginResponse) => {
    console.log('Login successful:', loginResponse);
    
    // Store authentication data
    localStorage.setItem('authToken', loginResponse.token);
    localStorage.setItem('userId', loginResponse.userId);
    localStorage.setItem('username', loginResponse.username);
    localStorage.setItem('bankCode', loginResponse.bankCode);
    localStorage.setItem('branchCode', loginResponse.branchCode);
    localStorage.setItem('currency', loginResponse.currency);
    
    const user = {
      userId: loginResponse.userId,
      username: loginResponse.username,
      bankCode: loginResponse.bankCode,
      branchCode: loginResponse.branchCode,
      currency: loginResponse.currency,
    };
    
    setToken(loginResponse.token);
    setUserData(user);
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    console.log('Logging out...');
    
    // Clear all stored data
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('bankCode');
    localStorage.removeItem('branchCode');
    localStorage.removeItem('currency');
    
    setToken(null);
    setUserData(null);
    setIsLoggedIn(false);
  };

  const handleViewCheques = () => {
    console.log('Navigating to cheques viewer...');
    // TODO: Navigate to cheques viewer MFE when available
    alert('Cheques viewer will be available soon!');
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <div className="app-container">
        <Suspense fallback={
          <div className="loading-container">
            <div className="spinner"></div>
            <p>Loading application...</p>
          </div>
        }>
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
    </ErrorBoundary>
  );
};

export default App;
