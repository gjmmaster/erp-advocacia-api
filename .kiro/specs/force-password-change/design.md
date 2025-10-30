# Design Document - Forçar Troca de Senha Temporária

**Data:** 30 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Em Revisão

---

## Overview

Este documento detalha o design técnico para implementar a funcionalidade de forçar usuários a trocarem suas senhas temporárias no primeiro login. A solução garante que apenas o usuário final conheça sua senha definitiva, seguindo melhores práticas de segurança.

### Objetivos do Design

1. **Segurança:** Implementar fluxo seguro de troca de senha
2. **Usabilidade:** Interface clara e intuitiva
3. **Performance:** Validação rápida e responsiva
4. **Manutenibilidade:** Código limpo e bem estruturado
5. **Escalabilidade:** Suportar múltiplos tenants

---

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Browser                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Change Password Page (Next.js)                      │  │
│  │  - Form validation                                    │  │
│  │  - Password strength indicator                       │  │
│  │  - Real-time feedback                                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTPS
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Next.js BFF (Middleware)                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Auth Middleware                                      │  │
│  │  - Check JWT for temporary_password flag             │  │
│  │  - Redirect to /change-password if needed            │  │
│  │  - Allow only specific routes                        │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  API Route: /api/auth/change-password                │  │
│  │  - Validate current password                         │  │
│  │  - Validate new password strength                    │  │
│  │  - Proxy to backend                                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Backend Clojure API                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  POST /api/auth/change-password                      │  │
│  │  - Validate current password (bcrypt)                │  │
│  │  - Hash new password (bcrypt cost 12)                │  │
│  │  - Update database                                   │  │
│  │  - Generate new JWT                                  │  │
│  │  - Create audit log entry                            │  │
│  │  - Send confirmation email                           │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  users table                                          │  │
│  │  - temporary_password: boolean                       │  │
│  │  - password_hash: string                             │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  audit_log table                                      │  │
│  │  - action: string                                     │  │
│  │  - user_id, tenant_id, timestamp                     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## Components and Interfaces

### 1. Database Schema

#### users table (alteração)

```sql
ALTER TABLE users 
ADD COLUMN temporary_password BOOLEAN DEFAULT false;

-- Índice para performance
CREATE INDEX idx_users_temporary_password 
ON users(temporary_password) 
WHERE temporary_password = true;
```

**Campos relevantes:**
- `id`: BIGINT (PK)
- `email`: VARCHAR(255)
- `password_hash`: VARCHAR(255)
- `temporary_password`: BOOLEAN (novo)
- `tenant_id`: BIGINT (FK)
- `role`: VARCHAR(50)

#### audit_log table (existente)

```sql
-- Já existe, apenas novos tipos de ação
```

**Novos tipos de ação:**
- `TEMPORARY_PASSWORD_CREATED`
- `LOGIN_WITH_TEMPORARY_PASSWORD`
- `PASSWORD_CHANGE_PAGE_ACCESSED`
- `TEMPORARY_PASSWORD_CHANGED`
- `PASSWORD_CHANGE_FAILED`

---

### 2. Backend API (Clojure)

#### Endpoint: POST /api/auth/change-password

**Handler:** `change-password-handler`

**Request:**
```clojure
{:current-password "KZM1bYZ2YVu7"
 :new-password "MyNewSecureP@ss123"
 :confirm-password "MyNewSecureP@ss123"}
```

**Response (Success):**
```clojure
{:success true
 :message "Senha alterada com sucesso"
 :token "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
```

**Response (Error):**
```clojure
{:success false
 :error "Senha atual incorreta"}
```


**Implementação:**

