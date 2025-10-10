import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import CreateTenantModal from '../components/CreateTenantModal';
import EditTenantModal from '../components/EditTenantModal';
import TenantsTable from '../components/TenantsTable';
import AuthContext from '../context/AuthContext';
import '../styles/Dashboard.css';

function SuperAdminDashboardPage() {
  const [tenants, setTenants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isEditModalOpen, setEditModalOpen] = useState(false);
  const [editingTenant, setEditingTenant] = useState(null);
  const { token, logout } = useContext(AuthContext);

  const fetchTenants = async () => {
    try {
      setLoading(true);
      setError('');
      const { data } = await axios.get('/admin/tenants', {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTenants(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Erro ao carregar escritórios:', err);
      setError(err.response?.data?.error || 'Falha ao carregar escritórios. Tente novamente.');
      setTenants([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (token) {
      fetchTenants();
    }
  }, [token]);

  const handleEdit = (tenant) => {
    setEditingTenant(tenant);
    setEditModalOpen(true);
  };

  const getValue = (obj, key) => {
    if (!obj) return null;
    if (obj[key] !== undefined) return obj[key];
    if (obj[`tenants/${key}`] !== undefined) return obj[`tenants/${key}`];
    if (obj[`:tenants/${key}`] !== undefined) return obj[`:tenants/${key}`];
    return null;
  };

  const handleDelete = async (tenant) => {
    const tenantId = getValue(tenant, 'id');
    const tenantName = getValue(tenant, 'company_name') || getValue(tenant, 'name') || `ID ${tenantId}`;
    
    if (!tenantId) {
      alert('❌ Erro: ID do escritório não encontrado.');
      return;
    }
    
    const confirmMessage = `⚠️ ATENÇÃO: Tem certeza que deseja deletar o escritório "${tenantName}"?\n\n` +
                          `Esta ação irá:\n` +
                          `• Deletar TODOS os dados do escritório\n` +
                          `• Remover TODOS os usuários associados\n` +
                          `• Apagar TODOS os processos jurídicos\n\n` +
                          `Esta ação NÃO PODE ser desfeita!\n\n` +
                          `Digite "DELETAR" para confirmar:`;
    
    const userInput = window.prompt(confirmMessage);
    
    if (userInput === 'DELETAR') {
      try {
        const url = `/admin/tenants/${tenantId}`;
        
        await axios.delete(url, {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        alert('✅ Escritório deletado com sucesso!');
        await fetchTenants();
      } catch (err) {
        console.error('Response:', err.response);
        console.error('Response data:', err.response?.data);
        console.error('Response status:', err.response?.status);
        console.error('Response headers:', err.response?.headers);
        console.error('Request config:', err.config);
        const errorMsg = err.response?.data?.error || err.message || 'Falha ao deletar escritório. Tente novamente.';
        setError(errorMsg);
        alert(`❌ Erro ao deletar: ${errorMsg}`);
      }
    } else if (userInput !== null) {
      alert('❌ Confirmação incorreta. Escritório não foi deletado.');
    }
  };

  const handleCloseError = () => {
    setError('');
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-content">
        <header className="dashboard-header">
          <div>
            <h1 className="dashboard-title">🏢 Dashboard do Super Admin</h1>
            <p className="dashboard-subtitle">
              Gerencie todos os escritórios do sistema
            </p>
          </div>
          <button className="logout-btn" onClick={logout}>
            🚪 Sair
          </button>
        </header>

        <div className="dashboard-body">
          <div className="section-header">
            <div>
              <h2 className="section-title">📋 Escritórios</h2>
              <p className="section-subtitle">
                {loading ? 'Carregando...' : `Total: ${tenants.length} escritório${tenants.length !== 1 ? 's' : ''}`}
              </p>
            </div>
            <button 
              className="primary-btn"
              onClick={() => setCreateModalOpen(true)}
              disabled={loading}
            >
              <span>➕</span>
              Provisionar Novo Escritório
            </button>
          </div>

          {error && (
            <div className="error-message">
              <span>⚠️</span>
              <span>{error}</span>
              <button 
                onClick={handleCloseError}
                style={{ 
                  marginLeft: 'auto', 
                  background: 'none', 
                  border: 'none', 
                  color: 'white', 
                  cursor: 'pointer',
                  fontSize: '1.2rem'
                }}
              >
                ✕
              </button>
            </div>
          )}

          {loading && (
            <div className="loading-state">
              <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>⏳</div>
              <p>Carregando escritórios...</p>
            </div>
          )}

          {!loading && !error && tenants.length === 0 && (
            <div className="empty-state">
              <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🏢</div>
              <h3>Nenhum escritório cadastrado</h3>
              <p>Comece criando o primeiro escritório do sistema.</p>
              <button 
                className="primary-btn"
                onClick={() => setCreateModalOpen(true)}
              >
                <span>➕</span>
                Criar Primeiro Escritório
              </button>
            </div>
          )}

          {!loading && tenants.length > 0 && (
            <TenantsTable 
              tenants={tenants} 
              onEdit={handleEdit} 
              onDelete={handleDelete} 
            />
          )}
        </div>

        <CreateTenantModal
          isOpen={isCreateModalOpen}
          onClose={() => setCreateModalOpen(false)}
          onSuccess={fetchTenants}
        />

        <EditTenantModal
          isOpen={isEditModalOpen}
          onClose={() => setEditModalOpen(false)}
          onSuccess={fetchTenants}
          tenant={editingTenant}
        />
      </div>
    </div>
  );
}

export default SuperAdminDashboardPage;
