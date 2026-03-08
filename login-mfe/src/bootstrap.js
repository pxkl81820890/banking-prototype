import React from 'react';
import ReactDOM from 'react-dom/client';
import LoginForm from './components/LoginForm.jsx';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <div style={{ padding: '20px' }}>
      <h1>Login Micro-Frontend (Standalone)</h1>
      <LoginForm />
    </div>
  </React.StrictMode>
);
