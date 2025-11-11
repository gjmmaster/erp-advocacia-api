'use client';

import { useState } from 'react';
import styles from './DocumentList.module.css';

interface Documento {
  id: string;
  nome_arquivo: string;
  tipo_arquivo: string;
  tamanho_bytes: number;
  caminho_storage: string;
  uploaded_by_name?: string;
  created_at: string;
}

interface Props {
  documentos: Documento[];
  processoId: string;
  onDelete: () => void;
}

export default function DocumentList({ documentos, processoId, onDelete }: Props) {
  const [deleting, setDeleting] = useState<string | null>(null);

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getFileIcon = (tipo: string): string => {
    if (tipo.includes('pdf')) return '📄';
    if (tipo.includes('word') || tipo.includes('document')) return '📝';
    if (tipo.includes('image')) return '🖼️';
    return '📎';
  };

  const handleDelete = async (documentoId: string, nomeArquivo: string) => {
    if (!confirm(`Tem certeza que deseja deletar "${nomeArquivo}"?`)) {
      return;
    }

    try {
      setDeleting(documentoId);

      // Pegar o token
      const tokenResponse = await fetch('/api/auth/token');
      if (!tokenResponse.ok) {
        throw new Error('Erro ao obter token de autenticação');
      }
      const { token } = await tokenResponse.json();

      // Deletar direto no backend
      const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'https://erp-advocacia-api.onrender.com';
      const response = await fetch(
        `${backendUrl}/api/tenant/processos/${processoId}/documentos/${documentoId}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error('Erro ao deletar documento');
      }

      alert('Documento deletado com sucesso!');
      onDelete();
    } catch (err) {
      alert('Erro ao deletar documento');
      console.error(err);
    } finally {
      setDeleting(null);
    }
  };

  const handleDownload = async (documento: Documento) => {
    try {
      // Pegar o token
      const tokenResponse = await fetch('/api/auth/token');
      if (!tokenResponse.ok) {
        throw new Error('Erro ao obter token de autenticação');
      }
      const { token } = await tokenResponse.json();

      // Buscar URL de download do backend
      const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'https://erp-advocacia-api.onrender.com';
      const response = await fetch(
        `${backendUrl}/api/tenant/processos/${processoId}/documentos/${documento.id}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error('Erro ao gerar link de download');
      }

      const data = await response.json();
      
      // Abrir URL de download em nova aba
      window.open(data.download_url, '_blank');
    } catch (err) {
      alert('Erro ao baixar documento');
      console.error(err);
    }
  };

  if (documentos.length === 0) {
    return (
      <div className={styles.empty}>
        <p>📁 Nenhum documento anexado</p>
      </div>
    );
  }

  return (
    <div className={styles.list}>
      {documentos.map((doc) => (
        <div key={doc.id} className={styles.item}>
          <div className={styles.icon}>
            {getFileIcon(doc.tipo_arquivo)}
          </div>
          
          <div className={styles.info}>
            <div className={styles.name}>{doc.nome_arquivo}</div>
            <div className={styles.meta}>
              <span>{formatFileSize(doc.tamanho_bytes)}</span>
              <span>•</span>
              <span>{formatDate(doc.created_at)}</span>
              {doc.uploaded_by_name && (
                <>
                  <span>•</span>
                  <span>{doc.uploaded_by_name}</span>
                </>
              )}
            </div>
          </div>

          <div className={styles.actions}>
            <button
              onClick={() => handleDownload(doc)}
              className={styles.btnDownload}
              title="Baixar documento"
            >
              ⬇️
            </button>
            <button
              onClick={() => handleDelete(doc.id, doc.nome_arquivo)}
              className={styles.btnDelete}
              disabled={deleting === doc.id}
              title="Deletar documento"
            >
              {deleting === doc.id ? '⏳' : '🗑️'}
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
