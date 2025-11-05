'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './clientes.module.css';

interface Cliente {
  id: number;
  nome: string;
  cpf_cnpj?: string;
  email?: string;
  telefone?: string;
  created_at: string;
}

export default function ClientesPage() {
  const router = useRouter();
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [search, setSearch] = useState('');

  useEffect(() => {
    loadClientes();
  }, [page]);

  const loadClientes = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('per-page', '20');

      const response = await fetch(`/api/tenant/clientes?${params.toString()}`);
      
      if (!response.ok) {
        throw new Error('Erro ao carregar clientes');
      }

      const data = await response.json();
      console.log('[CLIENTES PAGE] Dados recebidos do backend:', data);
      console.log('[CLIENTES PAGE] data.clientes:', data.clientes);
      console.log('[CLIENTES PAGE] Tipo de data.clientes:', typeof data.clientes, Array.isArray(data.clientes));
      
      setClientes(data.clientes || []);
      setTotal(data.total || 0);
    } catch (err) {
      setError('Erro ao carregar clientes');
      console.error('[CLIENTES PAGE] Erro:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (search.length < 2) {
      alert('Digite pelo menos 2 caracteres para buscar');
      return;
    }

    try {
      setLoading(true);
      const response = await fetch(`/api/tenant/clientes?search=${encodeURIComponent(search)}`);
      
      if (!response.ok) {
        throw new Error('Erro ao buscar clientes');
      }

      const data = await response.json();
      setClientes(data.clientes || []);
      setTotal(data.total || 0);
    } catch (err) {
      setError('Erro ao buscar clientes');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Tem certeza que deseja deletar este cliente?')) {
      return;
    }

    try {
      const response = await fetch(`/api/tenant/clientes/${id}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Erro ao deletar cliente');
      }

      alert('Cliente deletado com sucesso!');
      loadClientes();
    } catch (err: any) {
      alert(err.message || 'Erro ao deletar cliente');
      console.error(err);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR');
  };

  if (loading && clientes.length === 0) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>Carregando clientes...</div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Clientes</h1>
        <button
          className={styles.btnPrimary}
          onClick={() => router.push('/dashboard/clientes/novo')}
        >
          + Novo Cliente
        </button>
      </div>

      {error && <div className={styles.error}>{error}</div>}

      <div className={styles.filters}>
        <form onSubmit={handleSearch} className={styles.searchForm}>
          <input
            type="text"
            placeholder="Buscar por nome, CPF/CNPJ ou email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className={styles.searchInput}
          />
          <button type="submit" className={styles.btnSearch}>
            Buscar
          </button>
          {search && (
            <button
              type="button"
              onClick={() => {
                setSearch('');
                loadClientes();
              }}
              className={styles.btnClear}
            >
              Limpar
            </button>
          )}
        </form>
      </div>

      {clientes.length === 0 ? (
        <div className={styles.empty}>
          <p>Nenhum cliente cadastrado</p>
          <button
            className={styles.btnPrimary}
            onClick={() => router.push('/dashboard/clientes/novo')}
          >
            Cadastrar Primeiro Cliente
          </button>
        </div>
      ) : (
        <>
          <div className={styles.tableContainer}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF/CNPJ</th>
                  <th>Email</th>
                  <th>Telefone</th>
                  <th>Data de Cadastro</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {clientes.map((cliente) => (
                  <tr key={cliente.id}>
                    <td className={styles.nameCell}>{cliente.nome}</td>
                    <td>{cliente.cpf_cnpj || '-'}</td>
                    <td>{cliente.email || '-'}</td>
                    <td>{cliente.telefone || '-'}</td>
                    <td>{formatDate(cliente.created_at)}</td>
                    <td className={styles.actions}>
                      <button
                        onClick={() => router.push(`/dashboard/clientes/${cliente.id}`)}
                        className={styles.btnView}
                        title="Ver detalhes"
                      >
                        👁️
                      </button>
                      <button
                        onClick={() => router.push(`/dashboard/clientes/${cliente.id}/editar`)}
                        className={styles.btnEdit}
                        title="Editar"
                      >
                        ✏️
                      </button>
                      <button
                        onClick={() => handleDelete(cliente.id)}
                        className={styles.btnDelete}
                        title="Deletar"
                      >
                        🗑️
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className={styles.pagination}>
            <button
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
              className={styles.btnPage}
            >
              ← Anterior
            </button>
            <span className={styles.pageInfo}>
              Página {page} • Total: {total} cliente(s)
            </span>
            <button
              onClick={() => setPage(p => p + 1)}
              disabled={clientes.length < 20}
              className={styles.btnPage}
            >
              Próxima →
            </button>
          </div>
        </>
      )}
    </div>
  );
}
