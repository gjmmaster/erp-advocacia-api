# Design Document - Migração para Next.js BFF

## Overview

Este documento detalha o design técnico completo para migrar o frontend de Vite + React para Next.js 14 com App Router, implementando um BFF (Backend for Frontend) que gerencia autenticação via cookies HttpOnly server-side.

### Arquitetura Atual vs Nova

```
┌─────────────────────────────────────────────────────────────┐
│  ARQUITETURA ATUAL (Vite + React)                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Browser                                                    │
│  ├─ React Components                                        │
│  ├─ Axios (client-side)                                     │
│  ├─ localStorage (JWT) ← VULNERÁVEL                         │
│  └─ Direct calls to Backend                                 │
│                                                             │
│  Backend Clojure                                            │
│  └─ REST API                                                │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  NOVA ARQUITETURA (Next.js 14 + BFF)                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Browser                                                    │
│  ├─ React Server Components (RSC)                           │
│  ├─ React Client Components                                 │
│  ├─ Cookies HttpOnly ← SEGURO                               │
│  └─ Calls to BFF only                                       │
│                                                             │
│  Next.js BFF (Server-Side)                                  │
│  ├─ API Routes (/api/*)                                     │
│  ├─ Middleware (auth, security)                             │
│  ├─ Cookie Management                                       │
│  ├─ JWT Validation                                          │
│  └─ Proxy to Backend                                        │
│                                                             │
│  Backend Clojure                                            │
│  └─ REST API (unchanged)                                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Architecture

### Estrutura de Pastas Completa

```
nextjs-frontend/
├── .env.local                    # Variáveis de ambiente locais
├── .env.example                  # Template de variáveis
├── .gitignore
├── next.config.js                # Configuração do Next.js
├── package.json
├── tsconfig.json                 # Configuração TypeScript
├── .eslintrc.json               # Configuração ESLint
│
├── public/                       # Assets estáticos
│   └── favicon.ico
│
├── src/
│   ├── app/                      # App Router (Next.js 14)
│   │   ├── layout.tsx            # Layout raiz
│   │   ├── page.tsx              # Página inicial (redirect)
│   │   ├── globals.css           # Estilos globais
│   │   │
│   │   ├── super-admin/          # Rotas do Super Admin
│   │   │   ├── login/
│   │   │   │   └── page.tsx      # Página de login
│   │   │   │
│   │   │   └── dashboard/
│   │   │       ├── page.tsx      # Dashboard principal
│   │   │       └── layout.tsx    # Layout do dashboard
│   │   │
│   │   └── api/                  # BFF - API Routes
│   │       ├── auth/
│   │       │   ├── login/
│   │       │   │   └── route.ts  # POST /api/auth/login
│   │       │   ├── logout/
│   │       │   │   └── route.ts  # POST /api/auth/logout
│   │       │   └── refresh/
│   │       │       └── route.ts  # POST /api/auth/refresh
│   │       │
│   │       └── admin/
│   │           ├── tenants/
│   │           │   ├── route.ts  # GET/POST /api/admin/tenants
│   │           │   └── [id]/
│   │           │       └── route.ts # GET/PUT/DELETE /api/admin/tenants/[id]
│   │           │
│   │           └── provision/
│   │               └── route.ts  # POST /api/admin/provision
│   │
│   ├── components/               # Componentes React
│   │   ├── ui/                   # Componentes de UI
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   └── Modal.tsx
│   │   │
│   │   ├── super-admin/          # Componentes específicos
│   │   │   ├── CreateTenantModal.tsx
│   │   │   ├── EditTenantModal.tsx
│   │   │   └── TenantsTable.tsx
│   │   │
│   │   └── providers/            # Context Providers
│   │       └── AuthProvider.tsx  # Provider de autenticação
│   │
│   ├── lib/                      # Utilitários e configurações
│   │   ├── api/
│   │   │   ├── client.ts         # Cliente API para componentes
│   │   │   └── server.ts         # Cliente API para server-side
│   │   │
│   │   ├── auth/
│   │   │   ├── jwt.ts            # Utilitários JWT (jose)
│   │   │   ├── cookies.ts        # Gerenciamento de cookies
│   │   │   └── session.ts        # Gerenciamento de sessão
│   │   │
│   │   └── utils/
│   │       ├── errors.ts         # Tratamento de erros
│   │       └── logger.ts         # Sistema de logs
│   │
│   ├── middleware.ts             # Middleware global do Next.js
│   │
│   ├── types/                    # Definições TypeScript
│   │   ├── api.ts                # Tipos de API
│   │   ├── auth.ts               # Tipos de autenticação
│   │   └── tenant.ts             # Tipos de tenant
│   │
│   └── styles/                   # Estilos CSS
│       ├── Modal.css
│       └── Dashboard.css
│
└── __tests__/                    # Testes
    ├── api/
    │   ├── auth.test.ts
    │   └── admin.test.ts
    └── components/
        └── TenantsTable.test.tsx
