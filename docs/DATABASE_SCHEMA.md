# Arquitetura do Banco de Dados

## Visão Geral

Este documento descreve a arquitetura completa do banco de dados do sistema jurídico multi-tenant, incluindo todas as tabelas, relacionamentos, índices e constraints.

**Banco de Dados:** CockroachDB  
**Schema:** public  
**Locality:** REGIONAL BY TABLE IN PRIMARY REGION

---

## Diagrama de Relacionamentos

```
┌─────────────────┐
│    tenants      │
│  (Multi-tenant) │
└────────┬────────┘
         │
         │ 1:N
         │
    ┌────┴──────────────────────────────────┐
    │                                       │
    ▼                                       ▼
┌─────────┐                          ┌──────────┐
│  users  │                          │ clientes │
└────┬────┘                          └────┬─────┘
     │                                    │
     │ 1:N (created_by/updated_by)        │ 1:N
     │                                    │
     ▼                                    ▼
┌──────────────┐                    ┌────────────┐
│ legal_cases  │                    │ processos  │
└──────────────┘                    └─────┬──────┘
                                          │
                                          │ 1:N
                                          │
                        ┌─────────────────┴─────────────────┐
                        │                                   │
                        ▼                                   ▼
                ┌──────────────────┐            ┌─────────────────────┐
                │processo_documentos│            │processo_historico   │
                └──────────────────┘            └─────────────────────┘
```

---

## Tabelas

### 1. `tenants` - Tabela de Tenants (Empresas)

Armazena informações sobre cada empresa/cliente que usa o sistema.

#### Estrutura

| Coluna          | Tipo           | Nullable | Default              | Descrição                           |
|-----------------|----------------|----------|----------------------|-------------------------------------|
| id              | INT8           | NOT NULL | unique_rowid()       | Identificador único do tenant       |
| company_name    | VARCHAR(255)   | NOT NULL | -                    | Nome da empresa                     |
| subdomain       | VARCHAR(100)   | NOT NULL | -                    | Subdomínio único (ex: empresa.app)  |
| plan_type       | VARCHAR(50)    | NULL     | 'default'            | Tipo de plano contratado            |
| is_active       | BOOL           | NULL     | true                 | Status de ativação do tenant        |
| created_at      | TIMESTAMPTZ    | NULL     | now()                | Data de criação do registro         |
| operator_limit  | INT8           | NOT NULL | 4                    | Limite de operadores permitidos     |

#### Constraints

- **Primary Key:** `tenants_pkey` (id)
- **Unique:** `tenants_subdomain_key` (subdomain) - Garante subdomínios únicos

#### Índices

1. **tenants_pkey** (PRIMARY KEY)
   - Coluna: id (ASC)
   
2. **tenants_subdomain_key** (UNIQUE)
   - Coluna: subdomain (ASC)

#### Regras de Negócio

- Cada tenant representa uma empresa cliente isolada
- O `subdomain` é usado para roteamento multi-tenant
- O `operator_limit` controla quantos operadores podem ser criados
- Tenants inativos (`is_active = false`) não podem acessar o sistema

---

### 2. `users` - Tabela de Usuários

Armazena todos os usuários do sistema, vinculados a seus respectivos tenants.

#### Estrutura

| Coluna              | Tipo           | Nullable | Default              | Descrição                                    |
|---------------------|----------------|----------|----------------------|----------------------------------------------|
| id                  | INT8           | NOT NULL | unique_rowid()       | Identificador único do usuário               |
| tenant_id           | INT8           | NOT NULL | -                    | FK para tenants - isolamento multi-tenant    |
| email               | VARCHAR(255)   | NOT NULL | -                    | Email do usuário (único globalmente)         |
| password_hash       | VARCHAR(255)   | NOT NULL | -                    | Hash bcrypt da senha                         |
| role                | VARCHAR(50)    | NOT NULL | -                    | Papel do usuário (master/operador/super-admin) |
| full_name           | VARCHAR(255)   | NULL     | -                    | Nome completo do usuário                     |
| created_at          | TIMESTAMPTZ    | NULL     | now()                | Data de criação do registro                  |
| temporary_password  | BOOL           | NULL     | false                | Flag para forçar troca de senha              |

