'use client';

import React from 'react';
import { StatusBadge, Button } from './ui';
import type { Tenant } from '@/types/tenant';
import styles from './TenantsTableModern.module.css';

interface TenantsTableModernProps {
  tenants: Tenant[];
  onEdit: (tenant: Tenant) => void;
  onDelete: (tenant: Tenant) => void;
  onImpersonate: (tenant: Tenant) => void;
}

export const TenantsTableModern: React.FC<TenantsTableModernProps> = ({
  tenants,
  onEdit,
  onDelete,
  onImpersonate
}) => {
  return (
    <div className={styles.tableCard}>
      <div className={styles.tableHeader}>
        <div className={styles.tableTitle}>
          <i className="fas fa-list"></i>
          <span>Lista de Tenants</span>
        </div>
        <select className={styles.filterSelect}>
          <option value="all">Todos os Status</option>
          <option value="ativo">Ativos</option>
          <option value="inativo">Inativos</option>
        </select>
      </div>

      {/* Desktop Table View */}
      <div className={styles.tableContainer}>
        <table className={styles.dataTable}>
          <thead>
            <tr>
              <th>Nome do Escritório</th>
              <th>Subdomínio</th>
              <th>Usuário Master (Email)</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {tenants.map((tenant) => (
              <tr key={tenant.id}>
                <td>
                  <div className={styles.tenantName}>{tenant.company_name}</div>
                </td>
                <td>
                  <div className={styles.tenantSubdomain}>
                    {tenant.subdomain}.meuerp.com
                  </div>
                </td>
                <td>
                  <div className={styles.tenantEmail}>{tenant.email || 'N/A'}</div>
                </td>
                <td>
                  <StatusBadge
                    status={tenant.is_active !== false ? 'active' : 'inactive'}
                    icon={
                      <i
                        className={
                          tenant.is_active !== false ? 'fas fa-check-circle' : 'fas fa-pause-circle'
                        }
                      ></i>
                    }
                  >
                    {tenant.is_active !== false ? 'Ativo' : 'Inativo'}
                  </StatusBadge>
                </td>
                <td>
                  <div className={styles.actionsGroup}>
                    <button
                      className={styles.btnAccessAs}
                      onClick={() => onImpersonate(tenant)}
                      title="Acessar como este tenant"
                    >
                      <i className="fas fa-sign-in-alt"></i>
                      <span>Acessar Como</span>
                    </button>
                    <button
                      className={styles.actionBtn}
                      onClick={() => onEdit(tenant)}
                      title="Editar tenant"
                    >
                      <i className="fas fa-edit"></i>
                    </button>
                    <button
                      className={`${styles.actionBtn} ${styles.actionBtnDanger}`}
                      onClick={() => onDelete(tenant)}
                      title="Deletar tenant"
                    >
                      <i className="fas fa-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile Card View */}
      <div className={styles.tenantsCards}>
        {tenants.map((tenant) => (
          <div key={tenant.id} className={styles.tenantCard}>
            <div className={styles.tenantCardHeader}>
              <div className={styles.tenantCardTitle}>{tenant.company_name}</div>
            </div>

            <div className={styles.tenantCardInfo}>
              <div className={styles.tenantCardRow}>
                <div className={styles.tenantCardLabel}>Subdomínio</div>
                <div className={`${styles.tenantCardValue} ${styles.tenantCardSubdomain}`}>
                  {tenant.subdomain}.meuerp.com
                </div>
              </div>
              <div className={styles.tenantCardRow}>
                <div className={styles.tenantCardLabel}>Email</div>
                <div className={`${styles.tenantCardValue} ${styles.tenantCardEmail}`}>
                  {tenant.email || 'N/A'}
                </div>
              </div>
            </div>

            <div className={styles.tenantCardFooter}>
              <StatusBadge
                status={tenant.is_active !== false ? 'active' : 'inactive'}
                icon={
                  <i
                    className={
                      tenant.is_active !== false ? 'fas fa-check-circle' : 'fas fa-pause-circle'
                    }
                  ></i>
                }
              >
                {tenant.is_active !== false ? 'Ativo' : 'Inativo'}
              </StatusBadge>

              <div className={styles.actionsGroup}>
                <button
                  className={styles.btnAccessAs}
                  onClick={() => onImpersonate(tenant)}
                  title="Acessar como este tenant"
                >
                  <i className="fas fa-sign-in-alt"></i>
                  <span>Acessar Como</span>
                </button>
                <button
                  className={styles.tenantCardAction}
                  onClick={() => onEdit(tenant)}
                  title="Editar tenant"
                >
                  <i className="fas fa-edit"></i>
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
