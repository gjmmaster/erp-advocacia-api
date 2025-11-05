'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './novo.module.css';

interface Cliente {
  id: number;
  nome: string;
}

export default function NovoProcessoPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [formData, setFormData] = useState({
    numero_processo: '',
    cliente_id: '',
    tipo: '',
    vara_tribunal: '',
    comarca: '',
    uf: '',
    status: 'Em Andamento',
    valor_causa: '',
    data_distribuicao: '',
    descricao: '',
    observacoes: '',
  });

  useEffect(() => {
    loadClientes();
  }, []);

  const loadClientes = async () => {
    try {
      const response = await fetch('/api/tenant/clientes?per-page=100');
      if (response.ok) {
        const data = await response.json();
        setClientes(data.clientes || []);
      }
    } catch (err) {
      console.error('Erro ao carregar clientes:', err);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.numero_processo) {
      alert('Número do processo é obrigatório');
      return;
    }

    if (!formData.cliente_id) {
      alert('Cliente é obrigatório');
      return;
    }

    if (!formData.tipo) {
      alert('Tipo é obrigatório');
      return;
    }

    try {
      setLoading(true);

      // Preparar dados
      const payload: any = {
        numero_processo: formData.numero_processo,
        cliente_id: formData.cliente_id, // Manter como string para evitar perda de precisão
        tipo: formData.tipo,
        status: formData.status,
      };

      // Adicionar campos opcionais
      if (formData.vara_tribunal) payload.vara_tribunal = formData.vara_tribunal;
      if (formData.comarca) payload.comarca = formData.comarca;
      if (formData.uf) payload.uf = formData.uf;
      if (formData.valor_causa) payload.valor_causa = parseFloat(formData.valor_causa);
      if (formData.data_distribuicao) payload.data_distribuicao = formData.data_distribuicao;
      if (formData.descricao) payload.descricao = formData.descricao;
      if (formData.observacoes) payload.observacoes = formData.observacoes;

      const response = await fetch('/api/tenant/processos', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Erro ao criar processo');
      }

      alert('Processo criado com sucesso!');
      router.push('/dashboard/processos');
    } catch (err: any) {
      alert(err.message || 'Erro ao criar processo');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Novo Processo</h1>
        <button
          onClick={() => router.back()}
          className={styles.btnBack}
        >
          ← Voltar
        </button>
      </div>

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.section}>
          <h2>Informações Básicas</h2>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label htmlFor="numero_processo">Número do Processo *</label>
              <input
                type="text"
                id="numero_processo"
                value={formData.numero_processo}
                onChange={(e) => setFormData({ ...formData, numero_processo: e.target.value })}
                required
                className={styles.input}
                placeholder="0000000-00.0000.0.00.0000"
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="cliente_id">Cliente *</label>
              <select
                id="cliente_id"
                value={formData.cliente_id}
                onChange={(e) => setFormData({ ...formData, cliente_id: e.target.value })}
                required
                className={styles.select}
              >
                <option value="">Selecione um cliente</option>
                {clientes.map((cliente) => (
                  <option key={cliente.id} value={cliente.id}>
                    {cliente.nome}
                  </option>
                ))}
              </select>
              {clientes.length === 0 && (
                <small className={styles.hint}>
                  Nenhum cliente cadastrado.{' '}
                  <a href="/dashboard/clientes/novo" className={styles.link}>
                    Cadastrar cliente
                  </a>
                </small>
              )}
            </div>
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label htmlFor="tipo">Tipo *</label>
              <input
                type="text"
                id="tipo"
                value={formData.tipo}
                onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
                required
                className={styles.input}
                placeholder="Ex: Cível, Trabalhista, Criminal"
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="status">Status</label>
              <select
                id="status"
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                className={styles.select}
              >
                <option value="Em Andamento">Em Andamento</option>
                <option value="Suspenso">Suspenso</option>
                <option value="Arquivado">Arquivado</option>
                <option value="Finalizado">Finalizado</option>
              </select>
            </div>
          </div>
        </div>

        <div className={styles.section}>
          <h2>Localização</h2>

          <div className={styles.formGroup}>
            <label htmlFor="vara_tribunal">Vara/Tribunal</label>
            <input
              type="text"
              id="vara_tribunal"
              value={formData.vara_tribunal}
              onChange={(e) => setFormData({ ...formData, vara_tribunal: e.target.value })}
              className={styles.input}
              placeholder="Ex: 1ª Vara Cível"
            />
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label htmlFor="comarca">Comarca</label>
              <input
                type="text"
                id="comarca"
                value={formData.comarca}
                onChange={(e) => setFormData({ ...formData, comarca: e.target.value })}
                className={styles.input}
                placeholder="Ex: São Paulo"
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="uf">UF</label>
              <input
                type="text"
                id="uf"
                value={formData.uf}
                onChange={(e) => setFormData({ ...formData, uf: e.target.value.toUpperCase() })}
                className={styles.input}
                placeholder="SP"
                maxLength={2}
              />
            </div>
          </div>
        </div>

        <div className={styles.section}>
          <h2>Valores e Datas</h2>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label htmlFor="valor_causa">Valor da Causa (R$)</label>
              <input
                type="number"
                id="valor_causa"
                value={formData.valor_causa}
                onChange={(e) => setFormData({ ...formData, valor_causa: e.target.value })}
                className={styles.input}
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
                className={styles.input}
              />
            </div>
          </div>
        </div>

        <div className={styles.section}>
          <h2>Detalhes</h2>

          <div className={styles.formGroup}>
            <label htmlFor="descricao">Descrição</label>
            <textarea
              id="descricao"
              value={formData.descricao}
              onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
              className={styles.textarea}
              rows={4}
              placeholder="Descreva o objeto do processo..."
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="observacoes">Observações</label>
            <textarea
              id="observacoes"
              value={formData.observacoes}
              onChange={(e) => setFormData({ ...formData, observacoes: e.target.value })}
              className={styles.textarea}
              rows={3}
              placeholder="Observações internas..."
            />
          </div>
        </div>

        <div className={styles.formActions}>
          <button
            type="button"
            onClick={() => router.back()}
            className={styles.btnCancel}
            disabled={loading}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className={styles.btnSubmit}
            disabled={loading}
          >
            {loading ? 'Salvando...' : 'Salvar Processo'}
          </button>
        </div>
      </form>
    </div>
  );
}
