import axios from 'axios';
import React, { useContext, useState } from 'react';
import AuthContext from '../context/AuthContext';

const CreateTenantModal = ({ isOpen, onClose, onSuccess }) => {
  const [companyName, setCompanyName] = useState('');
  const [masterEmail, setMasterEmail] = useState('');
  const [error, setError] = useState('');
  const { token } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      // Ajustado para enviar os campos corretos que o backend espera
      await axios.post(
        '/admin/provision-tenant',
        {
          company_name: companyName,
          email: masterEmail,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      // Limpa os campos após o sucesso
      setCompanyName('');
      setMasterEmail('');
      
      onSuccess(); // Atualiza a lista de escritórios
      onClose();   // Fecha o modal
    } catch (err) {
      setError('Falha ao provisionar escritório. Tente novamente.');
      console.error(err);
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal">
      <div className="modal-content">
        <h2>Provisionar Novo Escritório</h2>
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
          <div>
            <label>E-mail do Administrador (Master):</label>
            <input
              type="email"
              value={masterEmail}
              onChange={(e) => setMasterEmail(e.target.value)}
              required
            />
          </div>
          {/* O campo de senha foi removido, pois o backend gera uma senha temporária */}
          {error && <p style={{ color: 'red' }}>{error}</p>}
          <button type="submit">Provisionar</button>
          <button type="button" onClick={onClose}>Cancelar</button>
        </form>
      </div>
    </div>
  );
};

export default CreateTenantModal;
