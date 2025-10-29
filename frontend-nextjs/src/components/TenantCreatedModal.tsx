'use client';

import { useState } from 'react';
import styles from './TenantCreatedModal.module.css';

interface TenantCreatedModalProps {
  isOpen: boolean;
  onClose: () => void;
  tenantName: string;
  email: string;
  tempPassword: string;
  emailSent: boolean;
}

export default function TenantCreatedModal({
  isOpen,
  onClose,
  tenantName,
  email,
  tempPassword,
  emailSent,
}: TenantCreatedModalProps) {
  const [copied, setCopied] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  if (!isOpen) return null;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(tempPassword);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Erro ao copiar:', err);
    }
  };

  const handleClose = () => {
    if (!showConfirm) {
      setShowConfirm(true);
    } else {
      onClose();
    }
  };

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        <div className={styles.header}>
          <h2>✅ Tenant Criado com Sucesso!</h2>
        </div>

        <div className={styles.content}>
          <div className={styles.info}>
            <p><strong>Escritório:</strong> {tenantName}</p>
            <p><strong>Email:</strong> {email}</p>
          </div>

          {!emailSent && (
            <div className={styles.warning}>
              ⚠️ O email não pôde ser enviado. Envie a senha manualmente ao usuário.
            </div>
          )}

          <div className={styles.passwordSection}>
            <h3>🔑 Senha Temporária</h3>
            <p className={styles.important}>
              ⚠️ <strong>IMPORTANTE:</strong> Anote esta senha. Ela não será exibida novamente!
            </p>
            
            <div className={styles.passwordBox}>
              <code className={styles.password}>{tempPassword}</code>
              <button 
                onClick={handleCopy}
                className={styles.copyButton}
              >
                {copied ? '✓ Copiado!' : '📋 Copiar'}
              </button>
            </div>
          </div>

          <div className={styles.instructions}>
            <h4>📱 Próximos Passos:</h4>
            <ol>
              <li>Copie a senha acima</li>
              <li>Envie ao usuário por WhatsApp, telefone ou outro meio seguro</li>
              <li>Informe que ele deve trocar a senha no primeiro login</li>
            </ol>
          </div>
        </div>

        <div className={styles.footer}>
          {!showConfirm ? (
            <button onClick={handleClose} className={styles.closeButton}>
              Fechar
            </button>
          ) : (
            <div className={styles.confirmClose}>
              <p>⚠️ Tem certeza? A senha não será exibida novamente!</p>
              <div className={styles.confirmButtons}>
                <button 
                  onClick={() => setShowConfirm(false)}
                  className={styles.cancelButton}
                >
                  Cancelar
                </button>
                <button 
                  onClick={onClose}
                  className={styles.confirmButton}
                >
                  Sim, já anotei
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
