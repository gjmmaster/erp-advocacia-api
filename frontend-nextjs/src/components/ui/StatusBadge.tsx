import React from 'react';
import styles from './StatusBadge.module.css';

interface StatusBadgeProps {
  status: 'success' | 'error' | 'warning' | 'info' | 'active' | 'inactive';
  children: React.ReactNode;
  icon?: React.ReactNode;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, children, icon }) => {
  return (
    <span className={`${styles.status} ${styles[`status--${status}`]}`}>
      {icon && <span className={styles.icon}>{icon}</span>}
      {children}
    </span>
  );
};
