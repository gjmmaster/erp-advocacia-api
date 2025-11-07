'use client';

import { useState, useRef } from 'react';
import styles from './DocumentUpload.module.css';

interface Props {
  processoId: string;
  onUploadComplete: () => void;
}

const ALLOWED_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'image/jpeg',
  'image/jpg',
  'image/png',
];

const MAX_SIZE = 10 * 1024 * 1024; // 10MB

export default function DocumentUpload({ processoId, onUploadComplete }: Props) {
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateFile = (file: File): string | null => {
    if (!ALLOWED_TYPES.includes(file.type)) {
      return 'Tipo de arquivo não permitido. Use PDF, DOC, DOCX, JPG ou PNG.';
    }
    if (file.size > MAX_SIZE) {
      return 'Arquivo muito grande. Tamanho máximo: 10MB.';
    }
    return null;
  };

  const handleUpload = async (file: File) => {
    setError('');
    
    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setUploading(true);

      // Criar metadados do documento
      const documentData = {
        nome_arquivo: file.name,
        tipo_arquivo: file.type,
        tamanho_bytes: file.size,
        caminho_storage: `/uploads/${processoId}/${Date.now()}_${file.name}`,
      };

      const response = await fetch(`/api/tenant/processos/${processoId}/documentos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(documentData),
      });

      if (!response.ok) {
        throw new Error('Erro ao fazer upload do documento');
      }

      alert('Documento enviado com sucesso!');
      onUploadComplete();
      
      // Limpar input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (err: any) {
      setError(err.message || 'Erro ao fazer upload');
      console.error(err);
    } finally {
      setUploading(false);
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleUpload(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      handleUpload(e.target.files[0]);
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className={styles.container}>
      <div
        className={`${styles.dropzone} ${dragActive ? styles.active : ''} ${uploading ? styles.uploading : ''}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        onClick={handleClick}
      >
        <input
          ref={fileInputRef}
          type="file"
          className={styles.fileInput}
          onChange={handleChange}
          accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
          disabled={uploading}
        />
        
        {uploading ? (
          <div className={styles.uploadingState}>
            <div className={styles.spinner}></div>
            <p>Enviando documento...</p>
          </div>
        ) : (
          <div className={styles.uploadPrompt}>
            <svg className={styles.icon} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
            <p className={styles.mainText}>
              Arraste um arquivo aqui ou <span className={styles.link}>clique para selecionar</span>
            </p>
            <p className={styles.subText}>
              PDF, DOC, DOCX, JPG, PNG (máx. 10MB)
            </p>
          </div>
        )}
      </div>

      {error && (
        <div className={styles.error}>
          ⚠️ {error}
        </div>
      )}
    </div>
  );
}
