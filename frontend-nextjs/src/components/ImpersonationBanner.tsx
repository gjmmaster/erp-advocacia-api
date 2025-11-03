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
      console.log('[BANNER] Parando impersonation...');
      const response = await fetch('/api/admin/stop-impersonate', {
        method: 'POST',
      });

      console.log('[BANNER] Resposta:', response.status);

      if (response.ok) {
        const data = await response.json();
        console.log('[BANNER] Dados recebidos:', data);
        
        // Aguardar um pouco para garantir que os cookies foram atualizados
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // Redirecionar para dashboard do super admin com reload completo
        window.location.href = '/super-admin/dashboard';
      } else {
        const error = await response.json();
        console.error('[BANNER] Erro:', error);
        alert(`Erro: ${error.error || 'Falha ao parar impersonation'}`);
      }
    } catch (error) {
      console.error('[BANNER] Erro ao parar impersonation:', error);
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