#### Constraints

- **Primary Key:** `users_pkey` (id)
- **Foreign Keys:**
  - `users_tenant_id_fkey`: tenant_id → tenants(id) ON DELETE CASCADE
- **Unique:**
  - `users_tenant_id_email_key` (tenant_id, email) - Email único por tenant
  - `unique_email` (email) - Email único globalmente
- **Check:**
  - `users_role_check`: role IN ('master', 'operador', 'super-admin')

#### Índices

1. **users_pkey** (PRIMARY KEY)
   - Coluna: id (ASC)

2. **users_tenant_id_email_key** (UNIQUE)
   - Colunas: tenant_id (ASC), email (ASC)

3. **unique_email** (UNIQUE)
   - Coluna: email (ASC)

4. **idx_users_temporary_password** (PARTIAL INDEX)
   - Coluna: temporary_password (ASC)
   - Condição: WHERE temporary_password = true
   - Otimiza queries para usuários que precisam trocar senha

#### Regras de Negócio

- **Roles disponíveis:**
  - `super-admin`: Administrador global do sistema
  - `master`: Administrador do tenant (1 por tenant)
  - `operador`: Usuário operacional do tenant
  
- Email é único globalmente (não pode repetir entre tenants)
- Quando um tenant é deletado, todos seus usuários são removidos (CASCADE)
- `temporary_password = true` força o usuário a trocar senha no próximo login

---

### 3. `legal_cases` - Tabela de Processos Jurídicos

Armazena informações sobre processos jurídicos gerenciados no sistema.

#### Estrutura

| Coluna              | Tipo           | Nullable | Default              | Descrição                                |
|---------------------|----------------|----------|----------------------|------------------------------------------|
| id                  | INT8           | NOT NULL | unique_rowid()       | Identificador único do processo          |
| tenant_id           | INT8           | NOT NULL | -                    | FK para tenants - isolamento multi-tenant|
| case_number         | VARCHAR(255)   | NOT NULL | -                    | Número do processo judicial              |
| details             | STRING         | NULL     | -                    | Detalhes e observações do processo       |
| created_by_user_id  | INT8           | NULL     | -                    | FK para users - usuário que criou        |
| created_at          | TIMESTAMPTZ    | NULL     | now()                | Data de criação do registro              |
| jurisdiction        | VARCHAR(255)   | NULL     | -                    | Jurisdição/comarca do processo           |

#### Constraints

- **Primary Key:** `legal_cases_pkey` (id)
- **Foreign Keys:**
  - `legal_cases_tenant_id_fkey`: tenant_id → tenants(id) ON DELETE CASCADE
  - `legal_cases_created_by_user_id_fkey`: created_by_user_id → users(id)

#### Índices

1. **legal_cases_pkey** (PRIMARY KEY)
   - Coluna: id (ASC)

#### Regras de Negócio

- Cada processo pertence a um tenant específico
- Processos são isolados por tenant (multi-tenancy)
- O campo `created_by_user_id` rastreia quem criou o processo
- Quando um tenant é deletado, todos seus processos são removidos (CASCADE)
- Se um usuário for deletado, o campo `created_by_user_id` fica NULL (sem CASCADE)

---

### 4. `clientes` - Tabela de Clientes

Armazena informações sobre clientes dos escritórios de advocacia.

#### Estrutura

