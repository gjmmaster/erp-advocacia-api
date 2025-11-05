'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './novo.module.css';

export default function NovoClientePage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    nome: '',
    cpf_cnpj: '',
    email: '',
    telefone: '',
    endereco: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.nome) {
      alert('Nome é obrigatório');
      return;
    }

    try {
      setLoading(true);
      const response = await fetch('/api/tenant/clientes', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Erro ao criar cliente');
      }

      alert('Cliente criado com sucesso!');
      router.push('/dashboard/clientes');
    } catch (err: any) {
      alert(err.message || 'Erro ao criar cliente');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Novo Cliente</h1>
        <button
          onClick={() => router.back()}
          className={styles.btnBack}
        >
          ← Voltar
        </button>
      </div>

      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.formGroup}>
          <label htmlFor="nome">Nome *</label>
          <input
            type="text"
            id="nome"
            value={formData.nome}
            onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
            required
            className={styles.input}
          />
        </div>

        <div className={styles.formRow}>
          <div className={styles.formGroup}>
            <label htmlFor="cpf_cnpj">CPF/CNPJ</label>
            <input
              type="text"
              id="cpf_cnpj"
              value={formData.cpf_cnpj}
              onChange={(e) => setFormData({ ...formData, cpf_cnpj: e.target.value })}
              className={styles.input}
              placeholder="000.000.000-00"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="telefone">Telefone</label>
            <input
              type="tel"
              id="telefone"
              value={formData.telefone}
              onChange={(e) => setFormData({ ...formData, telefone: e.target.value })}
              className={styles.input}
              placeholder="(00) 00000-0000"
            />
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            className={styles.input}
            placeholder="cliente@example.com"
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="endereco">Endereço</label>
          <textarea
            id="endereco"
            value={formData.endereco}
            onChange={(e) => setFormData({ ...formData, endereco: e.target.value })}
            className={styles.textarea}
            rows={3}
            placeholder="Rua, número, bairro, cidade, estado"
          />
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
            {loading ? 'Salvando...' : 'Salvar Cliente'}
          </button>
        </div>
      </form>
    </div>
  );
}