```clojure
(ns juridico.api.handlers.password
  (:require [buddy.hashers :as hashers]
            [juridico.api.db.repository :as repo]
            [juridico.api.auth.jwt :as jwt]
            [juridico.api.audit :as audit]
            [juridico.api.email :as email]))

(defn validate-password-strength
  "Valida força da senha"
  [password]
  (and (>= (count password) 8)
       (re-find #"[A-Z]" password)
       (re-find #"[a-z]" password)
       (re-find #"[0-9]" password)))

(defn change-password-handler
  [{:keys [db-repo jwt-payload body] :as request}]
  (let [{:keys [current-password new-password confirm-password]} body
        user-id (:user-id jwt-payload)
        user (repo/get-user-by-id db-repo user-id)]
    
    (cond
      ;; Validar senha atual
      (not (hashers/check current-password (:password-hash user)))
      {:status 401
       :body {:success false
              :error "Senha atual incorreta"}}
      
      ;; Validar força da nova senha
      (not (validate-password-strength new-password))
      {:status 400
       :body {:success false
              :error "Senha não atende aos requisitos de segurança"}}
      
      ;; Validar confirmação
      (not= new-password confirm-password)
      {:status 400
       :body {:success false
              :error "As senhas não coincidem"}}
      
      ;; Validar que nova senha é diferente
      (hashers/check new-password (:password-hash user))
      {:status 400
       :body {:success false
              :error "Nova senha deve ser diferente da atual"}}
      
      ;; Tudo válido - atualizar senha
      :else
      (let [new-hash (hashers/derive new-password {:alg :bcrypt+sha512 :iterations 12})
            updated-user (repo/update-user-password! 
                          db-repo 
                          user-id 
                          new-hash 
                          false) ;; temporary_password = false
            new-token (jwt/generate-token 
                       {:user-id user-id
                        :email (:email user)
                        :role (:role user)
                        :tenant-id (:tenant-id user)
                        :temporary-password false})]
        
        ;; Audit log
        (audit/log! db-repo
                    {:action "TEMPORARY_PASSWORD_CHANGED"
                     :user-id user-id
                     :tenant-id (:tenant-id user)
                     :ip-address (get-in request [:headers "x-forwarded-for"])
                     :details {:method "user-initiated"}})
        
        ;; Email de confirmação
        (email/send-password-changed-notification! 
         (:email user)
         {:timestamp (java.time.Instant/now)
          :ip-address (get-in request [:headers "x-forwarded-for"])})
        
        {:status 200
         :body {:success true
                :message "Senha alterada com sucesso"
                :token new-token}}))))
```

---

### 3. Next.js BFF - API Route

#### File: `frontend-nextjs/src/app/api/auth/change-password/route.ts`

```typescript
import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';
import { fetchBackend } from '@/lib/api';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const cookieStore = cookies();
    const accessToken = cookieStore.get('access_token')?.value;

    if (!accessToken) {
      return NextResponse.json(
        { success: false, error: 'Não autenticado' },
        { status: 401 }
      );
    }

    // Validação básica
    const { current_password, new_password, confirm_password } = body;

    if (!current_password || !new_password || !confirm_password) {
      return NextResponse.json(
        { success: false, error: 'Todos os campos são obrigatórios' },
        { status: 400 }
      );
    }

    if (new_password !== confirm_password) {
      return NextResponse.json(
        { success: false, error: 'As senhas não coincidem' },
        { status: 400 }
      );
    }

    // Proxy para backend
    const response = await fetchBackend('/api/auth/change-password', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        current_password,
        new_password,
        confirm_password,
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      return NextResponse.json(data, { status: response.status });
    }

    // Atualizar cookie com novo token
    if (data.token) {
      const cookieOptions = {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax' as const,
        maxAge: 60 * 60 * 24 * 7, // 7 dias
        path: '/',
      };

      cookieStore.set('access_token', data.token, cookieOptions);
    }

    return NextResponse.json(data);
  } catch (error) {
    console.error('[CHANGE PASSWORD ERROR]', error);
    return NextResponse.json(
      { success: false, error: 'Erro ao alterar senha' },
      { status: 500 }
    );
  }
}
```


---

### 4. Next.js Middleware

#### File: `frontend-nextjs/src/middleware.ts` (atualização)

