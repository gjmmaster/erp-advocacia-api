import React from 'react';
import styles from './StatCard.module.css';

interface StatCardProps {
  icon: string;
  value: string | number;
  label: string;
  color?: 'blue' | 'green' | 'orange' | 'purple';
}

export const StatCard: React.FC<StatCardProps> = ({ icon, value, label, color = 'blue' }) => {
  return (
    <div className={styles.statCard}>
      <div className={`${styles.statIcon} ${styles[`statIcon--${color}`]}`}>
        <i className={icon}></i>
      </div>
      <div className={styles.statInfo}>
        <div className={styles.statValue}>{value}</div>
        <div className={styles.statLabel}>{label}</div>
      </div>
    </div>
  );
};
