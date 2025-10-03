import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import AuthContext from '../context/AuthContext';

const EditTenantModal = ({ isOpen, onClose, onSuccess, tenant }) => {
  // Renomeado para clareza
  const [companyName, setCompanyName] = useState('');
  const [error, setError] = useState('');
  const { token } = useContext(AuthContext);

  useEffect(() => {
    if (tenant) {
      // CORREÇÃO: O backend envia 'company_name', não 'name'
      setCompanyName(tenant.company_name);
    }
  }, [tenant]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!tenant) return;

    try {
      await axios.put(
        `/admin/tenants/${tenant.id}`,
        {
          // CORREÇÃO: A API espera 'company_name' no corpo da requisição
          company_name: companyName,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      onSuccess(); // Atualiza a lista de tenants
      onClose();   // Fecha o modal
    } catch (err) {
      setError('Falha ao atualizar o escritório. Tente novamente.');
      console.error(err);
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal">
      <div className="modal-content">
        <h2>Editar Escritório</h2>
        <form onSubmit={handleSubmit}>
          <div>
            <label>Nome do Escritório:</label>
            <input
              type="text"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              required
            />
          </div>
          {error && <p style={{ color: 'red' }}>{error}</p>}
          <button type="submit">Atualizar</button>
          <button type="button" onClick={onClose}>Cancelar</button>
        </form>
      </div>
    </div>
  );
};

export default EditTenantModal;
