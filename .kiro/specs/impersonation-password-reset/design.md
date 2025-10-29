# Design Document - Reset de Senha por Super Admin

## Overview

Este documento detalha o design técnico para permitir que o super admin visualize e resete senhas de usuários tenants, com foco na funcionalidade de exibir senha temporária.

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│  FLUXO: Super Admin Cria Tenant                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. Super Admin preenche formulário                         │
│     - Nome do escritório                                    │
│     - Email do admin                                        │
│     - Limite de operadores                                  │
│                                                             │
│  2. Backend gera senha temporária                           │
│     - 12 caracteres aleatórios                              │
│     - Letras maiúsculas + minúsculas + números              │
│     - Armazena hash no banco                                │
│                                                             │
│  3. Backend retorna senha em texto plano                    │
│     - APENAS nesta resposta                                 │
│     - Nunca mais será exibida                               │
│                                                             │
│  4. Frontend exibe modal com senha                          │
│     - Texto grande e legível                                │
│     - Botão "Copiar Senha"                                  │
│     - Aviso: "Anote esta senha"                             │
│                                                             │
│  5. Super admin copia e envia ao usuário                    │
│     - WhatsApp, telefone, etc                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Components and Interfaces

### 1. Backend: Modificar Handler de Criação de Tenant

**Arquivo:** `src/juridico/api/handlers.clj`

**Mudança:** Retornar senha temporária na resposta

```clojure
(defn provision-tenant-handler
  "Handler para o Super Admin criar um novo tenant e seu usuário Admin.
   MODIFICADO: Agora retorna a senha temporária na resposta."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/provision-payload body-params)
    (let [resultado (p/criar-tenant-e-usuario-master db-repo body-params)]
      ;; Tentar enviar email (mas não falhar se não conseguir)
      (try
        (email-service/send-welcome-email (:user resultado) (:tenant resultado))
        (catch Exception e
          (println "AVISO: Falha ao enviar email:" (.getMessage e))))
      
      ;; IMPORTANTE: Retornar senha temporária
      {:status 201
       :body (assoc resultado
                :message "Tenant criado com sucesso."
                :temp_password (:temp_password (:user resultado))  ; ← NOVO!
                :email_sent (try 
                              (email-service/send-welcome-email (:user resultado) (:tenant resultado))
                              true
                              (catch Exception e false)))})
    {:status 400
     :body {:error "Dados para provisionamento inválidos."
            :details (s/explain-data :juridico.api.specs/provision-payload body-params)}}))
```

---

### 2. Frontend: Modal de Sucesso com Senha

**Arquivo:** `frontend-nextjs/src/components/TenantCreatedModal.tsx`

**Novo componente:**

```typescript
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
```

---

### 3. Frontend: Estilos do Modal

**Arquivo:** `frontend-nextjs/src/components/TenantCreatedModal.module.css`

```css
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.header {
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.header h2 {
  margin: 0;
  font-size: 24px;
}

.content {
  padding: 24px;
}

.info {
  margin-bottom: 20px;
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
}

.info p {
  margin: 8px 0;
  font-size: 14px;
}

.warning {
  padding: 12px 16px;
  background: #fed7d7;
  border: 1px solid #fc8181;
  border-radius: 8px;
  color: #c53030;
  margin-bottom: 20px;
  font-size: 14px;
}

.passwordSection {
  margin: 24px 0;
  padding: 20px;
  background: #fffaf0;
  border: 2px solid #f6ad55;
  border-radius: 8px;
}

.passwordSection h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #2d3748;
}

.important {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #c05621;
}

.passwordBox {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 16px;
  background: white;
  border: 2px solid #f6ad55;
  border-radius: 8px;
}

.password {
  flex: 1;
  font-size: 24px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  color: #2d3748;
  letter-spacing: 2px;
}

.copyButton {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.copyButton:hover {
  background: #5568d3;
  transform: translateY(-2px);
}

.instructions {
  margin-top: 24px;
  padding: 16px;
  background: #ebf8ff;
  border-radius: 8px;
}

.instructions h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #2c5282;
}

.instructions ol {
  margin: 0;
  padding-left: 20px;
}

.instructions li {
  margin: 8px 0;
  font-size: 14px;
  color: #2d3748;
}

.footer {
  padding: 20px 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.closeButton {
  padding: 12px 32px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.closeButton:hover {
  background: #5568d3;
  transform: translateY(-2px);
}

.confirmClose {
  width: 100%;
}

.confirmClose p {
  margin: 0 0 16px 0;
  color: #c53030;
  font-weight: 600;
  text-align: center;
}

.confirmButtons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.cancelButton {
  padding: 12px 24px;
  background: #e2e8f0;
  color: #2d3748;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.cancelButton:hover {
  background: #cbd5e0;
}

.confirmButton {
  padding: 12px 24px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.confirmButton:hover {
  background: #38a169;
}

@media (max-width: 640px) {
  .password {
    font-size: 18px;
  }
  
  .passwordBox {
    flex-direction: column;
  }
  
  .copyButton {
    width: 100%;
  }
}
```