```typescript
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';
import { verifyToken } from '@/lib/auth';

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Rotas públicas (não requerem autenticação)
  const publicRoutes = ['/login', '/super-admin/login', '/reset-password'];
  if (publicRoutes.some(route => pathname.startsWith(route))) {
    return NextResponse.next();
  }

  // Verificar token
  const token = request.cookies.get('access_token')?.value;
  
  if (!token) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  try {
    const payload = await verifyToken(token);

    // ⭐ NOVO: Verificar se precisa trocar senha
    if (payload.temporary_password === true || payload.requires_password_change === true) {
      // Permitir apenas rotas relacionadas a troca de senha
      const allowedRoutes = [
        '/change-password',
        '/api/auth/change-password',
        '/api/auth/logout'
      ];

      if (!allowedRoutes.some(route => pathname.startsWith(route))) {
        // Redirecionar para página de troca de senha
        return NextResponse.redirect(new URL('/change-password', request.url));
      }
    }

    // Continuar normalmente
    return NextResponse.next();
  } catch (error) {
    console.error('[MIDDLEWARE ERROR]', error);
    return NextResponse.redirect(new URL('/login', request.url));
  }
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico|public).*)',
  ],
};
```

---

### 5. Frontend Page - Change Password

#### File: `frontend-nextjs/src/app/change-password/page.tsx`

```typescript
'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { PasswordStrengthIndicator } from '@/components/PasswordStrengthIndicator';
import styles from './page.module.css';

export default function ChangePasswordPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    current_password: '',
    new_password: '',
    confirm_password: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false,
  });

  const validatePassword = (password: string): string[] => {
    const errors: string[] = [];
    if (password.length < 8) errors.push('Mínimo 8 caracteres');
    if (!/[A-Z]/.test(password)) errors.push('Uma letra maiúscula');
    if (!/[a-z]/.test(password)) errors.push('Uma letra minúscula');
    if (!/[0-9]/.test(password)) errors.push('Um número');
    return errors;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setLoading(true);

    // Validação client-side
    const newErrors: Record<string, string> = {};

    if (!formData.current_password) {
      newErrors.current_password = 'Senha atual é obrigatória';
    }

    const passwordErrors = validatePassword(formData.new_password);
    if (passwordErrors.length > 0) {
      newErrors.new_password = `Senha deve ter: ${passwordErrors.join(', ')}`;
    }

    if (formData.new_password !== formData.confirm_password) {
      newErrors.confirm_password = 'As senhas não coincidem';
    }

    if (formData.new_password === formData.current_password) {
      newErrors.new_password = 'Nova senha deve ser diferente da atual';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      setLoading(false);
      return;
    }

    try {
      const response = await fetch('/api/auth/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (!response.ok) {
        setErrors({ general: data.error || 'Erro ao alterar senha' });
        setLoading(false);
        return;
      }

      // Sucesso!
      setSuccess(true);
      setTimeout(() => {
        router.push('/dashboard');
      }, 2000);
    } catch (error) {
      setErrors({ general: 'Erro de conexão. Tente novamente.' });
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className={styles.successContainer}>
        <div className={styles.successIcon}>✓</div>
        <h1>Senha alterada com sucesso!</h1>
        <p>Redirecionando para o dashboard...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h1>Criar Nova Senha</h1>
          <p>Por segurança, você precisa criar uma nova senha</p>
        </div>

        <form onSubmit={handleSubmit} className={styles.form}>
          {/* Senha Atual */}
          <div className={styles.field}>
            <label htmlFor="current_password">
              Senha Atual (Temporária)
            </label>
            <div className={styles.passwordInput}>
              <input
                id="current_password"
                type={showPasswords.current ? 'text' : 'password'}
                value={formData.current_password}
                onChange={(e) => setFormData({ ...formData, current_password: e.target.value })}
                disabled={loading}
                aria-invalid={!!errors.current_password}
                aria-describedby={errors.current_password ? 'current-error' : undefined}
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, current: !showPasswords.current })}
                aria-label={showPasswords.current ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showPasswords.current ? '👁️' : '👁️‍🗨️'}
              </button>
            </div>
            {errors.current_password && (
              <span id="current-error" className={styles.error}>
                {errors.current_password}
              </span>
            )}
          </div>

          {/* Nova Senha */}
          <div className={styles.field}>
            <label htmlFor="new_password">Nova Senha</label>
            <div className={styles.passwordInput}>
              <input
                id="new_password"
                type={showPasswords.new ? 'text' : 'password'}
                value={formData.new_password}
                onChange={(e) => setFormData({ ...formData, new_password: e.target.value })}
                disabled={loading}
                aria-invalid={!!errors.new_password}
                aria-describedby={errors.new_password ? 'new-error' : undefined}
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, new: !showPasswords.new })}
                aria-label={showPasswords.new ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showPasswords.new ? '👁️' : '👁️‍🗨️'}
              </button>
            </div>
            <PasswordStrengthIndicator password={formData.new_password} />
            {errors.new_password && (
              <span id="new-error" className={styles.error}>
                {errors.new_password}
              </span>
            )}
          </div>

          {/* Confirmar Senha */}
          <div className={styles.field}>
            <label htmlFor="confirm_password">Confirmar Nova Senha</label>
            <div className={styles.passwordInput}>
              <input
                id="confirm_password"
                type={showPasswords.confirm ? 'text' : 'password'}
                value={formData.confirm_password}
                onChange={(e) => setFormData({ ...formData, confirm_password: e.target.value })}
                disabled={loading}
                aria-invalid={!!errors.confirm_password}
                aria-describedby={errors.confirm_password ? 'confirm-error' : undefined}
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, confirm: !showPasswords.confirm })}
                aria-label={showPasswords.confirm ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showPasswords.confirm ? '👁️' : '👁️‍🗨️'}
              </button>
            </div>
            {errors.confirm_password && (
              <span id="confirm-error" className={styles.error}>
                {errors.confirm_password}
              </span>
            )}
          </div>

          {errors.general && (
            <div className={styles.generalError}>
              {errors.general}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className={styles.submitButton}
          >
            {loading ? 'Alterando...' : 'Criar Nova Senha'}
          </button>
        </form>
      </div>
    </div>
  );
}
```


