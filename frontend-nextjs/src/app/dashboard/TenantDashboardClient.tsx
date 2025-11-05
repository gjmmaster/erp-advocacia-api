'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { DashboardLayout, StatCard, Card, CardHeader, CardBody } from '@/components';
import styles from './dashboard.module.css';

interface TenantDashboardClientProps {
  session: any;
  tenantName: string;
  tenantId: number;
  impersonating: boolean;
  impersonatorEmail?: string;
}

export default function TenantDashboardClient({
  session,
  tenantName,
  tenantId,
  impersonating,
  impersonatorEmail
}: TenantDashboardClientProps) {
  const [stats, setStats] = useState({
    processos: 0,
    clientes: 0,
    operadores: 0
  });
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  const navItems = [
    { href: '/dashboard', icon: 'fas fa-chart-line', label: 'Dashboard' },
    { href: '/dashboard/processos', icon: 'fas fa-folder', label: 'Processos' },
    { href: '/dashboard/clientes', icon: 'fas fa-users', label: 'Clientes' },
    { href: '/dashboard/operadores', icon: 'fas fa-user-tie', label: 'Operadores' },
    { href: '/dashboard/perfil', icon: 'fas fa-user-circle', label: 'Perfil' }
  ];

  useEffect(() => {
    fetchStats();
  }, [tenantId]);

  const fetchStats = async () => {
    try {
      const response = await fetch('/api/tenant/dashboard/stats');
      if (response.ok) {
        const data = await response.json();
        setStats(data);
      }
    } catch (error) {
      console.error('Erro ao carregar estatísticas:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
      router.push('/login');
    } catch (err) {
      console.error('Erro ao fazer logout:', err);
      router.push('/login');
    }
  };

  return (
    <DashboardLayout
      navItems={navItems}
      userName={session.email?.split('@')[0] || 'Usuário'}
      userRole={tenantName}
      onLogout={handleLogout}
    >
      {/* Banner de Impersonation */}
      {impersonating && impersonatorEmail && (
        <div className={styles.adminBanner}>
          <div className={styles.adminBannerContent}>
            <div className={styles.adminBannerInfo}>
              <i className="fas fa-exclamation-triangle"></i>
              <div>
                <strong>MODO ADMINISTRADOR</strong>
                <span className={styles.adminBannerEmail}>
                  Acessando como: {session.email}
                </span>
              </div>
            </div>
            <button
              className={styles.btnReturnAdmin}
              onClick={async () => {
                await fetch('/api/admin/stop-impersonate', { method: 'POST' });
                router.push('/super-admin/dashboard');
              }}
            >
              <i className="fas fa-arrow-left"></i>
              <span>Voltar para Super Admin</span>
            </button>
          </div>
        </div>
      )}

      <div className={styles.pageHeader}>
        <div>
          <h2 className={styles.pageTitle}>Dashboard</h2>
          <p className={styles.pageSubtitle}>Visão geral do escritório</p>
        </div>
      </div>

      <div className={styles.statsGrid}>
        <StatCard
          icon="fas fa-folder"
          value={loading ? '...' : stats.processos}
          label="Processos Ativos"
          color="blue"
        />
        <StatCard
          icon="fas fa-users"
          value={loading ? '...' : stats.clientes}
          label="Clientes"
          color="green"
        />
        <StatCard
          icon="fas fa-user-tie"
          value={loading ? '...' : stats.operadores}
          label="Operadores"
          color="orange"
        />
      </div>

      <Card hover>
        <CardHeader>
          <div className={styles.cardTitle}>
            <i className="fas fa-clock"></i>
            <span>Processos Recentes</span>
          </div>
        </CardHeader>
        <CardBody>
          <div className={styles.comingSoon}>
            <i className="fas fa-folder"></i>
            <h3>Em Desenvolvimento</h3>
            <p>Lista de processos recentes em breve...</p>
          </div>
        </CardBody>
      </Card>
    </DashboardLayout>
  );
}
