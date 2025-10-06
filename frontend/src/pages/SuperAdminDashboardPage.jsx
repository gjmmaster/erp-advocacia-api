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
      setError('Failed to fetch tenants.');
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
    if (window.confirm(`Are you sure you want to delete tenant "${tenant.name}"? This action cannot be undone.`)) {
      try {
        await axios.delete(`/admin/tenants/${tenant.id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        fetchTenants(); // Refresh the list after deletion
      } catch (err) {
        setError('Failed to delete tenant.');
        console.error(err);
      }
    }
  };

  return (
    <div>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Super Admin Dashboard</h1>
        <button onClick={logout}>Logout</button>
      </header>

      <div style={{ margin: '1rem 0' }}>
        <button onClick={() => setCreateModalOpen(true)}>Provisionar Novo Escritório</button>
      </div>

      <h2>Tenants</h2>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {!loading && !error && <TenantsTable tenants={tenants} onEdit={handleEdit} onDelete={handleDelete} />}

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
  );
}

export default SuperAdminDashboardPage;
