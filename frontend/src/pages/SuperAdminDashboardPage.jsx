import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import CreateTenantModal from '../components/CreateTenantModal';
import EditTenantModal from '../components/EditTenantModal';
import TenantsTable from '../components/TenantsTable';
import AuthContext from '../context/AuthContext';

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
      const { data } = await axios.get('/admin/tenants', {
        headers: { Authorization: `Bearer ${token}` },
      });
      setTenants(data);
      setError('');
    } catch (err) {
      setError('Falha ao carregar escritórios. Tente novamente.');
      console.error(err);
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

  const handleDelete = async (tenant) => {
    const tenantName = tenant.company_name || tenant.name;
    if (window.confirm(`Tem certeza que deseja deletar o escritório "${tenantName}"? Esta ação não pode ser desfeita e todos os dados associados serão perdidos.`)) {
      try {
        await axios.delete(`/admin/tenants/${tenant.id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        fetchTenants();
      } catch (err) {
        setError(err.response?.data?.error || 'Falha ao deletar escritório. Tente novamente.');
        console.error(err);
      }
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f5f5f5',
      padding: '2rem'
    }}>
      <div style={{
        maxWidth: '1400px',
        margin: '0 auto',
        backgroundColor: 'white',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        padding: '2rem'
      }}>
        <header style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '2rem',
          paddingBottom: '1rem',
          borderBottom: '2px solid #f0f0f0'
        }}>
          <div>
            <h1 style={{ margin: 0, color: '#333' }}>Dashboard do Super Admin</h1>
            <p style={{ margin: '0.5rem 0 0 0', color: '#666', fontSize: '14px' }}>
              Gerencie todos os escritórios do sistema
            </p>
          </div>
          <button 
            onClick={logout}
            style={{
              padding: '10px 20px',
              backgroundColor: '#f44336',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontWeight: '500',
              fontSize: '14px'
            }}
          >
            Sair
          </button>
        </header>

        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '1.5rem'
        }}>
          <div>
            <h2 style={{ margin: 0, color: '#333' }}>Escritórios</h2>
            <p style={{ margin: '0.5rem 0 0 0', color: '#666', fontSize: '14px' }}>
              Total: {tenants.length} escritório{tenants.length !== 1 ? 's' : ''}
            </p>
          </div>
          <button 
            onClick={() => setCreateModalOpen(true)}
            style={{
              padding: '12px 24px',
              backgroundColor: '#2196F3',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontWeight: '500',
              fontSize: '14px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
          >
            <span style={{ fontSize: '18px' }}>+</span>
            Provisionar Novo Escritório
          </button>
        </div>

        {loading && (
          <div style={{ textAlign: 'center', padding: '3rem', color: '#666' }}>
            <p>Carregando escritórios...</p>
          </div>
        )}
        
        {error && (
          <div style={{
            padding: '1rem',
            backgroundColor: '#ffebee',
            border: '1px solid #f44336',
            borderRadius: '4px',
            color: '#c62828',
            marginBottom: '1rem'
          }}>
            {error}
          </div>
        )}
        
        {!loading && !error && tenants.length === 0 && (
          <div style={{
            textAlign: 'center',
            padding: '3rem',
            color: '#666',
            backgroundColor: '#f9f9f9',
            borderRadius: '4px'
          }}>
            <p style={{ fontSize: '16px', marginBottom: '1rem' }}>
              Nenhum escritório cadastrado ainda.
            </p>
            <button 
              onClick={() => setCreateModalOpen(true)}
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
              Criar Primeiro Escritório
            </button>
          </div>
        )}
        
        {!loading && !error && tenants.length > 0 && (
          <TenantsTable tenants={tenants} onEdit={handleEdit} onDelete={handleDelete} />
        )}

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