---

### 4. Frontend: Integrar Modal no Dashboard

**Arquivo:** `frontend-nextjs/src/app/super-admin/dashboard/page.tsx`

**Modificar para usar o novo modal:**

```typescript
// Adicionar imports
import TenantCreatedModal from '@/components/TenantCreatedModal';

// Adicionar estados
const [showSuccessModal, setShowSuccessModal] = useState(false);
const [createdTenant, setCreatedTenant] = useState<{
  tenantName: string;
  email: string;
  tempPassword: string;
  emailSent: boolean;
} | null>(null);

// Modificar handleCreateTenant
const handleCreateTenant = async (data: CreateTenantData) => {
  try {
    const response = await fetch('/api/admin/tenants', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });

    if (response.ok) {
      const result = await response.json();
      
      // Armazenar dados para o modal
      setCreatedTenant({
        tenantName: result.tenant.company_name,
        email: result.user.email,
        tempPassword: result.temp_password,  // ← NOVO!
        emailSent: result.email_sent || false,
      });
      
      setShowSuccessModal(true);  // ← Mostrar modal
      setShowCreateModal(false);
      fetchTenants();
    } else {
      const error = await response.json();
      alert(`Erro: ${error.error}`);
    }
  } catch (error) {
    alert('Erro ao criar tenant');
  }
};

// Adicionar modal no JSX
{showSuccessModal && createdTenant && (
  <TenantCreatedModal
    isOpen={showSuccessModal}
    onClose={() => {
      setShowSuccessModal(false);
      setCreatedTenant(null);
    }}
    tenantName={createdTenant.tenantName}
    email={createdTenant.email}
    tempPassword={createdTenant.tempPassword}
    emailSent={createdTenant.emailSent}
  />
)}
```

---

## Data Models

### Response de Criação de Tenant (Modificado)

```typescript
interface CreateTenantResponse {
  message: string;
  tenant: {
    id: string;
    company_name: string;
    subdomain: string;
  };
  user: {
    email: string;
    temp_password: string;  // ← NOVO! Apenas nesta resposta
  };
  temp_password: string;  // ← NOVO! Duplicado para facilitar acesso
  email_sent: boolean;    // ← NOVO! Indica se email foi enviado
}
```

---

## Security Considerations

1. **Senha em Texto Plano:** Apenas na resposta HTTP, nunca armazenada
2. **HTTPS Obrigatório:** Em produção, sempre usar HTTPS
3. **Timeout:** Modal força confirmação antes de fechar
4. **Audit Log:** Registrar quando super admin cria tenant
5. **Senha Temporária:** Usuário deve trocar no primeiro login

---

## Testing Strategy

### Testes Manuais

1. Criar tenant e verificar que senha aparece no modal
2. Copiar senha e verificar que foi copiada
3. Tentar fechar modal e verificar confirmação
4. Verificar que senha não aparece em logs do navegador
5. Verificar que email_sent indica corretamente se email foi enviado

---

**Documento criado em:** 29/10/2025  
**Status:** Pronto para Implementação
