# Implementação: Auto-Descoberta por Email

**Data:** 29/10/2025  
**Status:** Pronto para Implementar

---

## 📋 Overview

Mudança de arquitetura de **subdomínios** para **auto-descoberta por email único**.

### Antes
```
URL: https://escritorio-silva.meudominio.com/login
Login: email + senha + subdomain (extraído da URL)
```

### Depois
```
URL: https://meudominio.com/login (única para todos)
Login: email + senha (sistema descobre tenant automaticamente)
```

---

## 🔧 Mudanças Necessárias

### 1. Backend: Adicionar Constraint de Email Único

**Arquivo:** SQL Migration

```sql
-- Garantir que emails são únicos globalmente
ALTER TABLE users ADD CONSTRAINT unique_email UNIQUE (email);

-- Se já existirem emails duplicados, você precisará corrigi-los primeiro:
-- SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;
```

---

### 2. Backend: Criar Função de Busca Global

**Arquivo:** `src/juridico/api/db/postgres.clj`

**Adicionar função:**

```clojure
(defn encontrar-usuario-por-email-global
  "Busca usuário por email em TODOS os tenants"
  [db-repo email]
  (jdbc/execute-one! 
    (:datasource db-repo)
    ["SELECT u.*, t.name as tenant_name, t.active as tenant_active
      FROM users u
      JOIN tenants t ON u.tenant_id = t.id
      WHERE LOWER(u.email) = LOWER(?)"
     email]))
```

---

### 3. Backend: Criar Handler de Auto-Descoberta

**Arquivo:** `src/juridico/api/handlers.clj`

**Adicionar handler:**

```clojure
(defn login-auto-discover-handler
  "Login que descobre tenant automaticamente pelo email.
   Não requer subdomain - mais seguro e simples."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/simple-login-payload body-params)
    (let [{:keys [email password]} body-params]
      (if-let [user (p/encontrar-usuario-por-email-global db-repo email)]
        ;; Validar que tenant está ativo
        (if (:tenant_active user)
          ;; Validar senha
          (if (hashers/check password (:users/password_hash user))
            ;; Gerar token com tenant-id
            (let [claims {:user-id (:users/id user)
                         :email (:users/email user)
                         :role (:users/role user)
                         :tenant-id (:users/tenant_id user)
                         :exp (-> (java.time.Instant/now)
                                 (.plusSeconds 3600)
                                 (.getEpochSecond))}
                  token (jwt/sign claims config/jwt-secret)]
              {:status 200
               :body {:message (str "Usuário " email " autenticado com sucesso.")
                      :token token
                      :user {:email email
                             :role (:users/role user)
                             :tenant-id (:users/tenant_id user)
                             :tenant-name (:tenant_name user)}}})
            {:status 401 :body {:error "Credenciais inválidas."}})
          {:status 403 :body {:error "Escritório inativo."}})
        {:status 401 :body {:error "Credenciais inválidas."}}))
    {:status 400
     :body {:error "Dados de login inválidos."
            :details (s/explain-data :juridico.api.specs/simple-login-payload body-params)}}))
```

---

### 4. Backend: Adicionar Spec Simples

**Arquivo:** `src/juridico/api/specs.clj`

**Adicionar spec:**

```clojure
;; Spec para login simples (sem subdomain)
(s/def ::simple-login-payload (s/keys :req-un [::email ::password]))
```

---

### 5. Backend: Adicionar Rota

**Arquivo:** `src/juridico/api/core.clj`

**Modificar rotas:**

```clojure
;; Adicionar nova rota de login (auto-descoberta)
["/api"
 {:middleware [mw/wrap-public-db-repo]}
 ["/auth"
  ["/login" {:post {:handler h/login-auto-discover-handler}}]]]

;; Manter rota antiga para compatibilidade (opcional)
["/auth" 
 {:middleware [mw/wrap-public-db-repo mw/wrap-tenant-context]}
 ["/login" {:post {:handler h/login-handler}}]]
```

---

### 6. Frontend: Simplificar Página de Login

**Arquivo:** `frontend-nextjs/src/app/login/page.tsx`

**Substituir por:**