---

### 6. Password Strength Indicator Component

#### File: `frontend-nextjs/src/components/PasswordStrengthIndicator.tsx`

```typescript
'use client';

import { useMemo } from 'react';
import styles from './PasswordStrengthIndicator.module.css';

interface Props {
  password: string;
}

type StrengthLevel = 'weak' | 'medium' | 'strong' | 'very-strong';

export function PasswordStrengthIndicator({ password }: Props) {
  const strength = useMemo(() => {
    if (!password) return null;

    let score = 0;
    const checks = {
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /[0-9]/.test(password),
      special: /[^A-Za-z0-9]/.test(password),
    };

    // Calcular score
    if (checks.length) score++;
    if (checks.uppercase) score++;
    if (checks.lowercase) score++;
    if (checks.number) score++;
    if (checks.special) score++;
    if (password.length >= 12) score++;
    if (password.length >= 16) score++;

    // Determinar nível
    let level: StrengthLevel;
    let label: string;
    let color: string;

    if (score <= 2) {
      level = 'weak';
      label = 'Fraca';
      color = '#ef4444';
    } else if (score <= 4) {
      level = 'medium';
      label = 'Média';
      color = '#f59e0b';
    } else if (score <= 5) {
      level = 'strong';
      label = 'Forte';
      color = '#10b981';
    } else {
      level = 'very-strong';
      label = 'Muito Forte';
      color = '#059669';
    }

    return {
      level,
      label,
      color,
      score,
      maxScore: 7,
      checks,
    };
  }, [password]);

  if (!strength) return null;

  return (
    <div className={styles.container}>
      <div className={styles.bar}>
        <div
          className={styles.fill}
          style={{
            width: `${(strength.score / strength.maxScore) * 100}%`,
            backgroundColor: strength.color,
          }}
        />
      </div>
      <div className={styles.label} style={{ color: strength.color }}>
        {strength.label}
      </div>
      <div className={styles.requirements}>
        <div className={strength.checks.length ? styles.met : styles.unmet}>
          {strength.checks.length ? '✓' : '○'} Mínimo 8 caracteres
        </div>
        <div className={strength.checks.uppercase ? styles.met : styles.unmet}>
          {strength.checks.uppercase ? '✓' : '○'} Uma letra maiúscula
        </div>
        <div className={strength.checks.lowercase ? styles.met : styles.unmet}>
          {strength.checks.lowercase ? '✓' : '○'} Uma letra minúscula
        </div>
        <div className={strength.checks.number ? styles.met : styles.unmet}>
          {strength.checks.number ? '✓' : '○'} Um número
        </div>
      </div>
    </div>
  );
}
```

