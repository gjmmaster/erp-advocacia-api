'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import TenantsTable from '@/components/TenantsTable';
import CreateTenantModal from '@/components/CreateTenantModal';
import EditTenantModal from '@/components/EditTenantModal';
import TenantCreatedModal from '@/components/TenantCreatedModal';
import type { Tenant } from '@/types/tenant';
import styles from './dashboard.module.css';

export default function SuperAdminDashboardPage() {
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isEditModalOpen, setEditModalOpen] = useState(false);
  const [editingTenant, setEditingTenant] = useState<Tenant | null>(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [createdTenant, setCreatedTenant] = useState<{
    tenantName: string;
    email: string;
    tempPassword: string;
    emailSent: boolean;
  } | null>(null);
  const router = useRouter();

  const fetchTenants = async () => {
    try {
      setLoading(true);
      setError('');
      
      const response = await fetch('/api/admin/tenants');
      
      if (!response.ok) {
        if (response.status === 401) {
          router.push('/super-admin/login');
          return;
        }
        throw new Error('Falha ao carregar escritórios');
      }

      const data = await response.json();
      setTenants(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao carregar escritórios');
      setTenants([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTenants();
  }, []);

  const handleEdit = (tenant: Tenant) => {
    setEditingTenant(tenant);
    setEditModalOpen(true);
  };

  const handleDelete = async (tenant: Tenant) => {
    const confirmMessage = `⚠️ ATENÇÃO: Tem certeza que deseja deletar o escritório "${tenant.company_name}"?\n\n` +
                          `Esta ação irá:\n` +
                          `• Deletar TODOS os dados do escritório\n` +
                          `• Remover TODOS os usuários associados\n` +
                          `• Apagar TODOS os processos jurídicos\n\n` +
                          `Esta ação NÃO PODE ser desfeita!\n\n` +
                          `Digite "DELETAR" para confirmar:`;
    
    const userInput = window.prompt(confirmMessage);
    
    if (userInput === 'DELETAR') {
      try {
        const response = await fetch(`/api/admin/tenants/${tenant.id}`, {
          method: 'DELETE',
        });

        if (!response.ok) {
          throw new Error('Falha ao deletar escritório');
        }

        alert('✅ Escritório deletado com sucesso!');
        await fetchTenants();
      } catch (err) {
        alert(`❌ Erro ao deletar: ${err instanceof Error ? err.message : 'Erro desconhecido'}`);
      }
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
      router.push('/super-admin/login');
    } catch (err) {
      console.error('Erro ao fazer logout:', err);
      router.push('/super-admin/login');
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerContent}>
          <h1 className={styles.title}>🏛️ Super Admin Dashboard</h1>
          <button onClick={handleLogout} className={styles.logoutButton}>
            🚪 Sair
          </button>
        </div>
      </header>

      <main className={styles.main}>
        <div className={styles.actions}>
          <button
            onClick={() => setCreateModalOpen(true)}
            className={styles.createButton}
          >
            ➕ Provisionar Novo Escritório
          </button>
        </div>

        {error && (
          <div className={styles.error}>
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className={styles.loading}>
            <div className={styles.spinner}></div>
            <p>Carregando escritórios...</p>
          </div>
        ) : (
          <TenantsTable
            tenants={tenants}
            onEdit={handleEdit}
            onDelete={handleDelete}
          />
        )}
      </main>

      <CreateTenantModal
        isOpen={isCreateModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onSuccess={(data) => {
          setCreatedTenant(data);
          setShowSuccessModal(true);
          setCreateModalOpen(false);
          fetchTenants();
        }}
      />

      <EditTenantModal
        isOpen={isEditModalOpen}
        tenant={editingTenant}
        onClose={() => {
          setEditModalOpen(false);
          setEditingTenant(null);
        }}
        onSuccess={() => {
          setEditModalOpen(false);
          setEditingTenant(null);
          fetchTenants();
        }}
      />

      {showSuccessModal && createdTenant && (
        <TenantCreatedModal
          isOpen={showSuccessModal}
          onClose={() => {
            setShowSuccessModal(false);
            setCreatedTenant(null);
          }}
          tenantName={createdTenant.tenantName}
          email={createdTenant.email}
          tempPassword={createdTenant.tempPassword}
          emailSent={createdTenant.emailSent}
        />
      )}
    </div>
  );
}
