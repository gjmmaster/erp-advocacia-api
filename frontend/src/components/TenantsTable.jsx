import React from 'react';

const TenantsTable = ({ tenants, onEdit, onDelete }) => {
  if (!tenants || tenants.length === 0) {
    // Texto traduzido para melhor experiência do usuário
    return <p>Nenhum escritório encontrado.</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>ID</th>
          {/* Cabeçalho da coluna corrigido */}
          <th>Nome do Escritório</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        {tenants.map((tenant) => (
          <tr key={tenant.id}>
            <td>{tenant.id}</td>
            {/* CORREÇÃO PRINCIPAL: trocado tenant.name por tenant.company_name */}
            <td>{tenant.company_name}</td>
            <td>
              <button onClick={() => onEdit(tenant)}>Editar</button>
              <button onClick={() => onDelete(tenant)}>Deletar</button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default TenantsTable;
