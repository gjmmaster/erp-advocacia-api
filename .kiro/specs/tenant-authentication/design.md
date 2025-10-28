# Design Document - Autenticação e Dashboard de Tenants

**Data:** 24 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Em Revisão

---

## Overview

Este documento detalha o design técnico para implementação do sistema de autenticação e dashboard para tenants (escritórios de advocacia). A solução utiliza identificação por subdomínio, autenticação JWT com cookies HttpOnly, e arquitetura BFF (Backend for Frontend) com Next.js 14.

---

## Architecture

### Fluxo Geral

```
1. Usuário acessa: escritorio-silva.seudominio.com
   ↓
2. Next.js identifica tenant pelo subdomínio
   ↓
3. Usuário faz login com e-mail e senha
   ↓
4. Backend valida credenciais e tenant
   ↓
5. Backend retorna JWT com tenant-id
   ↓
6. Next.js BFF armazena JWT em cookie HttpOnly
   ↓
7. Usuário acessa dashboard
   ↓
8. Middleware valida JWT e tenant-id
   ↓
9. Dashboard carrega dados do tenant
```

### Arquitetura de Componentes

```
┌─────────────────────────────────────────────────────────────┐
│  Browser (escritorio-silva.seudominio.com)                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Next.js Frontend                                    │   │
│  │  ├─ /[subdomain]/login                              │   │
│  │  ├─ /[subdomain]/dashboard                          │   │
│  │  └─ Middleware (valida tenant + auth)               │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Next.js BFF (API Routes)                           │   │
│  │  ├─ /api/tenant/login                               │   │
│  │  ├─ /api/tenant/logout                              │   │
│  │  ├─ /api/tenant/dashboard                           │   │
│  │  └─ Cookie Management (HttpOnly)                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                          ↓                                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Backend Clojure                                            │
├─────────────────────────────────────────────────────────────┤
│  ├─ POST /auth/login (valida tenant + credenciais)         │
│  ├─ GET /api/tenants/:subdomain (busca tenant)             │
│  ├─ GET /api/dashboard/stats (métricas do tenant)          │
│  └─ Middleware JWT + Tenant Validation                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  PostgreSQL (CockroachDB)                                   │
├─────────────────────────────────────────────────────────────┤
│  ├─ tenants (id, name, subdomain, active)                  │
│  ├─ users (id, tenant_id, email, password_hash, role)      │
│  └─ legal_cases (id, tenant_id, ...)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Components and Interfaces

### 1. Identificação de Tenant por Subdomínio

#### 1.1. Next.js Middleware

**Arquivo:** `frontend-nextjs/src/middleware.ts`

**Responsabilidade:** Extrair subdomínio e validar tenant antes de processar requisição.

**Implementação:**

```typescript
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export async function middleware(request: NextRequest) {
  const { hostname, pathname } = request.nextUrl;
  
  // Extrair subdomínio
  const subdomain = extractSubdomain(hostname);
  
  // Se não tem subdomínio, redireciona para super-admin ou página de seleção
  if (!subdomain) {
    if (pathname.startsWith('/super-admin')) {
      return NextResponse.next();
    }
    return NextResponse.redirect(new URL('/super-admin/login', request.url));
  }
  
  // Validar se tenant existe (cache ou API)
  const tenant = await validateTenant(subdomain);
  
  if (!tenant) {
    return NextResponse.json(
      { error: 'Escritório não encontrado' },
      { status: 404 }
    );
  }
  
  if (!tenant.active) {
    return NextResponse.json(
      { error: 'Escritório desativado' },
      { status: 403 }
    );
  }
  
  // Adicionar tenant-id ao header para uso posterior
  const response = NextResponse.next();
  response.headers.set('x-tenant-id', tenant.id.toString());
  response.headers.set('x-tenant-subdomain', subdomain);
  
  return response;
}

function extractSubdomain(hostname: string): string | null {
  // Remove porta se existir
  const host = hostname.split(':')[0];
  
  // Exemplo: escritorio-silva.seudominio.com → escritorio-silva
  const parts = host.split('.');
  
  // Se tem 3+ partes, o primeiro é o subdomínio
  if (parts.length >= 3) {
    return parts[0];
  }
  
  // Localhost ou domínio sem subdomínio
  return null;
}

