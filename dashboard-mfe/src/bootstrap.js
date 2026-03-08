import React from 'react';
import ReactDOM from 'react-dom/client';
import Dashboard from './components/Dashboard';

const root = ReactDOM.createRoot(document.getElementById('root'));

// For standalone testing
const mockUser = {
  username: 'john.doe',
  userId: 'user-123',
  bankCode: '101',
  branchCode: '1119',
  currency: 'SGD'
};

root.render(
  <React.StrictMode>
    <Dashboard 
      user={mockUser}
      token="mock-token"
      onLogout={() => console.log('Logout clicked')}
      onViewCheques={() => console.log('View cheques clicked')}
    />
  </React.StrictMode>
);
