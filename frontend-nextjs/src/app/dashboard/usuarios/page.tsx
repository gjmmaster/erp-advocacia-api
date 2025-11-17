"use client";

import { useEffect, useState } from 'react';
import styles from './usuarios.module.css';

interface User {
  id: number;
  email: string;
  full_name: string;
  role: string;
  active: boolean;
  created_at: string;
}

export default function UsuariosPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formData, setFormData] = useState({
    email: '',
    full_name: '',
    role: 'operador'
  });
  const [tempPassword, setTempPassword] = useState('');

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      const response = await fetch('/api/tenant/users');
      if (response.ok) {
        const data = await response.json();
        setUsers(data.users || []);
      }
    } catch (error) {
      console.error('Erro ao carregar usuários:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const url = editingUser
        ? `/api/tenant/users/${editingUser.id}`
        : '/api/tenant/users';

      const response = await fetch(url, {
        method: editingUser ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        const data = await response.json();
        if (data.temporary_password) {
          setTempPassword(data.temporary_password);
        }
        await loadUsers();
        if (!data.temporary_password) {
          closeModal();
        }
      } else {
        const error = await response.json();
        alert(error.error || 'Erro ao salvar usuário');
      }
    } catch (error) {
      console.error('Erro ao salvar usuário:', error);
      alert('Erro ao salvar usuário');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Deseja realmente desativar este usuário?')) return;

    try {
      const response = await fetch(`/api/tenant/users/${id}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        await loadUsers();
      } else {
        alert('Erro ao deletar usuário');
      }
    } catch (error) {
      console.error('Erro ao deletar usuário:', error);
    }
  };

  const handleResetPassword = async (id: number) => {
    if (!confirm('Deseja resetar a senha deste usuário?')) return;

    try {
      const response = await fetch(`/api/tenant/users/${id}/reset-password`, {
        method: 'POST'
      });

      if (response.ok) {
        const data = await response.json();
        alert(`Senha resetada com sucesso!\nSenha temporária: ${data.temporary_password}`);
      } else {
        alert('Erro ao resetar senha');
      }
    } catch (error) {
      console.error('Erro ao resetar senha:', error);
    }
  };

  const openModal = (user?: User) => {
    if (user) {
      setEditingUser(user);
      setFormData({
        email: user.email,
        full_name: user.full_name,
        role: user.role
      });
    } else {
      setEditingUser(null);
      setFormData({
        email: '',
        full_name: '',
        role: 'operador'
      });
    }
    setTempPassword('');
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingUser(null);
    setTempPassword('');
    setFormData({
      email: '',
      full_name: '',
      role: 'operador'
    });
  };

  if (loading) {
    return <div className={styles.loading}>Carregando usuários...</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Gestão de Usuários</h1>
        <button className={styles.btnPrimary} onClick={() => openModal()}>
          + Novo Usuário
        </button>
      </div>

      <div className={styles.tableContainer}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Email</th>
              <th>Função</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.id}>
                <td>{user.full_name}</td>
                <td>{user.email}</td>
                <td>
                  <span className={styles.badge}>
                    {user.role === 'master' ? 'Master' : 'Operador'}
                  </span>
                </td>
                <td>
                  <span className={`${styles.status} ${user.active ? styles.active : styles.inactive}`}>
                    {user.active ? 'Ativo' : 'Inativo'}
                  </span>
                </td>
                <td>
                  <div className={styles.actions}>
                    <button
                      className={styles.btnEdit}
                      onClick={() => openModal(user)}
                      title="Editar"
                    >
                      ✏️
                    </button>
                    <button
                      className={styles.btnReset}
                      onClick={() => handleResetPassword(user.id)}
                      title="Resetar Senha"
                    >
                      🔑
                    </button>
                    <button
                      className={styles.btnDelete}
                      onClick={() => handleDelete(user.id)}
                      title="Desativar"
                    >
                      🗑️
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {users.length === 0 && (
          <div className={styles.empty}>
            Nenhum usuário cadastrado
          </div>
        )}
      </div>

      {showModal && (
        <div className={styles.modalOverlay} onClick={closeModal}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>{editingUser ? 'Editar Usuário' : 'Novo Usuário'}</h2>
              <button className={styles.btnClose} onClick={closeModal}>×</button>
            </div>

            {tempPassword ? (
              <div className={styles.passwordSuccess}>
                <h3>✅ Usuário criado com sucesso!</h3>
                <p>Senha temporária gerada:</p>
                <div className={styles.passwordBox}>
                  <code>{tempPassword}</code>
                </div>
                <p className={styles.warning}>
                  ⚠️ Anote esta senha! Ela não será mostrada novamente.
                </p>
                <button className={styles.btnPrimary} onClick={closeModal}>
                  Fechar
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit}>
                <div className={styles.formGroup}>
                  <label>Email*</label>
                  <input
                    type="email"
                    value={formData.email}
                    onChange={e => setFormData({...formData, email: e.target.value})}
                    required
                    disabled={!!editingUser}
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>Nome Completo*</label>
                  <input
                    type="text"
                    value={formData.full_name}
                    onChange={e => setFormData({...formData, full_name: e.target.value})}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>Função*</label>
                  <select
                    value={formData.role}
                    onChange={e => setFormData({...formData, role: e.target.value})}
                    required
                  >
                    <option value="operador">Operador</option>
                    <option value="master">Master</option>
                  </select>
                </div>

                <div className={styles.modalActions}>
                  <button type="button" className={styles.btnSecondary} onClick={closeModal}>
                    Cancelar
                  </button>
                  <button type="submit" className={styles.btnPrimary}>
                    {editingUser ? 'Salvar' : 'Criar Usuário'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
