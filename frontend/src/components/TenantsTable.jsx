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

  // Função para obter valor com suporte a chaves com namespace
  const getValue = (obj, key) => {
    // Tenta primeiro a chave simples
    if (obj[key] !== undefined) return obj[key];
    // Tenta com namespace :tenants/
    if (obj[`tenants/${key}`] !== undefined) return obj[`tenants/${key}`];
    // Tenta com dois pontos
    if (obj[`:tenants/${key}`] !== undefined) return obj[`:tenants/${key}`];
    return null;
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '-';
      return date.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      console.error('Erro ao formatar data:', e);
      return '-';
    }
  };

  return (
    <div className="table-container">
      <table className="data-table">
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
          {tenants.map((tenant, index) => {
            const id = getValue(tenant, 'id');
            const companyName = getValue(tenant, 'company_name') || getValue(tenant, 'name');
            const subdomain = getValue(tenant, 'subdomain');
            const operatorLimit = getValue(tenant, 'operator_limit');
            const createdAt = getValue(tenant, 'created_at');

            return (
              <tr key={id || index}>
                <td>
                  <strong>#{id || 'N/A'}</strong>
                </td>
                <td>
                  <div className="tenant-name">
                    {companyName || 'Nome não informado'}
                  </div>
                </td>
                <td>
                  <span className="tenant-subdomain">
                    {subdomain || 'N/A'}
                  </span>
                </td>
                <td style={{ textAlign: 'center' }}>
                  <strong>{operatorLimit || 4}</strong>
                </td>
                <td>
                  <div className="tenant-date">
                    {formatDate(createdAt)}
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
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default TenantsTable;
