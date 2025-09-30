import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import AuthContext from '../context/AuthContext';

const EditTenantModal = ({ isOpen, onClose, onSuccess, tenant }) => {
  const [tenantName, setTenantName] = useState('');
  const [error, setError] = useState('');
  const { token } = useContext(AuthContext);

  useEffect(() => {
    if (tenant) {
      setTenantName(tenant.name);
    }
  }, [tenant]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!tenant) return;

    try {
      await axios.put(
        `/admin/tenants/${tenant.id}`,
        {
          name: tenantName,
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
      setError('Failed to update tenant. Please try again.');
      console.error(err);
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal">
      <div className="modal-content">
        <h2>Edit Tenant</h2>
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
          {error && <p style={{ color: 'red' }}>{error}</p>}
          <button type="submit">Update</button>
          <button type="button" onClick={onClose}>Cancel</button>
        </form>
      </div>
    </div>
  );
};

export default EditTenantModal;