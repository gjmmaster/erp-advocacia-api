import React from 'react';
import '../styles/Dashboard.css';

const TenantsTable = ({ tenants, onEdit, onDelete }) => {
  if (!tenants || tenants.length === 0) {
    return (
      <div className="empty-state">
        <h3>📋 Nenhum escritório encontrado</h3>
        <p>Ainda não há escritórios cadastrados no sistema.</p>
      </div>
    );
  }

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome do Escritório</th>
            <th>Subdomínio</th>
            <th style={{ textAlign: 'center' }}>Limite de Operadores</th>
            <th>Criado em</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {tenants.map((tenant) => (
            <tr key={tenant.id}>
              <td>
                <strong>#{tenant.id}</strong>
              </td>
              <td>
                <div className="tenant-name">
                  {tenant.company_name || tenant.name || 'Nome não informado'}
                </div>
              </td>
              <td>
                <span className="tenant-subdomain">
                  {tenant.subdomain || 'N/A'}
                </span>
              </td>
              <td style={{ textAlign: 'center' }}>
                <strong>{tenant.operator_limit || 4}</strong>
              </td>
              <td>
                <div className="tenant-date">
                  {formatDate(tenant.created_at)}
                </div>
              </td>
              <td className="actions-cell">
                <button 
                  className="action-btn edit-btn"
                  onClick={() => onEdit(tenant)}
                  title="Editar escritório"
                >
                  ✏️ Editar
                </button>
                <button 
                  className="action-btn delete-btn"
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
};

export default TenantsTable;
