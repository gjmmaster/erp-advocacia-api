'use client';

import React, { useState } from 'react';
import { Button } from './ui/Button';
import { Input } from './ui/Input';
import styles from './LoginScreen.module.css';

interface LoginScreenProps {
  onSubmit: (email: string, password: string) => Promise<void>;
  title?: string;
  subtitle?: string;
  isSuperAdmin?: boolean;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({
  onSubmit,
  title = 'Login',
  subtitle = 'Sistema de Gerenciamento Legal ERP',
  isSuperAdmin = false
}) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onSubmit(email, password);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginScreen}>
      <div className={styles.loginBackground}>
        <div className={styles.particle}></div>
        <div className={styles.particle}></div>
        <div className={styles.particle}></div>
        <div className={styles.particle}></div>
        <div className={styles.particle}></div>
      </div>

      <div className={styles.loginCard}>
        <div className={styles.loginHeader}>
          <div className={styles.logoIcon}>⚖️</div>
          <h1 className={styles.gradientText}>
            {isSuperAdmin ? 'Super Admin Login' : title}
          </h1>
          <p className={styles.subtitle}>{subtitle}</p>
        </div>

        <form onSubmit={handleSubmit} className={styles.loginForm}>
          <Input
            type="email"
            id="loginEmail"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            icon={<i className="fas fa-envelope"></i>}
          />

          <Input
            type="password"
            id="loginPassword"
            label="Senha"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            icon={<i className="fas fa-lock"></i>}
          />

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            glow
            disabled={loading}
            icon={<i className="fas fa-arrow-right"></i>}
          >
            {loading ? 'Entrando...' : 'Entrar'}
          </Button>
        </form>
      </div>
    </div>
  );
};
