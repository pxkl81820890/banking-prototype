import React, { useState } from 'react';
import './LoginForm.css';

const LoginForm = ({ onLoginSuccess }) => {
  const [formData, setFormData] = useState({
    bankCode: '',
    branchCode: '',
    username: '',
    password: '',
    currency: 'SGD',
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const validateForm = () => {
    const newErrors = {};

    if (!formData.bankCode || !/^\d{3}$/.test(formData.bankCode)) {
      newErrors.bankCode = 'Bank code must be 3 digits';
    }

    if (!formData.branchCode || !/^\d{4}$/.test(formData.branchCode)) {
      newErrors.branchCode = 'Branch code must be 4 digits';
    }

    if (!formData.username || formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }

    if (!formData.password || formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }

    if (!formData.currency) {
      newErrors.currency = 'Currency is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/v1/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        setMessage('Login successful!');
        
        // Store form data temporarily for the host app to use
        localStorage.setItem('tempBankCode', formData.bankCode);
        localStorage.setItem('tempBranchCode', formData.branchCode);
        localStorage.setItem('tempCurrency', formData.currency);
        localStorage.setItem('username', formData.username);
        localStorage.setItem('bankCode', formData.bankCode);
        localStorage.setItem('branchCode', formData.branchCode);
        localStorage.setItem('currency', formData.currency);
        
        if (onLoginSuccess) {
          // Pass both API response and form data
          onLoginSuccess({
            ...data,
            username: formData.username,
            bankCode: formData.bankCode,
            branchCode: formData.branchCode,
            currency: formData.currency,
          });
        }
      } else {
        setMessage(data.message || 'Login failed');
      }
    } catch (error) {
      setMessage('Error connecting to server');
      console.error('Login error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-form-container">
      <form className="login-form" onSubmit={handleSubmit}>
        <h2>CTS BankWeb</h2>
        
        <div className="form-group">
          <label htmlFor="bankCode">Bank Code *</label>
          <input
            type="text"
            id="bankCode"
            name="bankCode"
            value={formData.bankCode}
            onChange={handleChange}
            placeholder="e.g., 101"
            maxLength="3"
            className={errors.bankCode ? 'error' : ''}
          />
          {errors.bankCode && <span className="error-message">{errors.bankCode}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="branchCode">Branch Code *</label>
          <input
            type="text"
            id="branchCode"
            name="branchCode"
            value={formData.branchCode}
            onChange={handleChange}
            placeholder="e.g., 1119"
            maxLength="4"
            className={errors.branchCode ? 'error' : ''}
          />
          {errors.branchCode && <span className="error-message">{errors.branchCode}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="username">Username *</label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="Enter username"
            className={errors.username ? 'error' : ''}
          />
          {errors.username && <span className="error-message">{errors.username}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="password">Password *</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Enter password"
            className={errors.password ? 'error' : ''}
          />
          {errors.password && <span className="error-message">{errors.password}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="currency">Currency *</label>
          <select
            id="currency"
            name="currency"
            value={formData.currency}
            onChange={handleChange}
            className={errors.currency ? 'error' : ''}
          >
            <option value="SGD">SGD - Singapore Dollar</option>
            <option value="USD">USD - US Dollar</option>
          </select>
          {errors.currency && <span className="error-message">{errors.currency}</span>}
        </div>

        {message && (
          <div className={`message ${message.includes('successful') ? 'success' : 'error'}`}>
            {message}
          </div>
        )}

        <button type="submit" disabled={loading} className="submit-button">
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
    </div>
  );
};

export default LoginForm;
