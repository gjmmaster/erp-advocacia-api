import React from 'react';

const TenantsTable = ({ tenants, onEdit, onDelete }) => {
  if (!tenants || tenants.length === 0) {
    return <p>Nenhum escritório encontrado.</p>;
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
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
        <thead>
          <tr style={{ backgroundColor: '#f5f5f5', borderBottom: '2px solid #ddd' }}>
            <th style={{ padding: '12px', textAlign: 'left' }}>ID</th>
            <th style={{ padding: '12px', textAlign: 'left' }}>Nome do Escritório</th>
            <th style={{ padding: '12px', textAlign: 'left' }}>Subdomínio</th>
            <th style={{ padding: '12px', textAlign: 'center' }}>Limite de Operadores</th>
            <th style={{ padding: '12px', textAlign: 'left' }}>Criado em</th>
            <th style={{ padding: '12px', textAlign: 'center' }}>Ações</th>
          </tr>
        </thead>
        <tbody>
          {tenants.map((tenant) => (
            <tr key={tenant.id} style={{ borderBottom: '1px solid #eee' }}>
              <td style={{ padding: '12px' }}>{tenant.id}</td>
              <td style={{ padding: '12px', fontWeight: '500' }}>
                {tenant.company_name || tenant.name}
              </td>
              <td style={{ padding: '12px', fontFamily: 'monospace', color: '#666' }}>
                {tenant.subdomain}
              </td>
              <td style={{ padding: '12px', textAlign: 'center' }}>
                {tenant.operator_limit || 4}
              </td>
              <td style={{ padding: '12px', fontSize: '0.9em', color: '#666' }}>
                {formatDate(tenant.created_at)}
              </td>
              <td style={{ padding: '12px', textAlign: 'center' }}>
                <button 
                  onClick={() => onEdit(tenant)}
                  style={{
                    padding: '6px 12px',
                    marginRight: '8px',
                    backgroundColor: '#4CAF50',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Editar
                </button>
                <button 
                  onClick={() => onDelete(tenant)}
                  style={{
                    padding: '6px 12px',
                    backgroundColor: '#f44336',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Deletar
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
