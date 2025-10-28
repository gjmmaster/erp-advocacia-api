'use client';

import { ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import type { UserSession } from '@/types/auth';
import styles from './DashboardLayout.module.css';

interface DashboardLayoutProps {
  children: ReactNode;
  user: UserSession;
  tenantName?: string;
}

export default function DashboardLayout({ children, user, tenantName }: DashboardLayoutProps) {
  const router = useRouter();
  const isAdmin = user.role === 'master';

  const handleLogout = async () => {
    try {
      await fetch('/api/tenant/logout', {
        method: 'POST',
      });
      
      router.push('/login');
    } catch (error) {
      console.error('Erro ao fazer logout:', error);
      // Redireciona mesmo se houver erro
      router.push('/login');
    }
  };

  return (
    <div className={styles.container}>
      {/* Header */}
      <header className={styles.header}>
        <div className={styles.headerContent}>
          <div className={styles.logo}>
            <h1 className={styles.tenantName}>{tenantName || 'Escritório'}</h1>
          </div>
          
          <div className={styles.userInfo}>
            <span className={styles.userEmail}>{user.email}</span>
            <span className={styles.userRole}>
              {isAdmin ? 'Administrador' : 'Operador'}
            </span>
            <button 
              onClick={handleLogout}
              className={styles.logoutButton}
              title="Sair"
            >
              Sair
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className={styles.main}>
        {/* Sidebar */}
        <aside className={styles.sidebar}>
          <nav className={styles.nav}>
            <a href="/dashboard" className={styles.navLink}>
              <span className={styles.navIcon}>📊</span>
              Dashboard
            </a>
            
            <a href="/processos" className={styles.navLink}>
              <span className={styles.navIcon}>📁</span>
              Processos
            </a>
            
            <a href="/clientes" className={styles.navLink}>
              <span className={styles.navIcon}>👥</span>
              Clientes
            </a>
            
            {isAdmin && (
              <a href="/operadores" className={styles.navLink}>
                <span className={styles.navIcon}>⚙️</span>
                Operadores
              </a>
            )}
            
            <a href="/perfil" className={styles.navLink}>
              <span className={styles.navIcon}>👤</span>
              Perfil
            </a>
          </nav>
        </aside>

        {/* Content */}
        <main className={styles.content}>
          {children}
        </main>
      </div>
    </div>
  );
}
