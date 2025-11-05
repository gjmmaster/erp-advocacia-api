# Changelog - Legal ERP

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased] - 2025-11-04 - EM DESENVOLVIMENTO 🚧

### 🏗️ Gestão de Processos Jurídicos (Fase 1 + 2 - Backend Completo) ✅

#### Adicionado - Database
- **Tabela `clientes`**: Clientes dos escritórios de advocacia
  - 10 campos com soft delete
  - Índices: tenant_id, cpf_cnpj único por tenant
  - FK para tenants com CASCADE
  - Migration: `004_create_clientes_table.sql`

- **Tabela `processos`**: Processos jurídicos completos
  - 20 campos com auditoria completa (created_by, updated_by, deleted_by)
  - 7 índices otimizados (tenant, cliente, status, número, deleted, created_at)
  - Constraint UNIQUE (tenant_id, numero_processo)
  - Soft delete

- **Tabela `processo_documentos`**: Documentos anexados aos processos
  - 9 campos para metadados de arquivos
  - Suporte para PDF, DOC, DOCX, JPG, PNG
  - 3 índices (processo_id, deleted_at)
  - FK para processos com CASCADE

- **Tabela `processo_historico`**: Auditoria imutável de alterações
  - 8 campos para timeline de eventos
  - 3 índices (processo_id, created_at DESC)
  - FK para processos com CASCADE
  - Migration: `005_create_processos_tables.sql`

#### Adicionado - Backend Protocols
- **ProcessoRepository**: 9 funções para CRUD de processos
  - Listagem com paginação e filtros
  - Busca full-text
  - Soft delete com auditoria
  - Validação de duplicatas

- **DocumentoRepository**: 5 funções para gestão de documentos
  - Upload e download de arquivos
  - Soft delete
  - Contagem por processo

- **HistoricoRepository**: 3 funções para auditoria
  - Registro imutável de alterações
  - Timeline de eventos
  - Contagem de registros

- **ClienteRepository**: 8 funções para gestão de clientes
  - CRUD completo
  - Busca e validação de CPF/CNPJ
  - Soft delete

#### Adicionado - Documentação
- `docs/DATABASE_SCHEMA.md` atualizado com:
  - Diagrama de relacionamentos expandido
  - Documentação completa de 4 novas tabelas
  - Índices, constraints e regras de negócio
  
- `PROGRESSO_GESTAO_PROCESSOS.md`: Documento de progresso
  - 3/28 tasks completas (10.7%)
  - Estatísticas e próximos passos
  - Guia de aplicação das migrations

- Spec completa em `.kiro/specs/gestao-processos/`:
  - `requirements.md`: 10 requisitos funcionais em formato EARS
  - `design.md`: Arquitetura completa (backend + frontend)
  - `tasks.md`: 30 tarefas organizadas em 5 fases

#### Scripts de Migration
- `run_migration_clientes.sh`: Script para aplicar tabela clientes
- `run_migration_processos.sh`: Script para aplicar tabelas de processos
  - Validação de pré-requisitos
  - Verificação de tabela clientes
  - Rollback automático em caso de erro

#### Adicionado - Backend Repositories (Tasks 4-6)
- **ProcessoRepository**: 9 funções implementadas
  - Listagem com paginação e filtros dinâmicos
  - Busca full-text em múltiplos campos
  - Criação com registro automático no histórico
  - Atualização com auditoria completa de alterações
  - Soft delete com registro no histórico
  - Validação de duplicatas por número
  - Estatísticas por status

- **DocumentoRepository**: 5 funções implementadas
  - Listagem de documentos por processo
  - Criação de metadados de documentos
  - Soft delete de documentos
  - Contagem de documentos por processo

- **HistoricoRepository**: 3 funções implementadas
  - Inserção imutável de registros de histórico
  - Timeline de alterações com JOIN de usuários
  - Contagem de registros

- **ClienteRepository**: 8 funções implementadas
  - CRUD completo de clientes
  - Busca em múltiplos campos (nome, CPF/CNPJ, email)
  - Validação de duplicatas por CPF/CNPJ
  - Contagem de processos por cliente
  - Paginação e filtros

**Total:** 25 funções implementadas em 4 repositories

#### Adicionado - Backend Handlers (Tasks 7-9)
- **Arquivo criado:** `src/juridico/api/handlers/processos.clj`

**Handlers de Processos (6):**
- `list-processos-handler` - Listagem com paginação e filtros
- `get-processo-handler` - Detalhes de um processo
- `create-processo-handler` - Criação com validações
- `update-processo-handler` - Atualização com auditoria
- `delete-processo-handler` - Soft delete
- `search-processos-handler` - Busca full-text

**Handlers de Documentos (3):**
- `list-documentos-handler` - Lista documentos de um processo
- `create-documento-handler` - Registra metadados
- `delete-documento-handler` - Soft delete

**Handlers de Histórico (1):**
- `get-historico-handler` - Timeline de alterações

**Handlers de Clientes (6):**
- `list-clientes-handler` - Listagem com paginação
- `get-cliente-handler` - Detalhes de um cliente
- `create-cliente-handler` - Criação com validações
- `update-cliente-handler` - Atualização
- `delete-cliente-handler` - Soft delete com validação
- `search-clientes-handler` - Busca

**Total:** 16 handlers implementados

**Validações implementadas:**
- Campos obrigatórios (nome, número, tipo, cliente)
- Duplicatas (número de processo, CPF/CNPJ)
- Permissões de tenant (isolamento multi-tenant)
- Validação de relacionamentos
- Tamanho mínimo de busca (2 caracteres)
- Tratamento de erros (404, 400, 409)

#### Adicionado - Backend Routes (Task 11)
- **Arquivo modificado:** `src/juridico/api/core.clj`

**15 endpoints REST criados:**

Processos:
- `GET /api/tenant/processos` - Lista com filtros
- `POST /api/tenant/processos` - Criar
- `GET /api/tenant/processos/search` - Buscar
- `GET /api/tenant/processos/:id` - Detalhes
- `PUT /api/tenant/processos/:id` - Atualizar
- `DELETE /api/tenant/processos/:id` - Deletar

Documentos:
- `GET /api/tenant/processos/:processo-id/documentos` - Listar
- `POST /api/tenant/processos/:processo-id/documentos` - Criar
- `DELETE /api/tenant/processos/:processo-id/documentos/:documento-id` - Deletar

Histórico:
- `GET /api/tenant/processos/:processo-id/historico` - Timeline

Clientes:
- `GET /api/tenant/clientes` - Lista
- `POST /api/tenant/clientes` - Criar
- `GET /api/tenant/clientes/search` - Buscar
- `GET /api/tenant/clientes/:id` - Detalhes
- `PUT /api/tenant/clientes/:id` - Atualizar
- `DELETE /api/tenant/clientes/:id` - Deletar

**Middleware aplicado:**
- JWT authentication em todas as rotas
- Tenant validation automática
- Rate limiting
- CORS configurado
- JSON parsing automático

#### Adicionado - Documentação
- `RESUMO_SESSAO_BACKEND_PROCESSOS.md` - Resumo completo da implementação
- `GUIA_APLICAR_MIGRATIONS.md` - Guia passo a passo para aplicar migrations
- `PROGRESSO_GESTAO_PROCESSOS.md` atualizado - 11/28 tasks completas (39%)

#### Pendente (17 tasks)
- Task 12: API routes do Next.js (BFF)
- Task 13-27: Interface frontend completa
- Task 28: Documentação de deploy

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
