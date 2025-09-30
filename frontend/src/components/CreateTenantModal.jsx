import axios from 'axios';
import React, { useContext, useState } from 'react';
import AuthContext from '../context/AuthContext';

const CreateTenantModal = ({ isOpen, onClose, onSuccess }) => {
  const [tenantName, setTenantName] = useState('');
  const [masterEmail, setMasterEmail] = useState('');
  const [masterPassword, setMasterPassword] = useState('');
  const [error, setError] = useState('');
  const { token } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      await axios.post(
        '/admin/provision-tenant',
        {
          tenant_name: tenantName,
          master_user_email: masterEmail,
          master_user_password: masterPassword,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      onSuccess(); // Refresh the tenants list
      onClose();   // Close the modal
    } catch (err) {
      setError('Failed to provision tenant. Please try again.');
      console.error(err);
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal">
      <div className="modal-content">
        <h2>Provision New Tenant</h2>
        <form onSubmit={handleSubmit}>
          <div>
            <label>Tenant Name:</label>
            <input
              type="text"
              value={tenantName}
              onChange={(e) => setTenantName(e.target.value)}
              required
            />
          </div>
          <div>
            <label>Master User Email:</label>
            <input
              type="email"
              value={masterEmail}
              onChange={(e) => setMasterEmail(e.target.value)}
              required
            />
          </div>
          <div>
            <label>Master User Password:</label>
            <input
              type="password"
              value={masterPassword}
              onChange={(e) => setMasterPassword(e.target.value)}
              required
            />
          </div>
          {error && <p style={{ color: 'red' }}>{error}</p>}
          <button type="submit">Provision</button>
          <button type="button" onClick={onClose}>Cancel</button>
        </form>
      </div>
    </div>
  );
};

export default CreateTenantModal;