```

---

## Components and Interfaces

### 1. Middleware de Autenticação Global

**Arquivo:** `src/middleware.ts`

**Responsabilidade:** Interceptar todas as requisições, validar autenticação e adicionar headers de segurança.

**Código Completo:**

```typescript
import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import { verifyJWT } from '@/lib/auth/jwt'

// Rotas que NÃO requerem autenticação
const PUBLIC_ROUTES = [
  '/super-admin/login',
  '/api/auth/login',
  '/api/auth/refresh',
]

// Rotas que requerem role de super-admin
const SUPER_ADMIN_ROUTES = [
  '/super-admin/dashboard',
  '/api/admin',
]

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  // Permitir acesso a rotas públicas
  if (PUBLIC_ROUTES.some(route => pathname.startsWith(route))) {
    return NextResponse.next()
  }

  // Extrair token do cookie
  const token = request.cookies.get('auth_token')?.value

  // Se não tem token e está tentando acessar rota protegida
  if (!token) {
    if (pathname.startsWith('/api/')) {
      return NextResponse.json(
        { error: 'Não autenticado' },
        { status: 401 }
      )
    }
    // Redirecionar para login
    return NextResponse.redirect(new URL('/super-admin/login', request.url))
  }

  // Validar JWT
  try {
    const payload = await verifyJWT(token)

    // Verificar se rota requer super-admin
    if (SUPER_ADMIN_ROUTES.some(route => pathname.startsWith(route))) {
      if (payload.role !== 'super-admin') {
        if (pathname.startsWith('/api/')) {
          return NextResponse.json(
            { error: 'Acesso negado' },
            { status: 403 }
          )
        }
        return NextResponse.redirect(new URL('/super-admin/login', request.url))
      }
    }

    // Adicionar headers de segurança
    const response = NextResponse.next()
    
    // Content Security Policy
    response.headers.set(
      'Content-Security-Policy',
      "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';"
    )
    
    // Outros headers de segurança
    response.headers.set('X-Frame-Options', 'DENY')
    response.headers.set('X-Content-Type-Options', 'nosniff')
    response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
    response.headers.set('Permissions-Policy', 'geolocation=(), microphone=(), camera=()')

    return response
  } catch (error) {
    // Token inválido ou expirado
    if (pathname.startsWith('/api/')) {
      return NextResponse.json(
        { error: 'Token inválido ou expirado' },
        { status: 401 }
      )
    }
    return NextResponse.redirect(new URL('/super-admin/login', request.url))
  }
}

// Configurar quais rotas o middleware deve processar
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
}
```

---

### 2. Utilitários JWT

**Arquivo:** `src/lib/auth/jwt.ts`

**Responsabilidade:** Validar e decodificar JWTs usando a biblioteca `jose`.

**Código Completo:**

```typescript
import { jwtVerify, SignJWT } from 'jose'

const JWT_SECRET = new TextEncoder().encode(
  process.env.JWT_SECRET || 'fallback-secret-for-development'
)

export interface JWTPayload {
  userId: number
  email: string
  role: string
  tenantId?: number
  exp: number
  iat: number
}

/**
 * Verifica e decodifica um JWT
 * @param token - JWT string
 * @returns Payload decodificado
 * @throws Error se token for inválido
 */
export async function verifyJWT(token: string): Promise<JWTPayload> {
  try {
    const { payload } = await jwtVerify(token, JWT_SECRET)
    return payload as unknown as JWTPayload
  } catch (error) {
    throw new Error('Token inválido ou expirado')
  }
}

/**
 * Cria um novo JWT (usado apenas se necessário no BFF)
 * @param payload - Dados para incluir no token
 * @returns JWT string
 */
export async function createJWT(payload: Omit<JWTPayload, 'exp' | 'iat'>): Promise<string> {
  const token = await new SignJWT(payload as any)
    .setProtectedHeader({ alg: 'HS256' })
    .setIssuedAt()
    .setExpirationTime('7d')
    .sign(JWT_SECRET)
  
  return token
}

/**
 * Decodifica JWT sem validar (útil para ler payload expirado)
 * @param token - JWT string
 * @returns Payload decodificado ou null
 */