---

## Data Flow

### Fluxo Completo: Login com Senha Temporária → Troca → Dashboard

```
1. Usuário faz login com senha temporária
   ↓
2. Backend valida credenciais
   ↓
3. Backend verifica flag temporary_password = true
   ↓
4. Backend gera JWT com temporary_password: true
   ↓
5. Frontend recebe JWT e armazena em cookie
   ↓
6. Usuário tenta acessar /dashboard
   ↓
7. Middleware intercepta requisição
   ↓
8. Middleware verifica JWT.temporary_password = true
   ↓
9. Middleware redireciona para /change-password
   ↓
10. Usuário preenche formulário de troca
    ↓
11. Frontend valida dados (client-side)
    ↓
12. Frontend envia POST /api/auth/change-password
    ↓
13. BFF valida e faz proxy para backend
    ↓
14. Backend valida senha atual (bcrypt)
    ↓
15. Backend valida força da nova senha
    ↓
16. Backend faz hash da nova senha (bcrypt cost 12)
    ↓
17. Backend atualiza users.password_hash
    ↓
18. Backend atualiza users.temporary_password = false
    ↓
19. Backend cria registro em audit_log
    ↓
20. Backend envia email de confirmação
    ↓
21. Backend gera novo JWT (temporary_password: false)
    ↓
22. BFF recebe novo JWT e atualiza cookie
    ↓
23. Frontend exibe mensagem de sucesso
    ↓
24. Frontend redireciona para /dashboard
    ↓
25. Middleware permite acesso (temporary_password: false)
    ↓
26. Dashboard carrega normalmente ✓
```

---

## Security Considerations

### 1. Password Hashing

```clojure
;; Usar bcrypt com cost factor 12
(hashers/derive password {:alg :bcrypt+sha512 :iterations 12})
```

**Justificativa:**
- Bcrypt é resistente a ataques de força bruta
- Cost factor 12 é recomendado pela OWASP (2025)
- SHA512 adiciona camada extra de segurança

### 2. Password Validation

**Requisitos mínimos:**
- 8+ caracteres
- 1 letra maiúscula
- 1 letra minúscula
- 1 número

**Recomendado:**
- 12+ caracteres
- Incluir caracteres especiais

### 3. Rate Limiting

```clojure
;; Limitar tentativas de troca de senha
(def rate-limiter
  (atom {}))

(defn check-rate-limit [user-id]
  (let [attempts (get @rate-limiter user-id 0)
        last-attempt (get @rate-limiter (str user-id "-time") 0)
        now (System/currentTimeMillis)
        time-diff (- now last-attempt)]
    (if (and (>= attempts 3) (< time-diff 300000)) ;; 5 minutos
      false
      (do
        (swap! rate-limiter assoc user-id (inc attempts))
        (swap! rate-limiter assoc (str user-id "-time") now)
        true))))
```

### 4. JWT Security

**Novo JWT após troca:**
```clojure
{:user-id 123
 :email "user@example.com"
 :role "master"
 :tenant-id 456
 :temporary-password false  ;; ⭐ Atualizado
 :exp (+ (System/currentTimeMillis) (* 7 24 60 60 1000))} ;; 7 dias
```

