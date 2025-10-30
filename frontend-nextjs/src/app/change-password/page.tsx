'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './page.module.css';

interface FormData {
  current_password: string;
  new_password: string;
  confirm_password: string;
}

interface FormErrors {
  current_password?: string;
  new_password?: string;
  confirm_password?: string;
  general?: string;
}

export default function ChangePasswordPage() {
  const router = useRouter();
  const [formData, setFormData] = useState<FormData>({
    current_password: '',
    new_password: '',
    confirm_password: '',
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false,
  });

  const validatePassword = (password: string): string[] => {
    const errors: string[] = [];
    if (password.length < 8) errors.push('Mínimo 8 caracteres');
    if (!/[A-Z]/.test(password)) errors.push('Uma letra maiúscula');
    if (!/[a-z]/.test(password)) errors.push('Uma letra minúscula');
    if (!/[0-9]/.test(password)) errors.push('Um número');
    return errors;
  };

  const getPasswordStrength = (password: string): { level: string; color: string; width: string } => {
    if (!password) return { level: '', color: '', width: '0%' };
    
    let score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    if (score <= 2) return { level: 'Fraca', color: '#ef4444', width: '33%' };
    if (score <= 4) return { level: 'Média', color: '#f59e0b', width: '66%' };
    return { level: 'Forte', color: '#10b981', width: '100%' };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setLoading(true);

    // Validação client-side
    const newErrors: FormErrors = {};

    if (!formData.current_password) {
      newErrors.current_password = 'Senha atual é obrigatória';
    }

    const passwordErrors = validatePassword(formData.new_password);
    if (passwordErrors.length > 0) {
      newErrors.new_password = `Senha deve ter: ${passwordErrors.join(', ')}`;
    }

    if (formData.new_password !== formData.confirm_password) {
      newErrors.confirm_password = 'As senhas não coincidem';
    }

    if (formData.new_password === formData.current_password) {
      newErrors.new_password = 'Nova senha deve ser diferente da atual';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      setLoading(false);
      return;
    }

    try {
      const response = await fetch('/api/auth/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (!response.ok) {
        setErrors({ general: data.error || 'Erro ao alterar senha' });
        setLoading(false);
        return;
      }

      // Sucesso!
      setSuccess(true);
      setTimeout(() => {
        router.push('/dashboard');
      }, 2000);
    } catch (error) {
      setErrors({ general: 'Erro de conexão. Tente novamente.' });
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className={styles.successContainer}>
        <div className={styles.successIcon}>✓</div>
        <h1>Senha alterada com sucesso!</h1>
        <p>Redirecionando para o dashboard...</p>
      </div>
    );
  }

  const strength = getPasswordStrength(formData.new_password);
  const passwordRequirements = validatePassword(formData.new_password);

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h1>Criar Nova Senha</h1>
          <p>Por segurança, você precisa criar uma nova senha</p>
        </div>

        <form onSubmit={handleSubmit} className={styles.form}>
          {/* Senha Atual */}
          <div className={styles.field}>
            <label htmlFor="current_password">
              Senha Atual (Temporária)
            </label>
            <div className={styles.passwordInput}>
              <input
                id="current_password"
                type={showPasswords.current ? 'text' : 'password'}
                value={formData.current_password}
                onChange={(e) => setFormData({ ...formData, current_password: e.target.value })}
                disabled={loading}
                aria-invalid={!!errors.current_password}
                aria-describedby={errors.current_password ? 'current-error' : undefined}
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, current: !showPasswords.current })}
                aria-label={showPasswords.current ? 'Ocultar senha' : 'Mostrar senha'}
                className={styles.toggleButton}
              >
                {showPasswords.current ? '👁️' : '👁️‍🗨️'}
              </button>
            </div>
            {errors.current_password && (
              <span id="current-error" className={styles.error}>
                {errors.current_password}
              </span>
            )}
          </div>

          {/* Nova Senha */}
          <div className={styles.field}>
            <label htmlFor="new_password">Nova Senha</label>
            <div className={styles.passwordInput}>
              <input
                id="new_password"
                type={showPasswords.new ? 'text' : 'password'}
                value={formData.new_password}
                onChange={(e) => setFormData({ ...formData, new_password: e.target.value })}
                disabled={loading}
                aria-invalid={!!errors.new_password}
                aria-describedby={errors.new_password ? 'new-error' : undefined}
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, new: !showPasswords.new })}
                aria-label={showPasswords.new ? 'Ocultar senha' : 'Mostrar senha'}
                className={styles.toggleButton}
              >
                {showPasswords.new ? '👁️' : '👁️‍🗨️'}
              </button>
            </div>
            
            {/* Indicador de Força */}
            {formData.new_password && (
              <div className={styles.strengthIndicator}>
                <div className={styles.strengthBar}>
                  <div 
                    className={styles.strengthFill}
                    style={{ width: strength.width, backgroundColor: strength.color }}
                  />
                </div>
                <span style={{ color: strength.color }}>{strength.level}</span>
              </div>
            )}

            {/* Requisitos */}
            {formData.new_password && (
              <div className={styles.requirements}>
                <div className={passwordRequirements.includes('Mínimo 8 caracteres') ? styles.unmet : styles.met}>
                  {passwordRequirements.includes('Mínimo 8 caracteres') ? '○' : '✓'} Mínimo 8 caracteres
                </div>
                <div className={passwordRequirements.includes('Uma letra maiúscula') ? styles.unmet : styles.met}>
                  {passwordRequirements.includes('Uma letra maiúscula') ? '○' : '✓'} Uma letra maiúscula
                </div>
                <div className={passwordRequirements.includes('Uma letra minúscula') ? styles.unmet : styles.met}>
                  {passwordRequirements.includes('Uma letra minúscula') ? '○' : '✓'} Uma letra minúscula
                </div>
                <div className={passwordRequirements.includes('Um número') ? styles.unmet : styles.met}>
                  {passwordRequirements.includes('Um número') ? '○' : '✓'} Um número
                </div>
              </div>
            )}

            {errors.new_password && (
              <span id="new-error" className={styles.error}>
                {errors.new_password}
              </span>
            )}
          </div>

          {/* Confirmar Senha */}
          <div className={styles.field}>
            <label htmlFor="confirm_password">Confirmar Nova Senha</label>
            <div className={styles.passwordInput}>
              <input
                id="confirm_password"
                type={showPasswords.confirm ? 'text' : 'password'}
                value={formData.confirm_password}
                onChange={(e) => setFormData({ ...formData, confirm_password: e.target.value })}
                disabled={loading}
                aria-invalid={!!errors.confirm_password}
                aria-describedby={errors.confirm_password ? 'confirm-error' : undefined}
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, confirm: !showPasswords.confirm })}
                aria-label={showPasswords.confirm ? 'Ocultar senha' : 'Mostrar senha'}
                className={styles.toggleButton}
              >
                {showPasswords.confirm ? '👁️' : '👁️‍🗨️'}
              </button>
            </div>
            {errors.confirm_password && (
              <span id="confirm-error" className={styles.error}>
                {errors.confirm_password}
              </span>
            )}
          </div>

          {errors.general && (
            <div className={styles.generalError}>
              {errors.general}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className={styles.submitButton}
          >
            {loading ? 'Alterando...' : 'Criar Nova Senha'}
          </button>
        </form>
      </div>
    </div>
  );
}
