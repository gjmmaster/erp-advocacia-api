'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import DocumentUpload from '@/components/DocumentUpload';
import DocumentList from '@/components/DocumentList';
import HistoricoTimeline from '@/components/HistoricoTimeline';
import styles from './detalhes.module.css';

interface Processo {
  id: string;
  numero_processo: string;
  cliente_id: string;
  cliente_nome: string;
  tipo: string;
  status: string;
  descricao?: string;
  valor_causa?: number;
  data_distribuicao?: string;
  vara?: string;
  comarca?: string;
  created_at: string;
  updated_at: string;
}

interface Documento {
  id: string;
  nome_arquivo: string;
  tipo_arquivo: string;
  tamanho_bytes: number;
  caminho_storage: string;
  uploaded_by_name?: string;
  created_at: string;
}

interface HistoricoItem {
  id: string;
  acao: string;
  campo_alterado?: string;
  valor_anterior?: string;
  valor_novo?: string;
  user_name: string;
  created_at: string;
}

export default function ProcessoDetalhesPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;
  
  const [processo, setProcesso] = useState<Processo | null>(null);
  const [documentos, setDocumentos] = useState<Documento[]>([]);
  const [historico, setHistorico] = useState<HistoricoItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingDocs, setLoadingDocs] = useState(false);
  const [loadingHistorico, setLoadingHistorico] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProcesso();
    loadDocumentos();
    loadHistorico();
  }, [id]);

  const loadProcesso = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/tenant/processos/${id}`);
      
      if (!response.ok) {
        throw new Error('Erro ao carregar processo');
      }

      const data = await response.json();
      setProcesso(data);
    } catch (err) {
      setError('Erro ao carregar processo');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadDocumentos = async () => {
    try {
      setLoadingDocs(true);
      const response = await fetch(`/api/tenant/processos/${id}/documentos`);
      
      if (response.ok) {
        const data = await response.json();
        setDocumentos(data || []);
      }
    } catch (err) {
      console.error('Erro ao carregar documentos:', err);
    } finally {
      setLoadingDocs(false);
    }
  };

  const loadHistorico = async () => {
    try {
      setLoadingHistorico(true);
      const response = await fetch(`/api/tenant/processos/${id}/historico`);
      
      if (response.ok) {
        const data = await response.json();
        setHistorico(data.historico || []);
      }
    } catch (err) {
      console.error('Erro ao carregar histórico:', err);
    } finally {
      setLoadingHistorico(false);
    }
  };

  const handleDelete = async () => {
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
      router.push('/dashboard/processos');
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

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('pt-BR');
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>Carregando processo...</div>
      </div>
    );
  }

  if (error || !processo) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>{error || 'Processo não encontrado'}</div>
        <button onClick={() => router.push('/dashboard/processos')} className={styles.btnBack}>
          ← Voltar
        </button>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <button onClick={() => router.push('/dashboard/processos')} className={styles.btnBack}>
            ← Voltar
          </button>
          <h1>Detalhes do Processo</h1>
        </div>
        <div className={styles.actions}>
          <button
            onClick={() => router.push(`/dashboard/processos/${id}/editar`)}
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
              <label>Número do Processo</label>
              <p>{processo.numero_processo}</p>
            </div>
            <div className={styles.field}>
              <label>Cliente</label>
              <p>{processo.cliente_nome}</p>
            </div>
            <div className={styles.field}>
              <label>Tipo</label>
              <p>{processo.tipo}</p>
            </div>
            <div className={styles.field}>
              <label>Status</label>
              <p>
                <span className={`${styles.badge} ${styles[`badge${(processo.status || '').replace(/\s/g, '')}`]}`}>
                  {processo.status || 'N/A'}
                </span>
              </p>
            </div>
          </div>
        </div>

        <div className={styles.section}>
          <h2>Detalhes do Processo</h2>
          <div className={styles.grid}>
            <div className={styles.field}>
              <label>Valor da Causa</label>
              <p>{formatCurrency(processo.valor_causa)}</p>
            </div>
            <div className={styles.field}>
              <label>Data de Distribuição</label>
              <p>{formatDate(processo.data_distribuicao)}</p>
            </div>
            <div className={styles.field}>
              <label>Vara</label>
              <p>{processo.vara || '-'}</p>
            </div>
            <div className={styles.field}>
              <label>Comarca</label>
              <p>{processo.comarca || '-'}</p>
            </div>
          </div>
        </div>

        {processo.descricao && (
          <div className={styles.section}>
            <h2>Descrição</h2>
            <p className={styles.description}>{processo.descricao}</p>
          </div>
        )}

        <div className={styles.section}>
          <h2>Documentos ({documentos.length})</h2>
          <DocumentUpload processoId={id} onUploadComplete={loadDocumentos} />
          {loadingDocs ? (
            <div className={styles.loading}>Carregando documentos...</div>
          ) : (
            <DocumentList 
              documentos={documentos} 
              processoId={id}
              onDelete={loadDocumentos}
            />
          )}
        </div>

        <div className={styles.section}>
          <h2>Histórico de Alterações ({historico.length})</h2>
          {loadingHistorico ? (
            <div className={styles.loading}>Carregando histórico...</div>
          ) : (
            <HistoricoTimeline items={historico} />
          )}
        </div>

        <div className={styles.section}>
          <h2>Informações do Sistema</h2>
          <div className={styles.grid}>
            <div className={styles.field}>
              <label>Criado em</label>
              <p>{formatDate(processo.created_at)}</p>
            </div>
            <div className={styles.field}>
              <label>Última atualização</label>
              <p>{formatDate(processo.updated_at)}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