### 5. Audit Trail

**Registros criados:**
1. `LOGIN_WITH_TEMPORARY_PASSWORD` - Quando usuário faz login
2. `PASSWORD_CHANGE_PAGE_ACCESSED` - Quando acessa página
3. `TEMPORARY_PASSWORD_CHANGED` - Quando troca com sucesso
4. `PASSWORD_CHANGE_FAILED` - Quando tentativa falha

---

## Error Handling

### Backend Errors

```clojure
(defn handle-change-password-error [error]
  (cond
    (instance? java.sql.SQLException error)
    {:status 500
     :body {:success false
            :error "Erro no banco de dados"}}
    
    (instance? IllegalArgumentException error)
    {:status 400
     :body {:success false
            :error (.getMessage error)}}
    
    :else
    {:status 500
     :body {:success false
            :error "Erro interno do servidor"}}))
```

### Frontend Errors

```typescript
const errorMessages: Record<string, string> = {
  'Senha atual incorreta': 'A senha temporária que você digitou está incorreta',
  'Senha não atende aos requisitos': 'Sua nova senha precisa ser mais forte',
  'As senhas não coincidem': 'As senhas digitadas não são iguais',
  'default': 'Erro ao alterar senha. Tente novamente',
};
```


---

## Testing Strategy

### 1. Unit Tests

#### Backend (Clojure)

```clojure
(ns juridico.api.handlers.password-test
  (:require [clojure.test :refer :all]
            [juridico.api.handlers.password :refer :all]))

(deftest validate-password-strength-test
  (testing "Senha válida"
    (is (true? (validate-password-strength "MyP@ssw0rd"))))
  
  (testing "Senha muito curta"
    (is (false? (validate-password-strength "Short1"))))
  
  (testing "Senha sem maiúscula"
    (is (false? (validate-password-strength "myp@ssw0rd"))))
  
  (testing "Senha sem número"
    (is (false? (validate-password-strength "MyPassword")))))

(deftest change-password-handler-test
  (testing "Senha atual incorreta"
    (let [request {:db-repo mock-repo
                   :jwt-payload {:user-id 1}
                   :body {:current-password "wrong"
                          :new-password "NewP@ss123"
                          :confirm-password "NewP@ss123"}}
          response (change-password-handler request)]
      (is (= 401 (:status response)))
      (is (= "Senha atual incorreta" (get-in response [:body :error])))))
  
  (testing "Nova senha fraca"
    (let [request {:db-repo mock-repo
                   :jwt-payload {:user-id 1}
                   :body {:current-password "correct"
                          :new-password "weak"
                          :confirm-password "weak"}}
          response (change-password-handler request)]
      (is (= 400 (:status response)))))
  
  (testing "Troca bem-sucedida"
    (let [request {:db-repo mock-repo
                   :jwt-payload {:user-id 1}
                   :body {:current-password "correct"
                          :new-password "NewP@ss123"
                          :confirm-password "NewP@ss123"}}
          response (change-password-handler request)]
      (is (= 200 (:status response)))
      (is (true? (get-in response [:body :success])))
      (is (some? (get-in response [:body :token]))))))
```

#### Frontend (TypeScript)

```typescript
import { validatePassword } from '@/lib/password';

describe('Password Validation', () => {
  it('should validate strong password', () => {
    const errors = validatePassword('MyP@ssw0rd123');
    expect(errors).toHaveLength(0);
  });

  it('should reject short password', () => {
    const errors = validatePassword('Short1');
    expect(errors).toContain('Mínimo 8 caracteres');
  });

  it('should reject password without uppercase', () => {
    const errors = validatePassword('mypassword123');
    expect(errors).toContain('Uma letra maiúscula');
  });

  it('should reject password without number', () => {
    const errors = validatePassword('MyPassword');
    expect(errors).toContain('Um número');
  });
});
```

### 2. Integration Tests

