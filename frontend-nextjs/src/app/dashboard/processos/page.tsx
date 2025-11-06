'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './processos.module.css';

interface Processo {
  id: number;
  numero_processo: string;
  cliente_nome: string;
  tipo: string;
  status: string;
  valor_causa?: number;
  created_at: string;
}

export default function ProcessosPage() {
  const router = useRouter();
  const [processos, setProcessos] = useState<Processo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    loadProcessos();
  }, [page, statusFilter]);

  const loadProcessos = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('per-page', '20');
      if (statusFilter) params.append('status', statusFilter);

      const response = await fetch(`/api/tenant/processos?${params.toString()}`);
      
      if (!response.ok) {
        throw new Error('Erro ao carregar processos');
      }

      const data = await response.json();
      setProcessos(data.processos || []);
      setTotal(data.total || 0);
    } catch (err) {
      setError('Erro ao carregar processos');
      console.error(err);
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
      const response = await fetch(`/api/tenant/processos/search?q=${encodeURIComponent(search)}`);
      
      if (!response.ok) {
        throw new Error('Erro ao buscar processos');
      }

      const data = await response.json();
      setProcessos(data.processos || []);
      setTotal(data.total || 0);
    } catch (err) {
      setError('Erro ao buscar processos');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Tem certeza que deseja deletar este processo?')) {
      return;
    }

    try {
      const response = await fetch(`/api/tenant/processos/${id}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        throw new Error('Erro ao deletar processo');
      }

      alert('Processo deletado com sucesso!');
      loadProcessos();
    } catch (err) {
      alert('Erro ao deletar processo');
      console.error(err);
    }
  };

  const formatCurrency = (value?: number) => {
    if (!value) return '-';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR');
  };

  if (loading && processos.length === 0) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>Carregando processos...</div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Processos Jurídicos</h1>
        <button
          className={styles.btnPrimary}
          onClick={() => router.push('/dashboard/processos/novo')}
        >
          + Novo Processo
        </button>
      </div>

      {error && <div className={styles.error}>{error}</div>}

      <div className={styles.filters}>
        <form onSubmit={handleSearch} className={styles.searchForm}>
          <input
            type="text"
            placeholder="Buscar por número, cliente ou descrição..."
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
                loadProcessos();
              }}
              className={styles.btnClear}
            >
              Limpar
            </button>
          )}
        </form>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className={styles.select}
        >
          <option value="">Todos os status</option>
          <option value="Em Andamento">Em Andamento</option>
          <option value="Suspenso">Suspenso</option>
          <option value="Arquivado">Arquivado</option>
          <option value="Finalizado">Finalizado</option>
        </select>
      </div>

      {processos.length === 0 ? (
        <div className={styles.empty}>
          <p>Nenhum processo cadastrado</p>
          <button
            className={styles.btnPrimary}
            onClick={() => router.push('/dashboard/processos/novo')}
          >
            Cadastrar Primeiro Processo
          </button>
        </div>
      ) : (
        <>
          <div className={styles.tableContainer}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Número</th>
                  <th>Cliente</th>
                  <th>Tipo</th>
                  <th>Status</th>
                  <th>Valor da Causa</th>
                  <th>Data</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {processos.map((processo) => (
                  <tr key={processo.id}>
                    <td>{processo.numero_processo}</td>
                    <td>{processo.cliente_nome}</td>
                    <td>{processo.tipo}</td>
                    <td>
                      <span className={`${styles.badge} ${styles[`badge${(processo.status || '').replace(/\s/g, '')}`]}`}>
                        {processo.status || 'N/A'}
                      </span>
                    </td>
                    <td>{formatCurrency(processo.valor_causa)}</td>
                    <td>{formatDate(processo.created_at)}</td>
                    <td className={styles.actions}>
                      <button
                        onClick={() => router.push(`/dashboard/processos/${processo.id}`)}
                        className={styles.btnView}
                        title="Ver detalhes"
                      >
                        👁️
                      </button>
                      <button
                        onClick={() => router.push(`/dashboard/processos/${processo.id}/editar`)}
                        className={styles.btnEdit}
                        title="Editar"
                      >
                        ✏️
                      </button>
                      <button
                        onClick={() => handleDelete(processo.id)}
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
              Página {page} • Total: {total} processo(s)
            </span>
            <button
              onClick={() => setPage(p => p + 1)}
              disabled={processos.length < 20}
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
