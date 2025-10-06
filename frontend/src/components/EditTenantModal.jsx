import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import AuthContext from '../context/AuthContext';
import '../styles/Modal.css';

const EditTenantModal = ({ isOpen, onClose, onSuccess, tenant }) => {
  const [editableCompanyName, setEditableCompanyName] = useState('');
  const [editableOperatorLimit, setEditableOperatorLimit] = useState(4);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { token } = useContext(AuthContext);

  // Função para obter valor com suporte a chaves com namespace
  const getValue = (obj, key) => {
    if (!obj) return null;
    if (obj[key] !== undefined) return obj[key];
    if (obj[`tenants/${key}`] !== undefined) return obj[`tenants/${key}`];
    if (obj[`:tenants/${key}`] !== undefined) return obj[`:tenants/${key}`];
    return null;
  };

  useEffect(() => {
    if (tenant) {
      console.log('Tenant recebido para edição:', tenant);
      const companyName = getValue(tenant, 'company_name') || getValue(tenant, 'name');
      const operatorLimit = getValue(tenant, 'operator_limit');
      
      setEditableCompanyName(companyName || '');
      setEditableOperatorLimit(operatorLimit || 4);
      setError('');
    } else {
      setEditableCompanyName('');
      setEditableOperatorLimit(4);
      setError('');
    }
  }, [tenant]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!tenant) {
      setError('Erro: Dados do escritório não encontrados.');
      setLoading(false);
      return;
    }

    const tenantId = getValue(tenant, 'id');
    
    if (!tenantId) {
      setError('Erro: ID do escritório não encontrado.');
      setLoading(false);
      return;
    }

    try {
      console.log('Enviando dados para atualização:', {
        id: tenantId,
        company_name: editableCompanyName,
        operator_limit: editableOperatorLimit,
      });

      await axios.put(
        `/admin/tenants/${tenantId}`,
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
      
      console.log('Escritório atualizado com sucesso');
      alert('✅ Escritório atualizado com sucesso!');
      onSuccess();
      onClose();
    } catch (err) {
      console.error('Erro ao atualizar escritório:', err);
      const errorMsg = err.response?.data?.error || err.message || 'Falha ao atualizar escritório. Tente novamente.';
      setError(errorMsg);
      alert(`❌ Erro: ${errorMsg}`);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setError('');
      onClose();
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'N/A';
      return date.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return 'N/A';
    }
  };

  if (!isOpen || !tenant) {
    return null;
  }

  const tenantId = getValue(tenant, 'id');
  const subdomain = getValue(tenant, 'subdomain');
  const createdAt = getValue(tenant, 'created_at');

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">✏️ Editar Escritório</h2>
        </div>
        
        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-group">
              <label className="form-label">
                🆔 ID:
              </label>
              <input
                type="text"
                className="form-input"
                value={tenantId || 'N/A'}
                disabled
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                🌐 Subdomínio:
              </label>
              <input
                type="text"
                className="form-input"
                value={subdomain || 'N/A'}
                disabled
                style={{ fontFamily: 'monospace' }}
              />
              <div className="form-help">
                O subdomínio não pode ser alterado após a criação
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                📅 Criado em:
              </label>
              <input
                type="text"
                className="form-input"
                value={formatDate(createdAt)}
                disabled
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                🏢 Nome do Escritório:
              </label>
              <input
                type="text"
                className="form-input"
                value={editableCompanyName}
                onChange={(e) => setEditableCompanyName(e.target.value)}
                required
                disabled={loading}
                placeholder="Nome do escritório"
              />
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
                value={editableOperatorLimit}
                onChange={(e) => setEditableOperatorLimit(parseInt(e.target.value) || 1)}
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
              className="modal-btn modal-btn-success"
              disabled={loading}
            >
              {loading ? '⏳ Salvando...' : '💾 Salvar Alterações'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditTenantModal;
