import React from 'react';

const TenantsTable = ({ tenants, onEdit, onDelete }) => {
  if (!tenants || tenants.length === 0) {
    return <p>No tenants found.</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {tenants.map((tenant) => (
          <tr key={tenant.id}>
            <td>{tenant.id}</td>
            <td>{tenant.name}</td>
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
