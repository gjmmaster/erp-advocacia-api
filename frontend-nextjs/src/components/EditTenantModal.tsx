'use client';

import { useState, useEffect, FormEvent } from 'react';
import type { Tenant, UpdateTenantPayload } from '@/types/tenant';
import styles from './Modal.module.css';

interface EditTenantModalProps {
  isOpen: boolean;
  tenant: Tenant | null;
  onClose: () => void;
  onSuccess: () => void;
}

export default function EditTenantModal({ isOpen, tenant, onClose, onSuccess }: EditTenantModalProps) {
  const [editableCompanyName, setEditableCompanyName] = useState('');
  const [editableOperatorLimit, setEditableOperatorLimit] = useState(4);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (tenant) {
      setEditableCompanyName(tenant.company_name || '');
      setEditableOperatorLimit(tenant.operator_limit || 4);
      setError('');
    } else {
      setEditableCompanyName('');
      setEditableOperatorLimit(4);
      setError('');
    }
  }, [tenant]);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    
    if (!tenant) {
      setError('Erro: Dados do escritório não encontrados.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const updateData: UpdateTenantPayload = {
        company_name: editableCompanyName,
        operator_limit: editableOperatorLimit,
      };

      const response = await fetch(`/api/admin/tenants/${tenant.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updateData),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Falha ao atualizar escritório');
      }

      alert('✅ Escritório atualizado com sucesso!');
      onSuccess();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao atualizar escritório');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setError('');
      onClose();
    }
  };

  if (!isOpen || !tenant) {
    return null;
  }

  return (
    <div className={styles.modalOverlay} onClick={handleClose}>
      <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalHeader}>
          <h2 className={styles.modalTitle}>✏️ Editar Escritório</h2>
        </div>

        <form onSubmit={handleSubmit}>
          <div className={styles.modalBody}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>🏢 Nome do Escritório:</label>
              <input
                type="text"
                className={styles.formInput}
                value={editableCompanyName}
                onChange={(e) => setEditableCompanyName(e.target.value)}
                required
                disabled={loading}
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel}>👥 Limite de Operadores:</label>
              <input
                type="number"
                className={styles.formInput}
                min="1"
                max="100"
                value={editableOperatorLimit}
                onChange={(e) => setEditableOperatorLimit(parseInt(e.target.value) || 1)}
                required
                disabled={loading}
              />
              <div className={styles.formHelp}>
                Número máximo de operadores que este escritório pode criar
              </div>
            </div>

            <div className={styles.infoBox}>
              <p>
                <strong>Subdomínio:</strong> {tenant.subdomain}
              </p>
              <p className={styles.infoNote}>
                ⚠️ O subdomínio não pode ser alterado após a criação
              </p>
            </div>

            {error && (
              <div className={styles.errorMessage}>
                <span>⚠️</span>
                <span>{error}</span>
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
              {loading ? '⏳ Salvando...' : '💾 Salvar Alterações'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
