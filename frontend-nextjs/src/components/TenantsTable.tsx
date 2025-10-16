import type { Tenant } from '@/types/tenant';
import styles from './TenantsTable.module.css';

interface TenantsTableProps {
  tenants: Tenant[];
  onEdit: (tenant: Tenant) => void;
  onDelete: (tenant: Tenant) => void;
}

export default function TenantsTable({ tenants, onEdit, onDelete }: TenantsTableProps) {
  if (!tenants || tenants.length === 0) {
    return (
      <div className={styles.emptyState}>
        <h3>📋 Nenhum escritório encontrado</h3>
        <p>Ainda não há escritórios cadastrados no sistema.</p>
      </div>
    );
  }

  const formatDate = (dateString: string): string => {
    if (!dateString) return '-';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '-';
      return date.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (e) {
      return '-';
    }
  };

  return (
    <div className={styles.tableContainer}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th style={{ width: '80px' }}>ID</th>
            <th style={{ minWidth: '200px' }}>Nome do Escritório</th>
            <th style={{ minWidth: '150px' }}>Subdomínio</th>
            <th style={{ width: '120px', textAlign: 'center' }}>Limite de Operadores</th>
            <th style={{ minWidth: '150px' }}>Criado em</th>
            <th style={{ width: '200px', textAlign: 'center' }}>Ações</th>
          </tr>
        </thead>
        <tbody>
          {tenants.map((tenant) => (
            <tr key={tenant.id}>
              <td>
                <strong>#{tenant.id}</strong>
              </td>
              <td>
                <div className={styles.tenantName}>
                  {tenant.company_name}
                </div>
              </td>
              <td>
                <span className={styles.tenantSubdomain}>
                  {tenant.subdomain}
                </span>
              </td>
              <td style={{ textAlign: 'center' }}>
                <strong>{tenant.operator_limit}</strong>
              </td>
              <td>
                <div className={styles.tenantDate}>
                  {formatDate(tenant.created_at)}
                </div>
              </td>
              <td className={styles.actionsCell}>
                <button
                  className={`${styles.actionBtn} ${styles.editBtn}`}
                  onClick={() => onEdit(tenant)}
                  title="Editar escritório"
                >
                  ✏️ Editar
                </button>
                <button
                  className={`${styles.actionBtn} ${styles.deleteBtn}`}
                  onClick={() => onDelete(tenant)}
                  title="Deletar escritório"
                >
                  🗑️ Deletar
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
