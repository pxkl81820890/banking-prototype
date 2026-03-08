import { useState, useEffect } from 'react';
import './Dashboard.css';

const Dashboard = ({ user, token, onLogout, onViewCheques }) => {
  const [featureFlags, setFeatureFlags] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Fetch feature flags from channel-configurations-service
    const fetchFeatureFlags = async () => {
      try {
        const response = await fetch('http://localhost:8082/api/v1/feature-flags', {
          headers: {
            'USER_ID': user.userId
          }
        });
        
        if (!response.ok) {
          throw new Error('Failed to fetch feature flags');
        }
        
        const data = await response.json();
        console.log('Feature flags response:', data);
        setFeatureFlags(data.featureFlags || {});
        setLoading(false);
      } catch (err) {
        console.error('Error fetching feature flags:', err);
        setError(err.message);
        setLoading(false);
      }
    };

    if (user && user.userId) {
      fetchFeatureFlags();
    }
  }, [user]);

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    
    if (onLogout) {
      onLogout();
    }
  };

  const handleViewArchivedImages = () => {
    console.log('Viewing archived images...');
    alert('Archived Images viewer will be available soon!');
  };

  const handleViewReports = () => {
    console.log('Viewing reports...');
    alert('Reports viewer will be available soon!');
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>CTS BankWeb</h1>
          <div className="user-info">
            <span className="user-name">Welcome, {user?.username || 'User'}</span>
            <button className="logout-btn" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="dashboard-grid">
          <div className="dashboard-card">
            <div className="card-icon">📊</div>
            <h2>Account Summary</h2>
            <div className="account-details">
              <div className="detail-row">
                <span>User ID:</span>
                <span>{user?.userId || 'N/A'}</span>
              </div>
              <div className="detail-row">
                <span>Bank Code:</span>
                <span>{user?.bankCode || 'N/A'}</span>
              </div>
              <div className="detail-row">
                <span>Branch Code:</span>
                <span>{user?.branchCode || 'N/A'}</span>
              </div>
              <div className="detail-row">
                <span>Currency:</span>
                <span>{user?.currency || 'N/A'}</span>
              </div>
            </div>
          </div>

          {loading && (
            <div className="dashboard-card">
              <p>Loading features...</p>
            </div>
          )}

          {error && (
            <div className="dashboard-card error-card">
              <p>Error loading features: {error}</p>
            </div>
          )}

          {!loading && featureFlags.isArchiveEnquiryEnabled && (
            <div className="dashboard-card action-card">
              <div className="card-icon">🏦</div>
              <h2>Archived Images</h2>
              <p>View and manage your archived cheque images</p>
              <button className="primary-btn" onClick={handleViewArchivedImages}>
                View Archived Images
              </button>
            </div>
          )}

          {!loading && featureFlags.isReportsEnabled && (
            <div className="dashboard-card action-card">
              <div className="card-icon">📈</div>
              <h2>Reports</h2>
              <p>View detailed reports and analytics</p>
              <button className="primary-btn" onClick={handleViewReports}>
                View Reports
              </button>
            </div>
          )}

          {!loading && !featureFlags.isArchiveEnquiryEnabled && !featureFlags.isReportsEnabled && !error && (
            <div className="dashboard-card">
              <div className="card-icon">🔒</div>
              <h2>No Features Available</h2>
              <p>Contact your administrator to enable features for your account.</p>
            </div>
          )}
        </div>
      </main>

      <footer className="dashboard-footer">
        <p>&copy; 2026 Banking Platform. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default Dashboard;