export function decodeJWT(token: string): JWTPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    
    const payload = JSON.parse(
      Buffer.from(parts[1], 'base64').toString('utf-8')
    )
    return payload as JWTPayload
  } catch {
    return null
  }
}
```

---

### 3. Gerenciamento de Cookies

**Arquivo:** `src/lib/auth/cookies.ts`

**Responsabilidade:** Criar, ler e deletar cookies de autenticação com configurações seguras.

**Código Completo:**

```typescript
import { cookies } from 'next/headers'
import { ResponseCookie } from 'next/dist/compiled/@edge-runtime/cookies'

const COOKIE_NAME = 'auth_token'
const COOKIE_MAX_AGE = 7 * 24 * 60 * 60 // 7 dias em segundos

/**
 * Configuração padrão de cookies seguros
 */
const getCookieOptions = (): Partial<ResponseCookie> => ({
  httpOnly: true,
  secure: process.env.NODE_ENV === 'production',
  sameSite: 'lax',
  maxAge: COOKIE_MAX_AGE,
  path: '/',
})

/**
 * Define o cookie de autenticação
 * @param token - JWT string
 */
export function setAuthCookie(token: string): void {
  cookies().set(COOKIE_NAME, token, getCookieOptions())
}

/**
 * Obtém o cookie de autenticação
 * @returns JWT string ou undefined
 */
export function getAuthCookie(): string | undefined {
  return cookies().get(COOKIE_NAME)?.value
}

/**
 * Remove o cookie de autenticação
 */
export function deleteAuthCookie(): void {
  cookies().delete(COOKIE_NAME)
}

/**
 * Verifica se o cookie de autenticação existe
 * @returns boolean
 */
export function hasAuthCookie(): boolean {
  return cookies().has(COOKIE_NAME)
}
```

---

### 4. API Route - Login

**Arquivo:** `src/app/api/auth/login/route.ts`

**Responsabilidade:** Receber credenciais, validar no backend Clojure e definir cookie HttpOnly.

**Código Completo:**

```typescript
import { NextRequest, NextResponse } from 'next/server'
import axios from 'axios'
import { setAuthCookie } from '@/lib/auth/cookies'
import { logger } from '@/lib/utils/logger'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:3000'

export async function POST(request: NextRequest) {
  try {
    // Extrair credenciais do body
    const body = await request.json()
    const { email, password } = body

    // Validar campos obrigatórios
    if (!email || !password) {
      return NextResponse.json(
        { error: 'Email e senha são obrigatórios' },
        { status: 400 }
      )
    }

    // Fazer requisição ao backend Clojure
    const response = await axios.post(
      `${BACKEND_URL}/admin/login`,
      { email, password },
      {
        headers: {
          'Content-Type': 'application/json',
        },
        validateStatus: (status) => status < 500, // Não lançar erro para 4xx
      }
    )

    // Se login falhou no backend
    if (response.status !== 200) {
      logger.warn('Login failed', { email, status: response.status })
      return NextResponse.json(
        { error: response.data.error || 'Credenciais inválidas' },
        { status: response.status }
      )
    }

    // Extrair token do backend
    const { token, message } = response.data

    if (!token) {
      logger.error('Backend did not return token', { email })
      return NextResponse.json(
        { error: 'Erro ao processar autenticação' },
        { status: 500 }
      )
    }

    // Definir cookie HttpOnly com o token
    setAuthCookie(token)

    logger.info('Login successful', { email })

    // Retornar sucesso (sem o token no body!)
    return NextResponse.json({
      success: true,
      message: message || 'Login realizado com sucesso',
    })
  } catch (error) {
    logger.error('Login error', { error })
    
    // Não expor detalhes do erro ao cliente
    return NextResponse.json(
      { error: 'Erro ao processar login. Tente novamente.' },
      { status: 500 }
    )
  }
}
```

---

### 5. API Route - Logout

**Arquivo:** `src/app/api/auth/logout/route.ts`

**Responsabilidade:** Limpar cookie de autenticação.

**Código Completo:**

```typescript
import { NextResponse } from 'next/server'
import { deleteAuthCookie } from '@/lib/auth/cookies'
import { logger } from '@/lib/utils/logger'

export async function POST() {
  try {
    // Remover cookie
    deleteAuthCookie()

    logger.info('Logout successful')

    return NextResponse.json({
      success: true,
      message: 'Logout realizado com sucesso',
    })
  } catch (error) {
    logger.error('Logout error', { error })
    
    return NextResponse.json(
      { error: 'Erro ao processar logout' },
      { status: 500 }
    )
  }
}
```

---

Devido ao limite de tamanho, vou continuar o design em um segundo arquivo:
