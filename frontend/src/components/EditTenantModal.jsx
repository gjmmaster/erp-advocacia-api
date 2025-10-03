// frontend/src/components/EditTenantModal.jsx
import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import AuthContext from '../context/AuthContext';

const EditTenantModal = ({ isOpen, onClose, onSuccess, tenant }) => {
  // Estado local para o nome do escritório, para que o campo de texto seja editável
  const [editableCompanyName, setEditableCompanyName] = useState('');
  const [error, setError] = useState('');
  const { token } = useContext(AuthContext);

  // Efeito para atualizar o estado local sempre que o tenant selecionado mudar
  useEffect(() => {
    if (tenant) {
      setEditableCompanyName(tenant.company_name);
    } else {
      setEditableCompanyName(''); // Limpa se não houver tenant
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
          // Envia o nome do estado local editável
          company_name: editableCompanyName,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      onSuccess(); // Atualiza a lista
      onClose();   // Fecha o modal
    } catch (err) {
      setError('Falha ao atualizar escritório. Tente novamente.');
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
              value={editableCompanyName}
              onChange={(e) => setEditableCompanyName(e.target.value)}
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
