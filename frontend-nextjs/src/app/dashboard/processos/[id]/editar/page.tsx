'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import styles from './editar.module.css';

interface Cliente {
  id: string;
  nome: string;
}

interface ProcessoForm {
  numero_processo: string;
  cliente_id: string;
  tipo: string;
  status: string;
  descricao: string;
  valor_causa: string;
  data_distribuicao: string;
  vara: string;
  comarca: string;
}

export default function EditarProcessoPage() {
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState<ProcessoForm>({
    numero_processo: '',
    cliente_id: '',
    tipo: '',
    status: '',
    descricao: '',
    valor_causa: '',
    data_distribuicao: '',
    vara: '',
    comarca: '',
  });

  useEffect(() => {
    loadData();
  }, [id]);

  const loadData = async () => {
    try {
      setLoading(true);
      
      const [processosRes, clientesRes] = await Promise.all([
        fetch(`/api/tenant/processos/${id}`),
        fetch('/api/tenant/clientes?page=1&per-page=100')
      ]);

      if (!processosRes.ok || !clientesRes.ok) {
        throw new Error('Erro ao carregar dados');
      }

      const processo = await processosRes.json();
      const clientesData = await clientesRes.json();

      setFormData({
        numero_processo: processo.numero_processo || '',
        cliente_id: processo.cliente_id || '',
        tipo: processo.tipo || '',
        status: processo.status || '',
        descricao: processo.descricao || '',
        valor_causa: processo.valor_causa ? String(processo.valor_causa) : '',
        data_distribuicao: processo.data_distribuicao ? processo.data_distribuicao.split('T')[0] : '',
        vara: processo.vara || '',
        comarca: processo.comarca || '',
      });

      setClientes(clientesData.clientes || []);
    } catch (err) {
      setError('Erro ao carregar dados');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.numero_processo || !formData.cliente_id || !formData.tipo || !formData.status) {
      alert('Preencha todos os campos obrigatórios');
      return;
    }

    try {
      setSubmitting(true);
      
      const payload = {
        numero_processo: formData.numero_processo,
        cliente_id: formData.cliente_id,
        tipo: formData.tipo,
        status: formData.status,
        descricao: formData.descricao || null,
        valor_causa: formData.valor_causa ? parseFloat(formData.valor_causa) : null,
        data_distribuicao: formData.data_distribuicao || null,
        vara: formData.vara || null,
        comarca: formData.comarca || null,
      };

      const response = await fetch(`/api/tenant/processos/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Erro ao atualizar processo');
      }

      alert('Processo atualizado com sucesso!');
      router.push(`/dashboard/processos/${id}`);
    } catch (err: any) {
      alert(err.message || 'Erro ao atualizar processo');
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>Carregando...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>{error}</div>
        <button onClick={() => router.push('/dashboard/processos')} className={styles.btnBack}>
          ← Voltar
        </button>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button onClick={() => router.back()} className={styles.btnBack}>
          ← Voltar
        </button>
        <h1>Editar Processo</h1>
      </div>

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.formGroup}>
          <label htmlFor="numero_processo">Número do Processo *</label>
          <input
            type="text"
            id="numero_processo"
            value={formData.numero_processo}
            onChange={(e) => setFormData({ ...formData, numero_processo: e.target.value })}
            placeholder="Ex: 0000000-00.0000.0.00.0000"
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="cliente_id">Cliente *</label>
          <select
            id="cliente_id"
            value={formData.cliente_id}
            onChange={(e) => setFormData({ ...formData, cliente_id: e.target.value })}
            required
          >
            <option value="">Selecione um cliente</option>
            {clientes.map((cliente) => (
              <option key={cliente.id} value={cliente.id}>
                {cliente.nome}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroup}>
            <label htmlFor="tipo">Tipo *</label>
            <select
              id="tipo"
              value={formData.tipo}
              onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
              required
            >
              <option value="">Selecione</option>
              <option value="Cível">Cível</option>
              <option value="Trabalhista">Trabalhista</option>
              <option value="Criminal">Criminal</option>
              <option value="Tributário">Tributário</option>
              <option value="Família">Família</option>
              <option value="Outro">Outro</option>
            </select>
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="status">Status *</label>
            <select
              id="status"
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              required
            >
              <option value="">Selecione</option>
              <option value="Em Andamento">Em Andamento</option>
              <option value="Suspenso">Suspenso</option>
              <option value="Arquivado">Arquivado</option>
              <option value="Finalizado">Finalizado</option>
            </select>
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroup}>
            <label htmlFor="valor_causa">Valor da Causa (R$)</label>
            <input
              type="number"
              id="valor_causa"
              value={formData.valor_causa}
              onChange={(e) => setFormData({ ...formData, valor_causa: e.target.value })}
              placeholder="0.00"
              step="0.01"
              min="0"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="data_distribuicao">Data de Distribuição</label>
            <input
              type="date"
              id="data_distribuicao"
              value={formData.data_distribuicao}
              onChange={(e) => setFormData({ ...formData, data_distribuicao: e.target.value })}
            />
          </div>
        </div>

        <div className={styles.row}>
          <div className={styles.formGroup}>
            <label htmlFor="vara">Vara</label>
            <input
              type="text"
              id="vara"
              value={formData.vara}
              onChange={(e) => setFormData({ ...formData, vara: e.target.value })}
              placeholder="Ex: 1ª Vara Cível"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="comarca">Comarca</label>
            <input
              type="text"
              id="comarca"
              value={formData.comarca}
              onChange={(e) => setFormData({ ...formData, comarca: e.target.value })}
              placeholder="Ex: São Paulo"
            />
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="descricao">Descrição</label>
          <textarea
            id="descricao"
            value={formData.descricao}
            onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
            placeholder="Descreva o processo..."
            rows={4}
          />
        </div>

        <div className={styles.actions}>
          <button
            type="button"
            onClick={() => router.back()}
            className={styles.btnCancel}
            disabled={submitting}
          >
            Cancelar
          </button>
          <button type="submit" className={styles.btnSubmit} disabled={submitting}>
            {submitting ? 'Salvando...' : 'Salvar Alterações'}
          </button>
        </div>
      </form>
    </div>
  );
}
