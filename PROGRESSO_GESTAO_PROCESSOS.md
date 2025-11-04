# Progresso - Gestão de Processos Jurídicos

**Data:** 04/11/2025  
**Feature:** Sistema de Gestão de Processos Jurídicos  
**Spec:** `.kiro/specs/gestao-processos/`

---

## ✅ Concluído (Tasks 1-3)

### Task 1: Criar tabela de clientes ✅

**Arquivos criados:**
- `migrations/004_create_clientes_table.sql` - Migration da tabela clientes
- `migrations/004_rollback_clientes_table.sql` - Rollback
- `run_migration_clientes.sh` - Script de execução

**Tabela criada:**
- `clientes` - Clientes dos escritórios de advocacia
  - 10 campos (id, tenant_id, nome, cpf_cnpj, email, telefone, endereco, timestamps)
  - Soft delete (deleted_at)
  - 3 índices (tenant_id, cpf_cnpj único por tenant, deleted_at)
  - FK para tenants com CASCADE

**Documentação:**
- `docs/DATABASE_SCHEMA.md` atualizado com tabela clientes

---

### Task 2: Criar migrations das tabelas de processos ✅

**Arquivos criados:**
- `migrations/005_create_processos_tables.sql` - Migration de 3 tabelas
- `migrations/005_rollback_processos_tables.sql` - Rollback
- `run_migration_processos.sh` - Script de execução com validações

**Tabelas criadas:**

1. **processos** (20 campos)
   - Dados completos do processo jurídico
   - Soft delete + auditoria (created_by, updated_by, deleted_by)
   - 7 índices (tenant, cliente, status, número, deleted, created_at)
   - Constraint UNIQUE (tenant_id, numero_processo)

2. **processo_documentos** (9 campos)
   - Metadados de arquivos anexados
   - Suporte para PDF, DOC, DOCX, JPG, PNG
   - 3 índices (processo_id, deleted_at)
   - FK para processos com CASCADE

3. **processo_historico** (8 campos)
   - Auditoria imutável de alterações
   - Timeline completa de eventos
   - 3 índices (processo_id, created_at DESC)
   - FK para processos com CASCADE

**Documentação:**
- `docs/DATABASE_SCHEMA.md` atualizado com:
  - Diagrama de relacionamentos expandido
  - Documentação completa das 3 tabelas
  - Índices e constraints
  - Regras de negócio

---

### Task 3: Implementar protocols do backend ✅

**Arquivo modificado:**
- `src/juridico/api/db/protocols.clj` - Adicionados 4 novos protocols

**Protocols criados:**

1. **ProcessoRepository** (9 funções)
   - `find-all-processos` - Listagem com paginação e filtros
   - `find-processo-by-id` - Busca por ID
   - `find-processo-by-numero` - Validação de duplicatas
   - `create-processo!` - Criação
   - `update-processo!` - Atualização com auditoria
   - `soft-delete-processo!` - Soft delete
   - `search-processos` - Busca full-text
   - `count-processos-by-status` - Estatísticas

2. **DocumentoRepository** (5 funções)
   - `find-documentos-by-processo` - Listar documentos
   - `find-documento-by-id` - Busca por ID
   - `create-documento!` - Registrar upload
   - `soft-delete-documento!` - Soft delete
   - `count-documentos-by-processo` - Contagem

3. **HistoricoRepository** (3 funções)
   - `add-historico!` - Adicionar registro (imutável)
   - `find-historico-by-processo` - Timeline de eventos
   - `count-historico-by-processo` - Contagem

4. **ClienteRepository** (8 funções)
   - `find-all-clientes` - Listagem com paginação
   - `find-cliente-by-id` - Busca por ID
   - `find-cliente-by-cpf-cnpj` - Validação de duplicatas
   - `create-cliente!` - Criação
   - `update-cliente!` - Atualização
   - `soft-delete-cliente!` - Soft delete
   - `search-clientes` - Busca
   - `count-processos-by-cliente` - Estatísticas

---

## 🚧 Pendente (Tasks 4-28)

### Fase 1 - Backend Core (Tasks 4-6)

- [ ] **Task 4:** Implementar repository PostgreSQL
  - Implementar ProcessoRepository em `src/juridico/api/db/postgres.clj`
  - Todas as queries SQL com paginação, filtros e validação de tenant
  - Registro automático de histórico nas operações