```typescript
describe('Change Password Flow', () => {
  it('should force password change on first login', async () => {
    // 1. Login com senha temporária
    const loginResponse = await fetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'TempPass123',
      }),
    });
    expect(loginResponse.ok).toBe(true);

    // 2. Tentar acessar dashboard
    const dashboardResponse = await fetch('/dashboard');
    
    // 3. Deve redirecionar para /change-password
    expect(dashboardResponse.url).toContain('/change-password');

    // 4. Trocar senha
    const changeResponse = await fetch('/api/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({
        current_password: 'TempPass123',
        new_password: 'MyNewP@ss123',
        confirm_password: 'MyNewP@ss123',
      }),
    });
    expect(changeResponse.ok).toBe(true);

    // 5. Acessar dashboard novamente
    const dashboardResponse2 = await fetch('/dashboard');
    expect(dashboardResponse2.ok).toBe(true);
  });
});
```

### 3. E2E Tests (Playwright)

```typescript
import { test, expect } from '@playwright/test';

test('Force password change flow', async ({ page }) => {
  // 1. Login com senha temporária
  await page.goto('/login');
  await page.fill('[name="email"]', 'test@example.com');
  await page.fill('[name="password"]', 'TempPass123');
  await page.click('button[type="submit"]');

  // 2. Deve redirecionar para /change-password
  await expect(page).toHaveURL(/\/change-password/);
  await expect(page.locator('h1')).toContainText('Criar Nova Senha');

  // 3. Preencher formulário
  await page.fill('[name="current_password"]', 'TempPass123');
  await page.fill('[name="new_password"]', 'MyNewP@ss123');
  await page.fill('[name="confirm_password"]', 'MyNewP@ss123');

  // 4. Verificar indicador de força
  await expect(page.locator('.password-strength')).toContainText('Forte');

  // 5. Submeter
  await page.click('button[type="submit"]');

  // 6. Deve exibir sucesso e redirecionar
  await expect(page.locator('.success-message')).toBeVisible();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 5000 });

  // 7. Dashboard deve carregar normalmente
  await expect(page.locator('h1')).toContainText('Dashboard');
});

test('Reject weak password', async ({ page }) => {
  await page.goto('/change-password');
  
  await page.fill('[name="current_password"]', 'TempPass123');
  await page.fill('[name="new_password"]', 'weak');
  await page.fill('[name="confirm_password"]', 'weak');

  await page.click('button[type="submit"]');

  // Deve exibir erro
  await expect(page.locator('.error')).toContainText('Senha deve ter');
});
```

---

## Performance Considerations

### 1. Password Hashing

**Bcrypt cost factor 12:**
- Tempo de hash: ~200-300ms
- Aceitável para operação única (troca de senha)
- Não impacta UX significativamente

### 2. Client-side Validation

**Debounce na validação:**
```typescript
const [password, setPassword] = useState('');
const [strength, setStrength] = useState(null);

useEffect(() => {
  const timer = setTimeout(() => {
    setStrength(calculateStrength(password));
  }, 300); // Aguardar 300ms após última digitação

  return () => clearTimeout(timer);
}, [password]);
```

### 3. Database Queries

**Índice para performance:**
```sql
CREATE INDEX idx_users_temporary_password 
ON users(temporary_password) 
WHERE temporary_password = true;
```

**Justificativa:**
- Consultas rápidas para usuários com senha temporária
- Índice parcial (apenas temporary_password = true)
- Reduz overhead em tabela grande

---

## Accessibility (WCAG 2.1 AA)

### 1. Keyboard Navigation

- Todos os campos acessíveis via Tab
- Enter submete formulário
- Escape fecha modais

### 2. Screen Readers

```tsx
<label htmlFor="new_password">
  Nova Senha
</label>
<input
  id="new_password"
  type="password"
  aria-invalid={!!errors.new_password}
  aria-describedby={errors.new_password ? 'new-error' : 'new-help'}
/>
{errors.new_password && (
  <span id="new-error" role="alert">
    {errors.new_password}
  </span>
)}
<span id="new-help" className="sr-only">
  Senha deve ter no mínimo 8 caracteres, incluindo maiúsculas, minúsculas e números
</span>
```

