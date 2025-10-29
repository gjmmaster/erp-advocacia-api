'use client';

import { useState, FormEvent } from 'react';
import type { CreateTenantPayload } from '@/types/tenant';
import styles from './Modal.module.css';

interface CreateTenantModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (data: {
    tenantName: string;
    email: string;
    tempPassword: string;
    emailSent: boolean;
  }) => void;
}

export default function CreateTenantModal({ isOpen, onClose, onSuccess }: CreateTenantModalProps) {
  const [companyName, setCompanyName] = useState('');
  const [masterEmail, setMasterEmail] = useState('');
  const [operatorLimit, setOperatorLimit] = useState(4);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const payload: CreateTenantPayload = {
        company_name: companyName,
        email: masterEmail,
        operator_limit: operatorLimit,
      };

      const response = await fetch('/api/admin/tenants', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Falha ao provisionar escritório');
      }

      const result = await response.json();
      
      // Chamar onSuccess com os dados da senha
      onSuccess({
        tenantName: result.tenant.company_name,
        email: result.user.email,
        tempPassword: result.temp_password,
        emailSent: result.email_sent || false,
      });

      // Limpa os campos após 5 segundos
      setTimeout(() => {
        setCompanyName('');
        setMasterEmail('');
        setOperatorLimit(4);
        setSuccess('');
        setError('');
        onSuccess();
        onClose();
      }, 5000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao provisionar escritório. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setCompanyName('');
      setMasterEmail('');
      setOperatorLimit(4);
      setError('');
      setSuccess('');
      onClose();
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className={styles.modalOverlay} onClick={handleClose}>
      <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalHeader}>
          <h2 className={styles.modalTitle}>➕ Provisionar Novo Escritório</h2>
        </div>

        <form onSubmit={handleSubmit}>
          <div className={styles.modalBody}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>🏢 Nome do Escritório:</label>
              <input
                type="text"
                className={styles.formInput}
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                required
                disabled={loading}
                placeholder="Ex: Advocacia Silva & Associados"
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel}>👤 E-mail do Administrador (Master):</label>
              <input
                type="email"
                className={styles.formInput}
                value={masterEmail}
                onChange={(e) => setMasterEmail(e.target.value)}
                required
                disabled={loading}
                placeholder="admin@escritorio.com"
              />
              <div className={styles.formHelp}>Este será o usuário principal do escritório</div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel}>👥 Limite de Operadores:</label>
              <input
                type="number"
                className={styles.formInput}
                min="1"
                max="100"
                value={operatorLimit}
                onChange={(e) => setOperatorLimit(parseInt(e.target.value) || 1)}
                required
                disabled={loading}
              />
              <div className={styles.formHelp}>
                Número máximo de operadores que este escritório pode criar
              </div>
            </div>

            {error && (
              <div className={styles.errorMessage}>
                <span>⚠️</span>
                <span>{error}</span>
              </div>
            )}

            {success && (
              <div className={styles.successMessage}>
                <span>✅</span>
                <div style={{ whiteSpace: 'pre-line' }}>{success}</div>
              </div>
            )}
          </div>

          <div className={styles.modalActions}>
            <button
              type="button"
              className={`${styles.modalBtn} ${styles.modalBtnCancel}`}
              onClick={handleClose}
              disabled={loading}
            >
              ❌ Cancelar
            </button>
            <button
              type="submit"
              className={`${styles.modalBtn} ${styles.modalBtnPrimary}`}
              disabled={loading}
            >
              {loading ? '⏳ Criando...' : '✅ Provisionar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
