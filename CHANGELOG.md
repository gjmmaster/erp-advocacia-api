# Changelog - Legal ERP

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [1.0.0] - 2025-11-03 - VERSÃO ESTÁVEL 🎉

### 🎨 Design System Completo

#### Adicionado
- Design system moderno baseado no Perplexity Design System
- 17 componentes UI reutilizáveis (Button, Card, Input, StatusBadge, etc.)
- Sistema de tokens CSS completo (cores, espaçamento, tipografia, sombras)
- Suporte a dark mode automático via `prefers-color-scheme`
- Responsividade total (mobile < 768px, tablet 768-1023px, desktop ≥ 1024px)
- Glassmorphism e gradientes modernos
- Animações suaves com GPU acceleration
- Font Awesome 6.4.0 integrado

#### Componentes Criados
- **UI Base**: Button, Card, Input, StatusBadge
- **Layout**: LoginScreen, Sidebar, DashboardHeader, DashboardLayout
- **Específicos**: StatCard, TenantsTableModern, ImpersonationBanner

#### Páginas Migradas
- Super Admin Login - Design moderno com partículas animadas
- Super Admin Dashboard - Layout completo com sidebar e cards
- Tenant Dashboard - Mesmo design moderno do Super Admin

### 🔐 Autenticação e Segurança

#### Adicionado
- Sistema de autenticação multi-tenant completo
- JWT tokens (access + refresh) com rotação automática
- Sistema de impersonation para Super Admin
- Force password change para novos usuários
- Rate limiting (100 req/min por IP)
- Audit logs para ações críticas
- Middleware de autenticação robusto
- Proteção contra CSRF

#### Funcionalidades
- Login Super Admin
- Login Tenant
- Logout com limpeza de sessão
- Refresh token automático
- Impersonation com audit trail
- Stop impersonation
- Change password obrigatório
- Session management

### 👥 Gestão de Tenants

#### Adicionado
- CRUD completo de tenants
- Provisionamento automático de tenant + usuário master
- Listagem com filtros e busca
- Edição de informações do tenant
- Deleção com confirmação (soft delete)
- Limite de operadores por tenant
- Subdomínio único por tenant
- Email de boas-vindas (preparado)

#### Funcionalidades
- Criar novo tenant
- Editar tenant existente
- Deletar tenant (com confirmação)
- Listar todos os tenants
- Filtrar por status (ativo/inativo)
- Acessar como tenant (impersonation)
- Ver usuário master do tenant

### 🏗️ Infraestrutura

#### Adicionado
- Backend Clojure com Leiningen
- Frontend Next.js 14 (App Router)
- Banco de dados CockroachDB (cloud)
- Deploy automático no Render
- Docker multi-stage build
- CI/CD via GitHub Actions
- Variáveis de ambiente seguras
- Health checks

#### Tecnologias
- **Backend**: Clojure 1.11, Ring, Reitit, next.jdbc
- **Frontend**: Next.js 14, React 18, TypeScript
- **Database**: CockroachDB (PostgreSQL compatible)
- **Deploy**: Render (free tier)
- **Storage**: Filesystem (preparado para S3)

### 📚 Documentação

#### Adicionado
- `DESIGN_SYSTEM.md` - Guia completo do design system
- `GUIA_MIGRACAO_DESIGN.md` - Como migrar páginas existentes
- `COMO_ATIVAR_NOVO_DESIGN.md` - Instruções de ativação
- `PROXIMOS_PASSOS_PROJETO.md` - Roadmap de 6 meses
- `SUCESSO_NOVO_DESIGN_DEPLOY.md` - Resumo do deploy
- `NOVO_DESIGN_IMPLEMENTADO.md` - Resumo da implementação
- Documentação inline em todos os componentes
- README atualizado com instruções completas

### 🐛 Correções

#### Corrigido
- Erro de build: `openjdk:11-jre-slim` não encontrado → `eclipse-temurin:11-jre-jammy`
- Erro de TypeScript: propriedades `is_active` e `email` faltando no tipo `Tenant`
- Erro de import: `dashboard-modern.module.css` → `dashboard.module.css`
- Export incorreto do `ImpersonationBanner`
- Verificação segura de `is_active` (usar `!== false` em vez de truthy check)

### 🔄 Alterações

#### Modificado
- Migrado Super Admin Dashboard para novo design
- Migrado Tenant Dashboard para novo design
- Migrado Login screens para novo design
- Atualizado `globals.css` com design system completo
- Melhorado responsividade em todos os componentes
- Otimizado performance com CSS Modules

### 📊 Estatísticas

- **Commits**: 10+ commits nesta versão
- **Arquivos criados**: 35+ novos arquivos
- **Linhas de código**: ~5.000+ linhas adicionadas
- **Componentes**: 17 componentes reutilizáveis
- **Páginas migradas**: 3 páginas principais
- **Documentação**: 7 documentos completos

---

## [0.9.0] - 2025-10-31 - Impersonation e Force Password

### Adicionado
- Sistema de impersonation completo
- Force password change para novos usuários
- Banner de modo administrador
- Audit logs para impersonation
- Middleware de impersonation

### Corrigido
- Bugs no fluxo de impersonation
- Problemas com refresh tokens
- Validação de senhas temporárias

---

## [0.8.0] - 2025-10-30 - Autenticação Multi-Tenant

### Adicionado
- Sistema de autenticação multi-tenant
- JWT tokens com refresh
- Middleware de autenticação
- Rate limiting
- Session management

---

## [0.7.0] - 2025-10-07 - CRUD de Tenants

### Adicionado
- CRUD completo de tenants
- Provisionamento automático
- Gestão de usuários master
- Validações de subdomínio

---

## [0.6.0] - 2025-09-22 - Migração CockroachDB

### Adicionado
- Migração para CockroachDB
- Schema multi-tenant
- Conexão segura com certificados
- Queries otimizadas

---

## [0.5.0] - 2025-09-16 - Backend Clojure

### Adicionado
- Backend Clojure inicial
- API REST com Reitit
- Conexão com PostgreSQL
- Handlers básicos

---

## [0.1.0] - 2025-09-01 - Projeto Inicial

### Adicionado
- Estrutura inicial do projeto
- Configuração básica
- README inicial

---

## 🎯 Próximas Versões Planejadas

### [1.1.0] - Gestão de Processos (Planejado)
- CRUD de processos jurídicos
- Upload de documentos
- Filtros e busca avançada
- Vinculação com clientes

### [1.2.0] - Gestão de Clientes (Planejado)
- CRUD de clientes
- Histórico de processos
- Documentos do cliente
- Contatos e endereços

### [1.3.0] - Gestão de Operadores (Planejado)
- CRUD de operadores
- Roles e permissões
- Convites por email
- Limite de operadores

### [2.0.0] - Funcionalidades Avançadas (Planejado)
- Sistema de notificações
- Relatórios avançados
- Integrações externas
- Mobile app

---

## 📝 Notas

### Convenções de Commit
- `feat:` - Nova funcionalidade
- `fix:` - Correção de bug
- `docs:` - Documentação
- `style:` - Formatação, CSS
- `refactor:` - Refatoração de código
- `test:` - Testes
- `chore:` - Manutenção

### Versionamento
- **MAJOR** (X.0.0) - Mudanças incompatíveis na API
- **MINOR** (0.X.0) - Novas funcionalidades compatíveis
- **PATCH** (0.0.X) - Correções de bugs

---

**Última atualização**: 03/11/2025  
**Versão atual**: 1.0.0  
**Status**: ✅ Estável e em produção
