'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import styles from './ImpersonationBanner.module.css';

interface ImpersonationBannerProps {
  targetEmail: string;
}

export default function ImpersonationBanner({ targetEmail }: ImpersonationBannerProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  const handleStopImpersonation = async () => {
    if (loading) return;

    setLoading(true);
    try {
      const response = await fetch('/api/admin/stop-impersonate', {
        method: 'POST',
      });

      if (response.ok) {
        // Redirecionar para dashboard do super admin
        router.push('/admin/dashboard');
        router.refresh();
      } else {
        const error = await response.json();
        alert(`Erro: ${error.error || 'Falha ao parar impersonation'}`);
      }
    } catch (error) {
      console.error('Erro ao parar impersonation:', error);
      alert('Erro ao parar impersonation');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.banner}>
      <div className={styles.content}>
        <div className={styles.info}>
          <span className={styles.icon}>⚠️</span>
          <span className={styles.text}>
            <strong>MODO ADMINISTRADOR</strong> - Acessando como: <strong>{targetEmail}</strong>
          </span>
        </div>
        <button
          onClick={handleStopImpersonation}
          disabled={loading}
          className={styles.button}
        >
          {loading ? 'Saindo...' : 'Voltar para Super Admin'}
        </button>
      </div>
    </div>
  );
}