async function validateTenant(subdomain: string): Promise<Tenant | null> {
  try {
    // Buscar tenant no backend
    const response = await fetch(
      `${process.env.BACKEND_URL}/api/tenants/by-subdomain/${subdomain}`
    );
    
    if (!response.ok) {
      return null;
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error validating tenant:', error);
    return null;
  }
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};
```

---

### 2. Backend - Endpoint de Busca de Tenant

#### 2.1. Novo Endpoint

**Arquivo:** `src/juridico/api/handlers.clj`

**Endpoint:** `GET /api/tenants/by-subdomain/:subdomain`

**Implementação:**

```clojure
(defn get-tenant-by-subdomain-handler [request]
  (let [subdomain (get-in request [:path-params :subdomain])
        db-repo (:db-repo request)]
    (if-let [tenant (db/get-tenant-by-subdomain db-repo subdomain)]
      {:status 200
       :body tenant}
      {:status 404
       :body {:error "Tenant not found"}})))
```

#### 2.2. Função no Repository

**Arquivo:** `src/juridico/api/db/postgres.clj`

```clojure
(defn get-tenant-by-subdomain [db-repo subdomain]
  (jdbc/execute-one!
    (:datasource db-repo)
    ["SELECT id, name, subdomain, active, operator_limit, created_at
      FROM tenants
      WHERE subdomain = ? AND active = true"
     subdomain]))
```

---

### 3. Login de Tenant

#### 3.1. Frontend - Página de Login

**Arquivo:** `frontend-nextjs/src/app/[subdomain]/login/page.tsx`

**Estrutura:**

```typescript
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './login.module.css';

export default function TenantLoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('/api/tenant/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Erro ao fazer login');
      }

      // Redirecionar para dashboard
      router.push('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro desconhecido');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.loginBox}>
        <h1>Login</h1>
        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          
          <div className={styles.formGroup}>
            <label htmlFor="password">Senha</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          {error && <div className={styles.error}>{error}</div>}

          <button type="submit" disabled={loading}>
            {loading ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  );
}
```

#### 3.2. BFF - API Route de Login

**Arquivo:** `frontend-nextjs/src/app/api/tenant/login/route.ts`

```typescript
import { NextRequest, NextResponse } from 'next/server';
import { fetchBackend } from '@/lib/api';
import { setSession } from '@/lib/auth';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { email, password } = body;

    // Extrair subdomain do header (adicionado pelo middleware)
    const subdomain = request.headers.get('x-tenant-subdomain');
    const tenantId = request.headers.get('x-tenant-id');

    if (!subdomain || !tenantId) {
      return NextResponse.json(
        { error: 'Tenant não identificado' },
        { status: 400 }
      );
    }

    // Fazer login no backend
    const response = await fetchBackend('/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        email,
        password,
        subdomain, // Backend valida que usuário pertence a este tenant
      }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      return NextResponse.json(
        { error: errorData.error || 'Credenciais inválidas' },
        { status: response.status }
      );
    }

    const data = await response.json();

    // Validar que o tenant-id do token corresponde ao subdomínio
    const tokenPayload = JSON.parse(
      Buffer.from(data.token.split('.')[1], 'base64').toString()
    );

    if (tokenPayload['tenant-id'] !== parseInt(tenantId)) {
      return NextResponse.json(
        { error: 'Usuário não pertence a este escritório' },
        { status: 403 }
      );
    }

    // Armazenar token em cookie HttpOnly
    await setSession(data.token);

    return NextResponse.json({
      message: 'Login realizado com sucesso',
      user: {
        email: tokenPayload.email,
        role: tokenPayload.role,
      },
    });
  } catch (error) {
    console.error('[TENANT LOGIN] Error:', error);
    return NextResponse.json(
      { error: 'Erro interno do servidor' },
      { status: 500 }
    );
  }
}
```

#### 3.3. Backend - Atualizar Login Handler

**Arquivo:** `src/juridico/api/handlers.clj`

**Modificação:** Adicionar validação de subdomain

```clojure
(defn login-handler [request]
  (let [{:keys [email password subdomain]} (:body request)
        db-repo (:db-repo request)]
    
    ;; Buscar tenant pelo subdomain
    (if-let [tenant (db/get-tenant-by-subdomain db-repo subdomain)]
      
      ;; Buscar usuário
      (if-let [user (db/get-user-by-email db-repo email)]
        
        ;; Validar que usuário pertence ao tenant
        (if (= (:tenant-id user) (:id tenant))
          
          ;; Validar senha
          (if (hashers/check password (:password-hash user))
            
            ;; Gerar JWT
            (let [token (jwt/sign
                          {:user-id (:id user)
                           :email (:email user)
                           :role (:role user)
                           :tenant-id (:tenant-id user)
                           :exp (+ (System/currentTimeMillis) (* 15 60 1000))}
                          jwt-secret)]
              {:status 200
               :body {:token token
                      :message "Login successful"}})
            
            ;; Senha incorreta
            {:status 401
             :body {:error "Invalid credentials"}})
          
          ;; Usuário não pertence ao tenant
          {:status 403
           :body {:error "User does not belong to this tenant"}})
        
        ;; Usuário não encontrado
        {:status 401
         :body {:error "Invalid credentials"}})
      
      ;; Tenant não encontrado
      {:status 404
       :body {:error "Tenant not found"}})))
