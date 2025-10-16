# Frontend Next.js BFF - Sistema Jurídico

Sistema de gerenciamento de tenants com autenticação segura usando Next.js 14 e TypeScript.

## 🎯 Visão Geral

Este é um **Backend for Frontend (BFF)** que atua como camada intermediária entre o navegador e o backend Clojure, proporcionando:

- ✅ Autenticação segura com cookies HttpOnly
- ✅ Renovação automática de tokens
- ✅ Proteção de rotas via middleware
- ✅ Gerenciamento completo de tenants (CRUD)
- ✅ Interface responsiva e moderna

## 🚀 Quick Start

```bash
# Instalar dependências
npm install

# Configurar ambiente
cp .env.example .env.local

# Iniciar desenvolvimento
npm run dev
```

Acesse: `http://localhost:3001/super-admin/login`

## 📚 Documentação

- **[QUICK_START.md](./QUICK_START.md)** - Início rápido
- **[MIGRATION_COMPLETE.md](./MIGRATION_COMPLETE.md)** - Visão geral completa
- **[IMPLEMENTATION_STATUS.md](./IMPLEMENTATION_STATUS.md)** - Status detalhado
- **[MANUAL_TESTING_GUIDE.md](./MANUAL_TESTING_GUIDE.md)** - Guia de testes
- **[.kiro/specs/nextjs-bff-migration/](./.kiro/specs/nextjs-bff-migration/)** - Especificações técnicas

## 🏗️ Arquitetura

```
Browser → Next.js BFF → Backend Clojure
         ↑ (Cookies HttpOnly)
         ↑ (Middleware de Auth)
         ↑ (API Routes)
```

## 📦 Tecnologias

- **Next.js 14** - Framework React com App Router
- **TypeScript** - Tipagem estática
- **React 18** - Biblioteca UI
- **JWT** - Autenticação
- **Jest** - Testes unitários

## 🔒 Segurança

- Cookies HttpOnly (tokens inacessíveis via JavaScript)
- Cookies Secure (apenas HTTPS em produção)
- SameSite: Strict (proteção CSRF)
- Middleware de proteção de rotas
- Renovação automática de tokens

## 📊 Status

**Progresso:** 71% (5 de 7 fases completas)

- ✅ Fase 1: Setup
- ✅ Fase 2: Componentes
- ✅ Fase 3: BFF (API Routes)
- ✅ Fase 4: Autenticação
- ✅ Fase 5: Testes
- ⏳ Fase 6: Deploy
- ⏳ Fase 7: Cleanup

## 🧪 Testes

```bash
# Testes unitários
npm test

# Testes em modo watch
npm run test:watch

# Cobertura de código
npm run test:coverage
```

## 📝 Licença

Propriedade privada - Todos os direitos reservados
