'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import styles from './detalhes.module.css';

interface Cliente {
  id: string;
  nome: string;
  cpf_cnpj?: string;
  email?: string;
  telefone?: string;
  endereco?: string;
  created_at: string;
  updated_at: string;
}

interface Processo {
  id: string;
  numero_processo: string;
  tipo: string;
  status: string;
  valor_causa?: number;
}

export default function ClienteDetalhesPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;
  
  const [cliente, setCliente] = useState<Cliente | null>(null);
  const [processos, setProcessos] = useState<Processo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadCliente();
    loadProcessos();
  }, [id]);

  const loadCliente = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/tenant/clientes/${id}`);
      
      if (!response.ok) {
        throw new Error('Erro ao carregar cliente');
      }

      const data = await response.json();
      setCliente(data);
    } catch (err) {
      setError('Erro ao carregar cliente');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadProcessos = async () => {
    try {
      const response = await fetch(`/api/tenant/processos?cliente_id=${id}&page=1&per-page=100`);
      
      if (response.ok) {
        const data = await response.json();
        setProcessos(data.processos || []);
      }
    } catch (err) {
      console.error('Erro ao carregar processos:', err);
    }
  };

  const handleDelete = async () => {
    if (processos.length > 0) {
      alert('Não é possível deletar um cliente que possui processos vinculados.');
      return;
    }

    if (!confirm('Tem certeza que deseja deletar este cliente?')) {
      return;
    }

    try {
      const response = await fetch(`/api/tenant/clientes/${id}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        throw new Error('Erro ao deletar cliente');
      }

      alert('Cliente deletado com sucesso!');
      router.push('/dashboard/clientes');
    } catch (err) {
      alert('Erro ao deletar cliente');
      console.error(err);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('pt-BR');
  };

  const formatCurrency = (value?: number) => {
    if (!value) return '-';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>Carregando cliente...</div>
      </div>
    );
  }

  if (error || !cliente) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>{error || 'Cliente não encontrado'}</div>
        <button onClick={() => router.push('/dashboard/clientes')} className={styles.btnBack}>
          ← Voltar
        </button>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <button onClick={() => router.push('/dashboard/clientes')} className={styles.btnBack}>
            ← Voltar
          </button>
          <h1>Detalhes do Cliente</h1>
        </div>
        <div className={styles.actions}>
          <button
            onClick={() => router.push(`/dashboard/clientes/${id}/editar`)}
            className={styles.btnEdit}
          >
            ✏️ Editar
          </button>
          <button onClick={handleDelete} className={styles.btnDelete}>
            🗑️ Deletar
          </button>
        </div>
      </div>

      <div className={styles.card}>
        <div className={styles.section}>
          <h2>Informações Básicas</h2>
          <div className={styles.grid}>
            <div className={styles.field}>
              <label>Nome</label>
              <p>{cliente.nome}</p>
            </div>
            <div className={styles.field}>
              <label>CPF/CNPJ</label>
              <p>{cliente.cpf_cnpj || '-'}</p>
            </div>
            <div className={styles.field}>
              <label>Email</label>
              <p>{cliente.email || '-'}</p>
            </div>
            <div className={styles.field}>
              <label>Telefone</label>
              <p>{cliente.telefone || '-'}</p>
            </div>
          </div>
        </div>

        {cliente.endereco && (
          <div className={styles.section}>
            <h2>Endereço</h2>
            <p className={styles.description}>{cliente.endereco}</p>
          </div>
        )}

        <div className={styles.section}>
          <h2>Processos Vinculados ({processos.length})</h2>
          {processos.length === 0 ? (
            <p className={styles.emptyState}>Nenhum processo vinculado a este cliente.</p>
          ) : (
            <div className={styles.processosList}>
              {processos.map((processo) => (
                <div
                  key={processo.id}
                  className={styles.processoCard}
                  onClick={() => router.push(`/dashboard/processos/${processo.id}`)}
                >
                  <div className={styles.processoHeader}>
                    <span className={styles.processoNumero}>{processo.numero_processo}</span>
                    <span className={`${styles.badge} ${styles[`badge${processo.status.replace(/\s/g, '')}`]}`}>
                      {processo.status}
                    </span>
                  </div>
                  <div className={styles.processoDetails}>
                    <span>Tipo: {processo.tipo}</span>
                    {processo.valor_causa && (
                      <span>Valor: {formatCurrency(processo.valor_causa)}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className={styles.section}>
          <h2>Informações do Sistema</h2>
          <div className={styles.grid}>
            <div className={styles.field}>
              <label>Criado em</label>
              <p>{formatDate(cliente.created_at)}</p>
            </div>
            <div className={styles.field}>
              <label>Última atualização</label>
              <p>{formatDate(cliente.updated_at)}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