```

---

### 4. Dashboard do Tenant

#### 4.1. Frontend - Página de Dashboard

**Arquivo:** `frontend-nextjs/src/app/[subdomain]/dashboard/page.tsx`

```typescript
import { getSession } from '@/lib/auth';
import { redirect } from 'next/navigation';
import DashboardStats from '@/components/tenant/DashboardStats';
import DashboardLayout from '@/components/tenant/DashboardLayout';

export default async function TenantDashboardPage() {
  const session = await getSession();

  if (!session) {
    redirect('/login');
  }

  return (
    <DashboardLayout user={session}>
      <h1>Dashboard</h1>
      <DashboardStats tenantId={session['tenant-id']} />
    </DashboardLayout>
  );
}
```

#### 4.2. Componente de Estatísticas

**Arquivo:** `frontend-nextjs/src/components/tenant/DashboardStats.tsx`

```typescript
'use client';

import { useEffect, useState } from 'react';
import styles from './DashboardStats.module.css';

interface Stats {
  totalProcessos: number;
  totalClientes: number;
  totalOperadores: number;
  processosAtivos: number;
}

export default function DashboardStats({ tenantId }: { tenantId: number }) {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await fetch('/api/tenant/dashboard/stats');
      const data = await response.json();
      setStats(data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div>Carregando...</div>;
  }

  return (
    <div className={styles.statsGrid}>
      <div className={styles.statCard}>
        <h3>Total de Processos</h3>
        <p className={styles.statValue}>{stats?.totalProcessos || 0}</p>
      </div>
      
      <div className={styles.statCard}>
        <h3>Processos Ativos</h3>
        <p className={styles.statValue}>{stats?.processosAtivos || 0}</p>
      </div>
      
      <div className={styles.statCard}>
        <h3>Total de Clientes</h3>
        <p className={styles.statValue}>{stats?.totalClientes || 0}</p>
      </div>
      
      <div className={styles.statCard}>
        <h3>Total de Operadores</h3>
        <p className={styles.statValue}>{stats?.totalOperadores || 0}</p>
      </div>
    </div>
  );
}
```

#### 4.3. BFF - API Route de Stats

**Arquivo:** `frontend-nextjs/src/app/api/tenant/dashboard/stats/route.ts`

```typescript
import { NextRequest, NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';
import { fetchBackend } from '@/lib/api';

export async function GET(request: NextRequest) {
  try {
    const session = await getSession();

    if (!session) {
      return NextResponse.json(
        { error: 'Não autenticado' },
        { status: 401 }
      );
    }

    const tenantId = session['tenant-id'];

    // Buscar stats no backend
    const response = await fetchBackend(`/api/dashboard/stats/${tenantId}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${request.cookies.get('access_token')?.value}`,
      },
    });

    if (!response.ok) {
      throw new Error('Failed to fetch stats');
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('[DASHBOARD STATS] Error:', error);
    return NextResponse.json(
      { error: 'Erro ao buscar estatísticas' },
      { status: 500 }
    );
  }
}
```

#### 4.4. Backend - Endpoint de Stats

**Arquivo:** `src/juridico/api/handlers.clj`

```clojure
(defn get-dashboard-stats-handler [request]
  (let [tenant-id (get-in request [:path-params :tenant-id])
        db-repo (:db-repo request)
        identity (:identity request)]
    
    ;; Validar que usuário pertence ao tenant
    (if (= (:tenant-id identity) (Long/parseLong tenant-id))
      
      (let [stats {:total-processos (db/count-processos db-repo tenant-id)
                   :total-clientes (db/count-clientes db-repo tenant-id)
                   :total-operadores (db/count-operadores db-repo tenant-id)
                   :processos-ativos (db/count-processos-ativos db-repo tenant-id)}]
        {:status 200
         :body stats})
      
      {:status 403
       :body {:error "Access denied"}})))
