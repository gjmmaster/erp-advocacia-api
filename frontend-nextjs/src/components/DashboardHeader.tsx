'use client';

import React from 'react';
import styles from './DashboardHeader.module.css';

interface DashboardHeaderProps {
  userName?: string;
  userRole?: string;
  onLogout?: () => void;
  onMenuToggle?: () => void;
  showSearch?: boolean;
}

export const DashboardHeader: React.FC<DashboardHeaderProps> = ({
  userName = 'Usuário',
  userRole = 'Administrador',
  onLogout,
  onMenuToggle,
  showSearch = true
}) => {
  return (
    <header className={styles.dashboardHeader}>
      <button className={styles.hamburgerMenu} onClick={onMenuToggle} aria-label="Abrir menu">
        <span></span>
        <span></span>
        <span></span>
      </button>

      {showSearch && (
        <div className={styles.headerSearch}>
          <i className="fas fa-search"></i>
          <input type="text" placeholder="Buscar..." />
        </div>
      )}

      <div className={styles.headerActions}>
        <button className={styles.btnIcon} title="Notificações">
          <i className="fas fa-bell"></i>
          <span className={styles.badge}>3</span>
        </button>

        <div className={styles.userProfile}>
          <div className={styles.userAvatar}>
            <i className="fas fa-user"></i>
          </div>
          <div className={styles.userInfo}>
            <span className={styles.userName}>{userName}</span>
            <span className={styles.userRole}>{userRole}</span>
          </div>
        </div>

        <button className={`${styles.btnIcon} ${styles.logoutBtn}`} onClick={onLogout} title="Sair">
          <i className="fas fa-sign-out-alt"></i>
        </button>
      </div>
    </header>
  );
};