### 3. Color Contrast

- Texto: #1f2937 em fundo #ffffff (contraste 16:1)
- Erros: #ef4444 em fundo #ffffff (contraste 4.5:1)
- Links: #3b82f6 em fundo #ffffff (contraste 4.5:1)

### 4. Focus Indicators

```css
input:focus {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}
```

---

## Deployment Checklist

### Backend

- [ ] Adicionar coluna `temporary_password` na tabela `users`
- [ ] Criar índice `idx_users_temporary_password`
- [ ] Implementar handler `change-password-handler`
- [ ] Adicionar rota POST `/api/auth/change-password`
- [ ] Configurar rate limiting
- [ ] Testar em staging
- [ ] Deploy em produção

### Frontend

- [ ] Criar página `/change-password`
- [ ] Criar componente `PasswordStrengthIndicator`
- [ ] Atualizar middleware para verificar `temporary_password`
- [ ] Criar API route `/api/auth/change-password`
- [ ] Adicionar estilos CSS
- [ ] Testar em staging
- [ ] Deploy em produção

### Database

- [ ] Backup do banco antes da migration
- [ ] Executar migration em staging
- [ ] Validar migration
- [ ] Executar migration em produção
- [ ] Validar em produção

### Testing

- [ ] Executar unit tests
- [ ] Executar integration tests
- [ ] Executar E2E tests
- [ ] Teste manual completo
- [ ] Validar acessibilidade

---

## Rollback Plan

### Se algo der errado:

1. **Reverter deploy do frontend:**
   ```bash
   git revert <commit-hash>
   git push origin main
   ```

2. **Reverter deploy do backend:**
   ```bash
   git revert <commit-hash>
   git push origin main
   ```

3. **Reverter migration do banco (se necessário):**
   ```sql
   ALTER TABLE users DROP COLUMN temporary_password;
   DROP INDEX idx_users_temporary_password;
   ```

4. **Validar que sistema voltou ao normal:**
   - Login funciona
   - Dashboard carrega
   - Sem erros nos logs

---

## Monitoring and Alerts

### Métricas para monitorar:

1. **Taxa de sucesso de troca de senha:**
   - Meta: > 95%
   - Alerta se < 90%

2. **Tempo médio de troca:**
   - Meta: < 5 segundos
   - Alerta se > 10 segundos

3. **Tentativas falhadas:**
   - Alerta se > 10 falhas/hora

4. **Usuários com senha temporária:**
   - Monitorar quantidade
   - Alerta se crescer muito

### Logs importantes:

```clojure
(log/info "Password change attempt" 
          {:user-id user-id
           :success true
           :duration-ms duration})

(log/warn "Password change failed"
          {:user-id user-id
           :reason "weak-password"
           :attempts attempts})
```

---

## Documentation

### Para Usuários

**Email de boas-vindas (atualizado):**
```
Olá!

Sua conta foi criada com sucesso.

Email: user@example.com
Senha temporária: KZM1bYZ2YVu7

⚠️ IMPORTANTE: Por segurança, você será solicitado a criar uma nova senha no primeiro login.

Acesse: https://seudominio.com/login

Atenciosamente,
Equipe de Suporte
```

### Para Desenvolvedores

**README atualizado:**
```markdown
## Troca de Senha Temporária

Usuários criados com senha temporária são forçados a trocar a senha no primeiro login.

### Fluxo:
1. Login com senha temporária
2. Redirecionamento automático para /change-password
3. Usuário cria nova senha
4. Acesso liberado ao dashboard

### Implementação:
- Backend: `src/juridico/api/handlers/password.clj`
- Frontend: `frontend-nextjs/src/app/change-password/`
- Middleware: `frontend-nextjs/src/middleware.ts`
```

---

**Documento criado em:** 30 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Aguardando Aprovação