```

---

## Data Models

### Tenant

```typescript
interface Tenant {
  id: number;
  name: string;
  subdomain: string;
  active: boolean;
  operatorLimit: number;
  createdAt: string;
}
```

### User (Session)

```typescript
interface UserSession {
  'user-id': number;
  email: string;
  role: 'master' | 'operador';
  'tenant-id': number;
  exp: number;
}
```

### Dashboard Stats

```typescript
interface DashboardStats {
  totalProcessos: number;
  totalClientes: number;
  totalOperadores: number;
  processosAtivos: number;
}
```

---

## Error Handling

### Frontend

```typescript
// Tratamento de erros padronizado
try {
  const response = await fetch('/api/...');
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Erro desconhecido');
  }
  
  return await response.json();
} catch (error) {
  console.error('Error:', error);
  // Exibir mensagem para usuário
  setError(error instanceof Error ? error.message : 'Erro desconhecido');
}
```

### Backend

```clojure
;; Middleware global de erro já existe
;; Retorna sempre:
{:status 500
 :body {:error "Internal server error"}}
```

---

## Testing Strategy

### Unit Tests

1. **Middleware de Subdomínio**
   - Teste: Extração correta de subdomínio
   - Teste: Validação de tenant existente
   - Teste: Validação de tenant inativo

2. **Login**
   - Teste: Login com credenciais válidas
   - Teste: Login com credenciais inválidas
   - Teste: Login de usuário de outro tenant
   - Teste: Validação de tenant-id no token

3. **Dashboard**
   - Teste: Carregamento de stats
   - Teste: Validação de permissões
   - Teste: Tratamento de erros

### Integration Tests

1. Fluxo completo de login
2. Acesso ao dashboard após login
3. Logout e redirecionamento
4. Proteção de rotas

---

## Security Considerations

1. **Tokens JWT:**
   - Armazenados em cookies HttpOnly
   - Expiração de 15 minutos
   - Validação de tenant-id em cada requisição

2. **Validação de Tenant:**
   - Middleware valida tenant antes de processar requisição
   - Backend valida que usuário pertence ao tenant correto
   - Token JWT contém tenant-id para validação

3. **Rate Limiting:**
   - Aplicado ao endpoint de login
   - 5 tentativas por 15 minutos por IP

4. **CORS:**
   - Configurado para aceitar apenas subdomínios do domínio principal

---

## Performance Optimizations

1. **Cache de Tenants:**
   - Implementar cache em memória para validação de subdomínios
   - TTL de 5 minutos
   - Invalidar ao atualizar tenant

2. **Lazy Loading:**
   - Componentes do dashboard carregados sob demanda
   - Stats carregadas após renderização inicial

3. **Server-Side Rendering:**
   - Páginas renderizadas no servidor quando possível
   - Reduz tempo de carregamento inicial

---

## Deployment Considerations

### Environment Variables

```bash
# Frontend Next.js
NEXT_PUBLIC_DOMAIN=seudominio.com
BACKEND_URL=https://api.seudominio.com
JWT_SECRET=<mesmo-secret-do-backend>

# Backend Clojure
JWT_SECRET=<secret-forte>
DATABASE_URL=<cockroachdb-url>
```

### DNS Configuration

```
# Wildcard subdomain
*.seudominio.com → Next.js Frontend
```

---

**Documento criado em:** 24/10/2025  
**Última atualização:** 24/10/2025  
**Status:** Aguardando Revisão
