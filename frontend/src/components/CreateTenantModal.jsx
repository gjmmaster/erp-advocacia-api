import axios from 'axios';
import React, { useContext, useState } from 'react';
import AuthContext from '../context/AuthContext';
import '../styles/Modal.css';

const CreateTenantModal = ({ isOpen, onClose, onSuccess }) => {
  const [companyName, setCompanyName] = useState('');
  const [masterEmail, setMasterEmail] = useState('');
  const [operatorLimit, setOperatorLimit] = useState(4);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const { token } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

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
      
      // Mostra mensagem de sucesso SEM expor a senha temporária
      setSuccess(`✅ Escritório criado com sucesso!\n\n📧 Um e-mail de boas-vindas com as instruções de acesso foi enviado para:\n${masterEmail}\n\n⚠️ O administrador deve verificar a caixa de entrada (e spam) para obter as credenciais de acesso.`);
      
      // Limpa os campos após 5 segundos
      setTimeout(() => {
        setCompanyName('');
        setMasterEmail('');
        setOperatorLimit(4);
        setSuccess('');
        setError('');
        onSuccess();
        onClose();
      }, 5000);
      
    } catch (err) {
      // Removido console.error para produção
      setError(err.response?.data?.error || 'Falha ao provisionar escritório. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setCompanyName('');
      setMasterEmail('');
      setOperatorLimit(4);
      setError('');
      setSuccess('');
      onClose();
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">➕ Provisionar Novo Escritório</h2>
        </div>
        
        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-group">
              <label className="form-label">
                🏢 Nome do Escritório:
              </label>
              <input
                type="text"
                className="form-input"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                required
                disabled={loading}
                placeholder="Ex: Advocacia Silva & Associados"
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                👤 E-mail do Administrador (Master):
              </label>
              <input
                type="email"
                className="form-input"
                value={masterEmail}
                onChange={(e) => setMasterEmail(e.target.value)}
                required
                disabled={loading}
                placeholder="admin@escritorio.com"
              />
              <div className="form-help">
                Este será o usuário principal do escritório
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                👥 Limite de Operadores:
              </label>
              <input
                type="number"
                className="form-input"
                min="1"
                max="100"
                value={operatorLimit}
                onChange={(e) => setOperatorLimit(parseInt(e.target.value) || 1)}
                required
                disabled={loading}
              />
              <div className="form-help">
                Número máximo de operadores que este escritório pode criar
              </div>
            </div>

            {error && (
              <div className="error-message-modal">
                <span>⚠️</span>
                <span>{error}</span>
              </div>
            )}

            {success && (
              <div className="success-message">
                <span>✅</span>
                <div style={{ whiteSpace: 'pre-line' }}>{success}</div>
              </div>
            )}
          </div>

          <div className="modal-actions">
            <button 
              type="button" 
              className="modal-btn modal-btn-cancel"
              onClick={handleClose}
              disabled={loading}
            >
              ❌ Cancelar
            </button>
            <button 
              type="submit"
              className="modal-btn modal-btn-primary"
              disabled={loading}
            >
              {loading ? '⏳ Criando...' : '✅ Provisionar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateTenantModal;