| Coluna          | Tipo           | Nullable | Default              | Descrição                           |
|-----------------|----------------|----------|----------------------|-------------------------------------|
| id              | INT8           | NOT NULL | unique_rowid()       | Identificador único do cliente      |
| tenant_id       | INT8           | NOT NULL | -                    | FK para tenants - isolamento        |
| nome            | VARCHAR(255)   | NOT NULL | -                    | Nome completo do cliente            |
| cpf_cnpj        | VARCHAR(20)    | NULL     | -                    | CPF ou CNPJ do cliente              |
| email           | VARCHAR(255)   | NULL     | -                    | Email do cliente                    |
| telefone        | VARCHAR(20)    | NULL     | -                    | Telefone de contato                 |
| endereco        | TEXT           | NULL     | -                    | Endereço completo                   |
| created_at      | TIMESTAMPTZ    | NULL     | now()                | Data de criação do registro         |
| updated_at      | TIMESTAMPTZ    | NULL     | now()                | Data da última atualização          |
| deleted_at      | TIMESTAMPTZ    | NULL     | -                    | Soft delete timestamp               |

#### Constraints

- **Primary Key:** `clientes_pkey` (id)
- **Foreign Keys:**
  - `clientes_tenant_id_fkey`: tenant_id → tenants(id) ON DELETE CASCADE
- **Unique:**
  - `idx_clientes_cpf_tenant` (tenant_id, cpf_cnpj) WHERE deleted_at IS NULL

#### Índices

1. **clientes_pkey** (PRIMARY KEY)
   - Coluna: id (ASC)

2. **idx_clientes_tenant**
   - Coluna: tenant_id (ASC)

3. **idx_clientes_cpf_tenant** (UNIQUE, PARTIAL)
   - Colunas: tenant_id (ASC), cpf_cnpj (ASC)
   - Condição: WHERE deleted_at IS NULL
   - Garante CPF/CNPJ único por tenant (excluindo deletados)

4. **idx_clientes_deleted** (PARTIAL)
   - Coluna: deleted_at (ASC)
   - Condição: WHERE deleted_at IS NULL
   - Otimiza queries de clientes ativos

#### Regras de Negócio

- Cada cliente pertence a um tenant específico
- CPF/CNPJ é único por tenant (não pode repetir no mesmo escritório)
- Soft delete permite manter histórico
- Quando um tenant é deletado, todos seus clientes são removidos (CASCADE)
- Clientes podem ter múltiplos processos vinculados

---

### 5. `processos` - Tabela de Processos Jurídicos

Armazena informações sobre processos jurídicos gerenciados pelos escritórios.

#### Estrutura

| Coluna             | Tipo           | Nullable | Default              | Descrição                                |
|--------------------|----------------|----------|----------------------|------------------------------------------|
| id                 | INT8           | NOT NULL | unique_rowid()       | Identificador único do processo          |
| tenant_id          | INT8           | NOT NULL | -                    | FK para tenants - isolamento             |
| numero_processo    | VARCHAR(50)    | NOT NULL | -                    | Número do processo judicial              |
| cliente_id         | INT8           | NOT NULL | -                    | FK para clientes - cliente do processo   |
| tipo               | VARCHAR(100)   | NOT NULL | -                    | Tipo do processo (Cível, Trabalhista...) |
| vara               | VARCHAR(200)   | NULL     | -                    | Vara ou tribunal do processo             |
| comarca            | VARCHAR(100)   | NULL     | -                    | Comarca do processo                      |
| uf                 | VARCHAR(2)     | NULL     | -                    | Estado (UF)                              |
| status             | VARCHAR(50)    | NULL     | 'Em Andamento'       | Status do processo                       |
| valor_causa        | DECIMAL(15,2)  | NULL     | -                    | Valor da causa                           |
| data_distribuicao  | DATE           | NULL     | -                    | Data de distribuição do processo         |
| descricao          | TEXT           | NULL     | -                    | Descrição detalhada do processo          |
| observacoes        | TEXT           | NULL     | -                    | Observações adicionais                   |
| created_by         | INT8           | NULL     | -                    | FK para users - quem criou               |
| updated_by         | INT8           | NULL     | -                    | FK para users - quem atualizou           |
| deleted_by         | INT8           | NULL     | -                    | FK para users - quem deletou             |
| created_at         | TIMESTAMPTZ    | NULL     | now()                | Data de criação do registro              |
| updated_at         | TIMESTAMPTZ    | NULL     | now()                | Data da última atualização               |
| deleted_at         | TIMESTAMPTZ    | NULL     | -                    | Soft delete timestamp                    |