- [ ] **Task 5:** Implementar repository de documentos
  - Implementar DocumentoRepository
  - Queries para listar, criar e deletar documentos

- [ ] **Task 6:** Implementar repository de histórico
  - Implementar HistoricoRepository
  - Queries para adicionar e listar histórico

### Fase 2 - Backend API (Tasks 7-11)

- [ ] **Task 7:** Criar handlers de processos
  - `src/juridico/api/handlers/processos.clj`
  - Handlers: list, get, create, update, delete, search
  - Tratamento de erros e validações

- [ ] **Task 8:** Criar handlers de documentos
  - Upload, download, delete de documentos
  - Validação de tipo e tamanho
  - Integração com storage

- [ ] **Task 9:** Criar handlers de histórico
  - Handler para retornar timeline de alterações

- [ ] **Task 10:** Implementar middleware de permissões
  - Validação de roles (master/operador)
  - Permissões por operação

- [ ] **Task 11:** Configurar rotas do backend
  - Adicionar rotas em `src/juridico/api/routes.clj`
  - 10+ endpoints REST
  - Middleware de JWT e tenant validation

### Fase 3 - Frontend BFF (Task 12)

- [ ] **Task 12:** Criar API routes do Next.js
  - 5 arquivos de API routes (BFF pattern)
  - Proxy para backend Clojure
  - Validação de sessão

### Fase 4 - Frontend UI (Tasks 13-27)

- [ ] **Tasks 13-27:** Interface completa
  - Páginas: listagem, detalhes, criar, editar
  - Componentes: filtros, formulário, documentos, histórico
  - Estilos CSS responsivos
  - Validações e loading states
  - Controle de permissões

### Fase 5 - Deploy (Task 28)

- [ ] **Task 28:** Documentação de deploy
  - Guia de deployment
  - Atualização do CHANGELOG

---

## 📊 Estatísticas

**Progresso:** 3/28 tasks completas (10.7%)  
**Fase atual:** Fase 1 - Backend Core  
**Próxima task:** Task 4 - Implementar repository PostgreSQL

---

## 🗄️ Estrutura do Banco de Dados

### Tabelas Criadas (4)

1. ✅ `clientes` - Clientes dos escritórios
2. ✅ `processos` - Processos jurídicos
3. ✅ `processo_documentos` - Documentos anexados
4. ✅ `processo_historico` - Auditoria de alterações

### Relacionamentos

```
tenants (1:N) → clientes (1:N) → processos
users (1:N) → processos (created_by, updated_by, deleted_by)
processos (1:N) → processo_documentos
processos (1:N) → processo_historico
```

---

## 🚀 Como Aplicar as Migrations

### 1. Aplicar tabela de clientes

```bash
chmod +x run_migration_clientes.sh
export DATABASE_URL='postgresql://user:password@host:port/database'
./run_migration_clientes.sh
```

### 2. Aplicar tabelas de processos

```bash
chmod +x run_migration_processos.sh
./run_migration_processos.sh
```

### Verificar tabelas criadas

```sql
-- No CockroachDB
SHOW TABLES;

-- Verificar estrutura
SHOW CREATE TABLE clientes;
SHOW CREATE TABLE processos;
SHOW CREATE TABLE processo_documentos;
SHOW CREATE TABLE processo_historico;
```

---

## 📝 Próximos Passos

1. **Aplicar migrations no banco de dados**
   - Executar scripts de migration
   - Validar estrutura das tabelas

2. **Continuar implementação (Task 4)**
   - Implementar PostgresRepository com todas as queries
   - Adicionar testes de integração

3. **Implementar handlers e rotas**
   - Criar endpoints REST
   - Adicionar validações e tratamento de erros

4. **Desenvolver interface frontend**
   - Criar páginas e componentes
   - Implementar upload de arquivos

---

## 📚 Documentação

- **Spec completa:** `.kiro/specs/gestao-processos/`
  - `requirements.md` - Requisitos funcionais
  - `design.md` - Arquitetura e design técnico
  - `tasks.md` - Plano de implementação (30 tasks)

- **Database:** `docs/DATABASE_SCHEMA.md`
  - Documentação completa do schema
  - Diagramas de relacionamento
  - Índices e constraints

---

**Última atualização:** 04/11/2025  
**Status:** Em desenvolvimento - Fase 1 (Backend Core)
