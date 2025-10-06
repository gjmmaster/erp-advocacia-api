import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import AuthContext from '../context/AuthContext';

const EditTenantModal = ({ isOpen, onClose, onSuccess, tenant }) => {
  const [editableCompanyName, setEditableCompanyName] = useState('');
  const [editableOperatorLimit, setEditableOperatorLimit] = useState(4);
  const [error, setError] = useState('');
  const { token } = useContext(AuthContext);

  useEffect(() => {
    if (tenant) {
      setEditableCompanyName(tenant.company_name || tenant.name || '');
      setEditableOperatorLimit(tenant.operator_limit || 4);
    } else {
      setEditableCompanyName('');
      setEditableOperatorLimit(4);
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
          company_name: editableCompanyName,
          operator_limit: editableOperatorLimit,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      onSuccess();
      onClose();
    } catch (err) {
      setError(err.response?.data?.error || 'Falha ao atualizar escritório. Tente novamente.');
      console.error(err);
    }
  };

  if (!isOpen || !tenant) {
    return null;
  }

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.5)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '8px',
        maxWidth: '500px',
        width: '90%',
        maxHeight: '90vh',
        overflowY: 'auto'
      }}>
        <h2 style={{ marginTop: 0 }}>Editar Escritório</h2>
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
              ID:
            </label>
            <input
              type="text"
              value={tenant.id}
              disabled
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
                backgroundColor: '#f5f5f5',
                color: '#666'
              }}
            />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
              Subdomínio:
            </label>
            <input
              type="text"
              value={tenant.subdomain}
              disabled
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
                backgroundColor: '#f5f5f5',
                color: '#666',
                fontFamily: 'monospace'
              }}
            />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
              Nome do Escritório:
            </label>
            <input
              type="text"
              value={editableCompanyName}
              onChange={(e) => setEditableCompanyName(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px'
              }}
            />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
              Limite de Operadores:
            </label>
            <input
              type="number"
              min="1"
              max="100"
              value={editableOperatorLimit}
              onChange={(e) => setEditableOperatorLimit(parseInt(e.target.value))}
              required
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px'
              }}
            />
            <small style={{ color: '#666', fontSize: '12px' }}>
              Número máximo de operadores que este escritório pode criar
            </small>
          </div>
          {error && <p style={{ color: '#f44336', marginBottom: '1rem' }}>{error}</p>}
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button 
              type="button" 
              onClick={onClose}
              style={{
                padding: '10px 20px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #ddd',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Cancelar
            </button>
            <button 
              type="submit"
              style={{
                padding: '10px 20px',
                backgroundColor: '#4CAF50',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              Atualizar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditTenantModal;
