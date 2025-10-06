import axios from 'axios';
import React, { useContext, useState } from 'react';
import AuthContext from '../context/AuthContext';

const CreateTenantModal = ({ isOpen, onClose, onSuccess }) => {
  const [companyName, setCompanyName] = useState('');
  const [masterEmail, setMasterEmail] = useState('');
  const [operatorLimit, setOperatorLimit] = useState(4);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { token } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      const response = await axios.post(
        '/admin/provision-tenant',
        {
          company_name: companyName,
          email: masterEmail,
          operator_limit: operatorLimit,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // Mostra mensagem de sucesso com a senha temporária
      if (response.data.user && response.data.user.temp_password) {
        setSuccess(`Escritório criado com sucesso! Senha temporária: ${response.data.user.temp_password}`);
      } else {
        setSuccess('Escritório criado com sucesso!');
      }
      
      // Limpa os campos após 3 segundos
      setTimeout(() => {
        setCompanyName('');
        setMasterEmail('');
        setOperatorLimit(4);
        setSuccess('');
        onSuccess();
        onClose();
      }, 3000);
      
    } catch (err) {
      setError(err.response?.data?.error || 'Falha ao provisionar escritório. Tente novamente.');
      console.error(err);
    }
  };

  if (!isOpen) {
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
        <h2 style={{ marginTop: 0 }}>Provisionar Novo Escritório</h2>
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
              Nome do Escritório:
            </label>
            <input
              type="text"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
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
              E-mail do Administrador (Master):
            </label>
            <input
              type="email"
              value={masterEmail}
              onChange={(e) => setMasterEmail(e.target.value)}
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
              value={operatorLimit}
              onChange={(e) => setOperatorLimit(parseInt(e.target.value))}
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
          {success && <p style={{ color: '#4CAF50', marginBottom: '1rem', fontWeight: '500' }}>{success}</p>}
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
                backgroundColor: '#2196F3',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              Provisionar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateTenantModal;