#### Constraints

- **Primary Key:** `processos_pkey` (id)
- **Foreign Keys:**
  - `processos_tenant_id_fkey`: tenant_id → tenants(id) ON DELETE CASCADE
  - `processos_cliente_id_fkey`: cliente_id → clientes(id)
  - `processos_created_by_fkey`: created_by → users(id)
  - `processos_updated_by_fkey`: updated_by → users(id)
  - `processos_deleted_by_fkey`: deleted_by → users(id)
- **Unique:**
  - `unique_processo_tenant` (tenant_id, numero_processo) - Número único por tenant

#### Índices

1. **processos_pkey** (PRIMARY KEY)
   - Coluna: id (ASC)

2. **idx_processos_tenant**
   - Coluna: tenant_id (ASC)

3. **idx_processos_cliente**
   - Coluna: cliente_id (ASC)

4. **idx_processos_status**
   - Coluna: status (ASC)

5. **idx_processos_numero**
   - Coluna: numero_processo (ASC)

6. **idx_processos_deleted** (PARTIAL)
   - Coluna: deleted_at (ASC)
   - Condição: WHERE deleted_at IS NULL

7. **idx_processos_created_at**
   - Coluna: created_at (DESC)

#### Regras de Negócio

- Cada processo pertence a um tenant e um cliente
- Número do processo é único por tenant
- Status padrão: "Em Andamento"
- Opções de status: Em Andamento, Suspenso, Arquivado, Encerrado
- Soft delete permite manter histórico
- Rastreamento de quem criou, atualizou e deletou
- Quando tenant é deletado, processos são removidos (CASCADE)

---

### 6. `processo_documentos` - Documentos dos Processos

Armazena metadados de documentos anexados aos processos.

#### Estrutura

| Coluna           | Tipo           | Nullable | Default              | Descrição                           |
|------------------|----------------|----------|----------------------|-------------------------------------|
| id               | INT8           | NOT NULL | unique_rowid()       | Identificador único do documento    |
| processo_id      | INT8           | NOT NULL | -                    | FK para processos                   |
| nome_arquivo     | VARCHAR(255)   | NOT NULL | -                    | Nome original do arquivo            |
| tipo_arquivo     | VARCHAR(50)    | NULL     | -                    | Tipo/extensão (PDF, DOC, JPG...)    |
| tamanho_bytes    | INT8           | NULL     | -                    | Tamanho do arquivo em bytes         |
| caminho_storage  | VARCHAR(500)   | NOT NULL | -                    | Caminho no storage (filesystem/S3)  |
| uploaded_by      | INT8           | NULL     | -                    | FK para users - quem fez upload     |
| created_at       | TIMESTAMPTZ    | NULL     | now()                | Data do upload                      |
| deleted_at       | TIMESTAMPTZ    | NULL     | -                    | Soft delete timestamp               |

#### Constraints

- **Primary Key:** `processo_documentos_pkey` (id)
- **Foreign Keys:**
  - `processo_documentos_processo_id_fkey`: processo_id → processos(id) ON DELETE CASCADE
  - `processo_documentos_uploaded_by_fkey`: uploaded_by → users(id)

#### Índices

1. **processo_documentos_pkey** (PRIMARY KEY)
   - Coluna: id (ASC)

2. **idx_processo_docs_processo**
   - Coluna: processo_id (ASC)

3. **idx_processo_docs_deleted** (PARTIAL)
   - Coluna: deleted_at (ASC)
   - Condição: WHERE deleted_at IS NULL

#### Regras de Negócio

- Cada documento pertence a um processo
- Tipos permitidos: PDF, DOC, DOCX, JPG, PNG
- Tamanho máximo: 10MB
- Soft delete permite manter histórico
- Quando processo é deletado, documentos são removidos (CASCADE)
- Arquivos físicos devem ser armazenados em storage seguro