```typescript
'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import styles from './login.module.css';

export default function TenantLoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('/api/tenant/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        router.push('/dashboard');
      } else {
        const data = await response.json();
        setError(data.error || 'Erro ao fazer login');
      }
    } catch (err) {
      setError('Erro ao conectar ao servidor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.header}>
          <h1 className={styles.title}>Login</h1>
          <p className={styles.subtitle}>
            Acesse sua conta
          </p>
        </div>

        <form onSubmit={handleSubmit} className={styles.form}>
          {error && (
            <div className={styles.error}>
              ⚠️ {error}
            </div>
          )}

          <div className={styles.formGroup}>
            <label htmlFor="email" className={styles.label}>
              E-mail:
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
              className={styles.input}
              placeholder="seu@email.com"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password" className={styles.label}>
              Senha:
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
              className={styles.input}
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className={styles.button}
          >
            {loading ? 'Entrando...' : 'Entrar'}
          </button>

          {loading && (
            <p className={styles.loadingHint}>
              ⏳ Primeira vez pode demorar até 1 minuto (servidor iniciando)
            </p>
          )}
        </form>

        <div className={styles.footer}>
          <p>Sistema de Gerenciamento Jurídico</p>
        </div>
      </div>
    </div>
  );
}
```

---

### 7. Frontend: Simplificar API Route

**Arquivo:** `frontend-nextjs/src/app/api/tenant/login/route.ts`

**Substituir por:**

```typescript
import { NextRequest, NextResponse } from 'next/server';
import { setSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    console.log('[TENANT LOGIN] Tentativa de login:', body.email);

    if (!body.email || !body.password) {
      return NextResponse.json(
        { error: 'E-mail e senha são obrigatórios' },
        { status: 400 }
      );
    }

    // Fazer login no backend (auto-descoberta)
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:3000';
    console.log('[TENANT LOGIN] Fazendo requisição ao backend:', backendUrl);

    const response = await fetch(`${backendUrl}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: body.email,
        password: body.password,
      }),
    });

    console.log('[TENANT LOGIN] Resposta do backend:', response.status);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ 
        error: 'Credenciais inválidas' 
      }));
      console.log('[TENANT LOGIN] Erro do backend:', errorData);
      return NextResponse.json(
        { error: errorData.error || 'Credenciais inválidas' },
        { status: response.status }
      );
    }

    const data = await response.json();
    console.log('[TENANT LOGIN] Token recebido:', data.token ? 'SIM' : 'NÃO');

    if (!data.token) {
      return NextResponse.json(
        { error: 'Token não recebido do servidor' },
        { status: 500 }
      );
    }

    // Armazenar token em cookie HttpOnly
    await setSession(data.token);
    console.log('[TENANT LOGIN] Sessão criada com sucesso');

    return NextResponse.json({
      message: 'Login realizado com sucesso',
      user: data.user,
    });
  } catch (error) {
    console.error('[TENANT LOGIN] Erro:', error);
    
    if (error instanceof Error) {
      if (error.message.includes('ECONNREFUSED')) {
        return NextResponse.json(
          { error: 'Servidor indisponível' },
          { status: 503 }
        );
      }
    }

    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}
