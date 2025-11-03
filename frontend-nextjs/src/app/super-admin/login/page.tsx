'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { LoginScreen } from '@/components';

export default function SuperAdminLoginPage() {
  const [error, setError] = useState('');
  const router = useRouter();

  const handleSubmit = async (email: string, password: string) => {
    setError('');

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Falha no login');
      }

      router.push('/super-admin/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao fazer login');
      throw err;
    }
  };

  return (
    <LoginScreen
      onSubmit={handleSubmit}
      title="Super Admin Login"
      subtitle="Sistema de Gerenciamento Legal ERP"
      isSuperAdmin={true}
    />
  );
}