---

### 7. `processo_historico` - Histórico de Alterações

Auditoria completa de todas as alterações nos processos.

#### Estrutura

| Coluna          | Tipo           | Nullable | Default              | Descrição                           |
|-----------------|----------------|----------|----------------------|-------------------------------------|
| id              | INT8           | NOT NULL | unique_rowid()       | Identificador único do registro     |
| processo_id     | INT8           | NOT NULL | -                    | FK para processos                   |
| user_id         | INT8           | NULL     | -                    | FK para users - quem fez alteração  |
| acao            | VARCHAR(50)    | NOT NULL | -                    | Tipo de ação realizada              |
| campo_alterado  | VARCHAR(100)   | NULL     | -                    | Nome do campo alterado              |
| valor_anterior  | TEXT           | NULL     | -                    | Valor antes da alteração            |
| valor_novo      | TEXT           | NULL     | -                    | Valor após a alteração              |
| created_at      | TIMESTAMPTZ    | NULL     | now()                | Data/hora da alteração              |

#### Constraints

- **Primary Key:** `processo_historico_pkey` (id)
- **Foreign Keys:**
  - `processo_historico_processo_id_fkey`: processo_id → processos(id) ON DELETE CASCADE
  - `processo_historico_user_id_fkey`: user_id → users(id)

#### Índices

1. **processo_historico_pkey** (PRIMARY KEY)
   - Coluna: id (ASC)

2. **idx_processo_hist_processo**
   - Coluna: processo_id (ASC)

3. **idx_processo_hist_created**
   - Coluna: created_at (DESC)

#### Regras de Negócio

- Registro imutável de todas as alterações
- Tipos de ação: criacao, edicao, exclusao, mudanca_status
- Armazena valores anterior e novo para comparação
- Quando processo é deletado, histórico é removido (CASCADE)
- Usado para auditoria e timeline de eventos

---

## Estratégia Multi-Tenant

O sistema utiliza **Row-Level Multi-Tenancy** com isolamento por `tenant_id`:

1. **Isolamento de Dados:**
   - Todas as tabelas principais têm `tenant_id`
   - Queries sempre filtram por `tenant_id` do usuário logado
   - Foreign keys com CASCADE garantem integridade referencial

2. **Hierarquia de Acesso:**
   ```
   super-admin (global)
        │
        ├─── Tenant A
        │     ├─── master (admin do tenant)
        │     └─── operadores
        │
        └─── Tenant B
              ├─── master (admin do tenant)
              └─── operadores
   ```

3. **Segurança:**
   - Subdomínio único por tenant
   - Email único globalmente
   - Validação de tenant_id em todas as operações
   - Cascade delete para limpeza automática

---

## Índices e Performance

### Índices Existentes

1. **Primary Keys:** Todas as tabelas têm PK em `id`
2. **Unique Constraints:** 
   - `tenants.subdomain`
   - `users.email` (global)
   - `users.(tenant_id, email)` (por tenant)
   - `clientes.(tenant_id, cpf_cnpj)` (por tenant, excluindo deletados)
   - `processos.(tenant_id, numero_processo)` (número único por tenant)
3. **Partial Indexes (otimizados para soft delete):** 
   - `users.temporary_password` (WHERE = true)
   - `clientes.deleted_at` (WHERE IS NULL)
   - `processos.deleted_at` (WHERE IS NULL)
   - `processo_documentos.deleted_at` (WHERE IS NULL)
4. **Performance Indexes:**
   - `processos.created_at` (DESC) - Listagem ordenada
   - `processo_historico.created_at` (DESC) - Timeline de eventos

### Índices Implementados

Todos os índices necessários já foram criados nas migrations. Os principais são:

**Processos:**
- tenant_id, cliente_id, status, numero_processo
- created_at DESC (para ordenação)
- deleted_at (partial index para ativos)

**Documentos:**
- processo_id (para listar documentos de um processo)
- deleted_at (partial index para ativos)