```

---

### 8. Frontend: Simplificar Middleware

**Arquivo:** `frontend-nextjs/src/middleware.ts`

**Substituir lógica de subdomínio por:**

```typescript
export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  console.log('[MIDDLEWARE] Requisição para:', pathname);

  // Rotas públicas do super admin
  const superAdminPublicPaths = ['/super-admin/login', '/api/auth/login'];
  
  if (superAdminPublicPaths.some(path => pathname.startsWith(path))) {
    console.log('[MIDDLEWARE] Rota pública do super admin');
    return NextResponse.next();
  }

  // Rotas públicas do tenant
  const tenantPublicPaths = ['/login', '/api/tenant/login'];
  
  if (tenantPublicPaths.some(path => pathname.startsWith(path))) {
    console.log('[MIDDLEWARE] Rota pública do tenant');
    return NextResponse.next();
  }

  // Rotas protegidas do super admin
  if (pathname.startsWith('/super-admin') || pathname.startsWith('/api/admin')) {
    const accessToken = request.cookies.get('access_token')?.value;

    if (!accessToken) {
      console.log('[MIDDLEWARE] Sem token, redirecionando para login super admin');
      if (pathname.startsWith('/api/')) {
        return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
      }
      return NextResponse.redirect(new URL('/super-admin/login', request.url));
    }

    try {
      const { payload } = await jwtVerify(accessToken, JWT_SECRET);
      const role = payload.role as string;
      
      if (role !== 'super-admin' && role !== 'superadmin') {
        console.error('[MIDDLEWARE] Role inválida:', role);
        if (pathname.startsWith('/api/')) {
          return NextResponse.json({ error: 'Acesso negado' }, { status: 403 });
        }
        return NextResponse.redirect(new URL('/super-admin/login', request.url));
      }

      console.log('[MIDDLEWARE] Autenticação super admin OK');
      return NextResponse.next();
    } catch (error) {
      console.error('[MIDDLEWARE] Erro ao verificar token:', error);
      if (pathname.startsWith('/api/')) {
        return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
      }
      return NextResponse.redirect(new URL('/super-admin/login', request.url));
    }
  }

  // Rotas protegidas do tenant
  const accessToken = request.cookies.get('access_token')?.value;

  if (!accessToken) {
    console.log('[MIDDLEWARE] Sem token, redirecionando para login tenant');
    if (pathname.startsWith('/api/')) {
      return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
    }
    return NextResponse.redirect(new URL('/login', request.url));
  }

  try {
    const { payload } = await jwtVerify(accessToken, JWT_SECRET);
    console.log('[MIDDLEWARE] Token verificado com sucesso');
    
    // Adicionar tenant info aos headers
    const response = NextResponse.next();
    response.headers.set('x-tenant-id', String(payload['tenant-id']));
    response.headers.set('x-user-id', String(payload['user-id']));
    response.headers.set('x-user-email', String(payload.email));
    response.headers.set('x-user-role', String(payload.role));
    
    return response;
  } catch (error) {
    console.error('[MIDDLEWARE] Erro ao verificar token:', error);
    if (pathname.startsWith('/api/')) {
      return NextResponse.json({ error: 'Não autenticado' }, { status: 401 });
    }
    return NextResponse.redirect(new URL('/login', request.url));
  }
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};
```

---

## ✅ Checklist de Implementação

- [ ] 1. Adicionar constraint de email único no banco
- [ ] 2. Adicionar função `encontrar-usuario-por-email-global` no postgres.clj
- [ ] 3. Adicionar spec `::simple-login-payload` no specs.clj
- [ ] 4. Adicionar handler `login-auto-discover-handler` no handlers.clj
- [ ] 5. Adicionar rota `/api/auth/login` no core.clj
- [ ] 6. Simplificar página de login (login/page.tsx)
- [ ] 7. Simplificar API route (api/tenant/login/route.ts)
- [ ] 8. Simplificar middleware (middleware.ts)
- [ ] 9. Testar localmente
- [ ] 10. Deploy e testar em produção

---

## 🧪 Como Testar

### 1. Criar Usuário de Teste

```sql
INSERT INTO users (email, password_hash, role, tenant_id)
VALUES (
  'teste@escritorio.com',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIiIiIiIiI', -- senha: test123
  'master',
  1
);
```

### 2. Testar Login

```
URL: http://localhost:3001/login
Email: teste@escritorio.com
Senha: test123
```

### 3. Validar

- ✅ Login bem-sucedido
- ✅ Redireciona para /dashboard
- ✅ Dashboard mostra nome do escritório correto
- ✅ Estatísticas carregam

---

## 📊 Benefícios

### Antes (Subdomínios)
- ❌ Requer DNS wildcard
- ❌ Não funciona no Render gratuito
- ❌ Complexo de configurar
- ❌ Expõe lista de tenants

### Depois (Auto-Descoberta)
- ✅ Funciona em qualquer hospedagem
- ✅ Funciona no Render gratuito
- ✅ Simples de configurar
- ✅ Mais seguro (não expõe tenants)
- ✅ Melhor UX (menos campos)
- ✅ Código mais limpo

---

**Documento criado em:** 29/10/2025  
**Status:** Pronto para Implementar
