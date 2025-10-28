'use client';

import { useEffect, useState } from 'react';
import type { DashboardStats as Stats } from '@/types/dashboard';
import styles from './DashboardStats.module.css';

interface DashboardStatsProps {
  tenantId: number;
}

export default function DashboardStats({ tenantId }: DashboardStatsProps) {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchStats();
  }, [tenantId]);

  const fetchStats = async () => {
    try {
      setLoading(true);
      setError('');
      
      const response = await fetch('/api/tenant/dashboard/stats');
      
      if (!response.ok) {
        throw new Error('Erro ao carregar estatísticas');
      }
      
      const data = await response.json();
      setStats(data);
    } catch (err) {
      console.error('Error fetching stats:', err);
      setError(err instanceof Error ? err.message : 'Erro desconhecido');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.statsGrid}>
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className={styles.statCard}>
            <div className={styles.skeleton}></div>
          </div>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.error}>
        {error}
      </div>
    );
  }

  return (
    <div className={styles.statsGrid}>
      <div className={styles.statCard}>
        <div className={styles.statIcon}>📁</div>
        <div className={styles.statContent}>
          <h3 className={styles.statLabel}>Total de Processos</h3>
          <p className={styles.statValue}>{stats?.['total-processos'] || 0}</p>
        </div>
      </div>
      
      <div className={styles.statCard}>
        <div className={styles.statIcon}>✅</div>
        <div className={styles.statContent}>
          <h3 className={styles.statLabel}>Processos Ativos</h3>
          <p className={styles.statValue}>{stats?.['processos-ativos'] || 0}</p>
        </div>
      </div>
      
      <div className={styles.statCard}>
        <div className={styles.statIcon}>👥</div>
        <div className={styles.statContent}>
          <h3 className={styles.statLabel}>Total de Clientes</h3>
          <p className={styles.statValue}>{stats?.['total-clientes'] || 0}</p>
        </div>
      </div>
      
      <div className={styles.statCard}>
        <div className={styles.statIcon}>⚙️</div>
        <div className={styles.statContent}>
          <h3 className={styles.statLabel}>Total de Operadores</h3>
          <p className={styles.statValue}>{stats?.['total-operadores'] || 0}</p>
        </div>
      </div>
    </div>
  );
}