**Histórico:**
- processo_id (para timeline)
- created_at DESC (para ordenação cronológica)

---

## Convenções e Padrões

### Nomenclatura

- **Tabelas:** snake_case, plural (users, legal_cases)
- **Colunas:** snake_case (tenant_id, created_at)
- **Constraints:** `{table}_{column}_key` ou `{table}_{column}_fkey`
- **Índices:** `idx_{table}_{column}` ou `{table}_pkey`

### Tipos de Dados

- **IDs:** INT8 com `unique_rowid()` (padrão CockroachDB)
- **Timestamps:** TIMESTAMPTZ (com timezone)
- **Strings:** VARCHAR com limite ou STRING (ilimitado)
- **Booleans:** BOOL com defaults explícitos

### Defaults

- **created_at:** `now():::TIMESTAMPTZ`
- **Flags booleanas:** Sempre com default (true/false)
- **IDs:** `unique_rowid()` (distribuído, não sequencial)

---

## Migrations e Versionamento

### Localização dos Scripts

- Migrations SQL: `/migrations/` (raiz do projeto)
- Migrations existentes:
  - `001_*` - Tabelas iniciais (tenants, users)
  - `002_*` - Temporary password
  - `003_*` - Legal cases
  - `004_*` - Clientes
  - `005_*` - Processos (processos, documentos, histórico)

### Processo de Migration

1. Criar arquivo SQL com nome descritivo
2. Testar em ambiente de desenvolvimento
3. Executar via script shell ou manualmente
4. Documentar no CHANGELOG.md

---

## Evoluções Recentes

### ✅ Feature: Force Password Change (Implementado em 30/10/2025)

**Alteração:** Adicionada coluna `temporary_password` na tabela `users`

- Permite forçar usuários a trocarem senha no primeiro login
- Índice parcial criado para otimizar queries
- Usado principalmente para usuários master criados pelo super-admin

### ✅ Feature: Gestão de Processos (Implementado em 05/11/2025)

**Tabelas criadas:**

1. **clientes** - Clientes dos escritórios de advocacia
   - Soft delete implementado
   - CPF/CNPJ único por tenant
   - Vinculado a processos

2. **processos** - Processos jurídicos completos
   - Número único por tenant
   - Status: Em Andamento, Suspenso, Arquivado, Encerrado
   - Auditoria completa (created_by, updated_by, deleted_by)
   - Soft delete implementado

3. **processo_documentos** - Documentos anexados aos processos
   - Metadados de arquivos (nome, tipo, tamanho)
   - Caminho para storage (filesystem ou S3)
   - Soft delete implementado

4. **processo_historico** - Auditoria de alterações
   - Registro imutável de todas as mudanças
   - Armazena valores anterior e novo
   - Timeline completa de eventos

**Impacto:** Sistema agora suporta gestão completa de clientes e processos jurídicos com auditoria.

Veja: `.kiro/specs/gestao-processos/` para detalhes completos.

### Próximas Evoluções Planejadas

1. **case_parties** - Partes do processo (autor, réu, advogados)
2. **case_hearings** - Audiências e prazos processuais
3. **case_movements** - Movimentações processuais automáticas

---

## Referências

- **CockroachDB Docs:** https://www.cockroachlabs.com/docs/
- **Multi-Tenancy Patterns:** https://www.cockroachlabs.com/docs/stable/multi-region-overview.html
- **Specs do Projeto:** `.kiro/specs/`

---

## Histórico de Alterações

| Data       | Versão | Alteração                                                    |
|------------|--------|--------------------------------------------------------------|
| 05/11/2025 | 1.2    | Adicionadas tabelas de gestão de processos (clientes, processos, processo_documentos, processo_historico) |
| 30/10/2025 | 1.1    | Adicionada coluna temporary_password na tabela users         |
| 29/10/2025 | 1.0    | Schema inicial (tenants, users, legal_cases)                 |

---

**Última Atualização:** 05/11/2025  
**Versão do Schema:** 1.2  
**Mantido por:** Equipe de Desenvolvimento
