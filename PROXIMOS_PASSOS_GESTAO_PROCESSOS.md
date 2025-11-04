# Próximos Passos - Gestão de Processos

**Última atualização:** 04/11/2025  
**Progresso:** 3/30 tasks (10%)  
**Fase atual:** Fase 1 - Backend Core

---

## 🎯 Resumo Rápido

Você está implementando o sistema de **Gestão de Processos Jurídicos** para o painel dos tenants.

**Já feito:**
- ✅ Spec completa (requirements, design, tasks)
- ✅ 4 tabelas no banco (clientes, processos, processo_documentos, processo_historico)
- ✅ 4 protocols definidos (25 funções)

**Falta fazer:**
- ⏳ Implementar repositories (queries SQL)
- ⏳ Criar handlers e rotas (backend API)
- ⏳ Criar API routes (Next.js BFF)
- ⏳ Desenvolver interface (frontend UI)

---

## 📋 Próximas 3 Tasks (Fase 1 - Backend Core)

### Task 4: Implementar repository PostgreSQL ⏳

**Arquivo:** `src/juridico/api/db/postgres.clj`

**O que fazer:**
1. Adicionar implementação de `ProcessoRepository` no record `PostgresRepository`
2. Implementar 9 funções com queries SQL:
   - `find-all-processos` - SELECT com JOIN, paginação e filtros
   - `find-processo-by-id` - SELECT com validação de tenant
   - `find-processo-by-numero` - SELECT para validar duplicatas
   - `create-processo!` - INSERT com retorno do ID
   - `update-processo!` - UPDATE com registro de histórico
   - `soft-delete-processo!` - UPDATE deleted_at
   - `search-processos` - SELECT com LIKE/ILIKE
   - `count-processos-by-status` - COUNT com filtro

**Complexidade:** Alta  
**Tempo estimado:** 2-3 horas  
**Dependências:** Nenhuma (pode começar)

**Exemplo de implementação:**

```clojure
(defrecord PostgresRepository [db-spec]
  ProcessoRepository
  
  (find-all-processos [this tenant-id opts]
    (let [{:keys [page per-page status tipo cliente-id]} opts
          offset (* (dec page) per-page)]
      (jdbc/execute! db-spec
        ["SELECT p.*, c.nome as cliente_nome
          FROM processos p
          LEFT JOIN clientes c ON p.cliente_id = c.id
          WHERE p.tenant_id = ? 
            AND p.deleted_at IS NULL
            AND (? IS NULL OR p.status = ?)
            AND (? IS NULL OR p.tipo = ?)
            AND (? IS NULL OR p.cliente_id = ?)
          ORDER BY p.created_at DESC
          LIMIT ? OFFSET ?"
         tenant-id status status tipo tipo cliente-id cliente-id per-page offset])))
  
  ;; ... outras 8 funções
)
```

---

### Task 5: Implementar repository de documentos ⏳

**Arquivo:** `src/juridico/api/db/postgres.clj` (mesmo arquivo)

**O que fazer:**
1. Adicionar implementação de `DocumentoRepository`
2. Implementar 5 funções:
   - `find-documentos-by-processo` - SELECT com filtro deleted_at
   - `find-documento-by-id` - SELECT simples
   - `create-documento!` - INSERT
   - `soft-delete-documento!` - UPDATE deleted_at
   - `count-documentos-by-processo` - COUNT

**Complexidade:** Média  
**Tempo estimado:** 1 hora  
**Dependências:** Task 4 (mesmo arquivo)

---

### Task 6: Implementar repository de histórico ⏳

**Arquivo:** `src/juridico/api/db/postgres.clj` (mesmo arquivo)

**O que fazer:**
1. Adicionar implementação de `HistoricoRepository`
2. Implementar 3 funções:
   - `add-historico!` - INSERT (chamado automaticamente em updates)
   - `find-historico-by-processo` - SELECT ORDER BY created_at DESC
   - `count-historico-by-processo` - COUNT

**Complexidade:** Baixa  
**Tempo estimado:** 30 minutos  
**Dependências:** Task 4 (mesmo arquivo)

---

## 📊 Roadmap Completo

### ✅ Fase 1 - Database & Backend Core (Tasks 1-6)

- [x] Task 1: Criar tabela de clientes
- [x] Task 2: Criar migrations das tabelas de processos
- [x] Task 3: Implementar protocols do backend
- [ ] Task 4: Implementar repository PostgreSQL ← **PRÓXIMA**
- [ ] Task 5: Implementar repository de documentos
- [ ] Task 6: Implementar repository de histórico

**Progresso:** 50% (3/6)

---

### ⏳ Fase 2 - Backend API (Tasks 7-11)

- [ ] Task 7: Criar handlers de processos
- [ ] Task 8: Criar handlers de documentos
- [ ] Task 9: Criar handlers de histórico
- [ ] Task 10: Implementar middleware de permissões
- [ ] Task 11: Configurar rotas do backend

**Progresso:** 0% (0/5)

---

### ⏳ Fase 3 - Frontend BFF (Task 12)

- [ ] Task 12: Criar API routes do Next.js

**Progresso:** 0% (0/1)

---

### ⏳ Fase 4 - Frontend UI (Tasks 13-27)

**Páginas:**
- [ ] Task 13: Criar página de listagem de processos
- [ ] Task 16: Criar página de novo processo
- [ ] Task 17: Criar página de detalhes do processo
- [ ] Task 21: Implementar página de edição

**Componentes:**
- [ ] Task 14: Criar componente de filtros
- [ ] Task 15: Criar formulário de processo
- [ ] Task 18: Implementar seção de documentos
- [ ] Task 19: Implementar timeline de histórico
- [ ] Task 20: Implementar edição de status

**Funcionalidades:**
- [ ] Task 22: Implementar exclusão de processo
- [ ] Task 23: Implementar filtro de arquivados
- [ ] Task 24: Adicionar estilos CSS
- [ ] Task 25: Implementar controle de permissões no frontend
- [ ] Task 26: Adicionar validações e mensagens de erro
- [ ] Task 27: Implementar loading states

**Progresso:** 0% (0/15)

---

### ⏳ Fase 5 - Deploy & Tests (Tasks 28-30)

- [ ] Task 28: Criar migration script e documentação
- [ ] Task 29: Adicionar testes backend (opcional)
- [ ] Task 30: Adicionar testes frontend (opcional)

**Progresso:** 0% (0/3)

---

## 🗂️ Estrutura de Arquivos

### Já Criados ✅

```
migrations/
├── 004_create_clientes_table.sql
├── 004_rollback_clientes_table.sql
├── 005_create_processos_tables.sql
└── 005_rollback_processos_tables.sql

src/juridico/api/db/
└── protocols.clj (4 protocols adicionados)

docs/
└── DATABASE_SCHEMA.md

.kiro/specs/gestao-processos/
├── requirements.md
├── design.md
└── tasks.md

run_migration_clientes.sh
run_migration_processos.sh
PROGRESSO_GESTAO_PROCESSOS.md
RESUMO_SESSAO_04_11_2025.md
```

### A Criar (Próximas Tasks) ⏳

```
src/juridico/api/
├── db/
│   └── postgres.clj (modificar - adicionar implementations)
├── handlers/
│   ├── processos.clj (criar)
│   ├── documentos.clj (criar)
│   └── historico.clj (criar)
└── routes.clj (modificar - adicionar rotas)

frontend-nextjs/src/
├── app/
│   ├── processos/
│   │   ├── page.tsx
│   │   ├── novo/page.tsx
│   │   └── [id]/
│   │       ├── page.tsx
│   │       └── editar/page.tsx
│   └── api/
│       └── processos/
│           ├── route.ts
│           ├── [id]/route.ts
│           ├── [id]/documentos/route.ts
│           ├── [id]/historico/route.ts
│           └── search/route.ts
└── components/
    └── processos/
        ├── ProcessosList.tsx
        ├── ProcessosList.module.css
        ├── ProcessoForm.tsx
        ├── ProcessoForm.module.css
        ├── ProcessoDetails.tsx
        ├── ProcessoDetails.module.css
        ├── ProcessosFilters.tsx
        ├── DocumentosList.tsx
        ├── DocumentosList.module.css
        ├── HistoricoTimeline.tsx
        └── HistoricoTimeline.module.css
```

---

## 🚀 Como Retomar o Trabalho

### 1. Aplicar Migrations (Se Ainda Não Fez)

```bash
# Definir DATABASE_URL
export DATABASE_URL='postgresql://user:password@host:port/database'

# Aplicar clientes
chmod +x run_migration_clientes.sh
./run_migration_clientes.sh

# Aplicar processos
chmod +x run_migration_processos.sh
./run_migration_processos.sh

# Verificar
psql $DATABASE_URL -c "SHOW TABLES;"
```

### 2. Abrir Arquivo da Próxima Task

```bash
# Abrir arquivo que será modificado
code src/juridico/api/db/postgres.clj

# Abrir spec para referência
code .kiro/specs/gestao-processos/design.md
code .kiro/specs/gestao-processos/tasks.md
```

### 3. Começar Task 4

Procure no arquivo `postgres.clj` pelo record `PostgresRepository` e adicione as implementações dos protocols.

**Dica:** Use as implementações existentes (AuthRepository, ProcessosRepository) como referência.

---

## 📚 Documentação de Referência

### Para Task 4 (Repository)

- **Design:** `.kiro/specs/gestao-processos/design.md` (seção "Repository Implementation")
- **Protocols:** `src/juridico/api/db/protocols.clj` (definições das funções)
- **Database Schema:** `docs/DATABASE_SCHEMA.md` (estrutura das tabelas)
- **Exemplo existente:** `src/juridico/api/db/postgres.clj` (implementações atuais)

### Queries SQL Úteis

```sql
-- Listar processos com cliente
SELECT p.*, c.nome as cliente_nome
FROM processos p
LEFT JOIN clientes c ON p.cliente_id = c.id
WHERE p.tenant_id = ? AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
LIMIT 20 OFFSET 0;

-- Buscar processo por número
SELECT * FROM processos
WHERE tenant_id = ? AND numero_processo = ? AND deleted_at IS NULL;

-- Criar processo
INSERT INTO processos (
  tenant_id, numero_processo, cliente_id, tipo, status, created_by, created_at
) VALUES (?, ?, ?, ?, 'Em Andamento', ?, NOW())
RETURNING *;

-- Atualizar processo
UPDATE processos
SET status = ?, updated_by = ?, updated_at = NOW()
WHERE id = ? AND tenant_id = ? AND deleted_at IS NULL
RETURNING *;

-- Soft delete
UPDATE processos
SET deleted_at = NOW(), deleted_by = ?
WHERE id = ? AND tenant_id = ?;
```

---

## 💡 Dicas de Implementação

### Task 4 - Repository

1. **Copie a estrutura existente** do `AuthRepository` como base
2. **Use prepared statements** (jdbc/execute!) para prevenir SQL injection
3. **Sempre valide tenant_id** em todas as queries
4. **Retorne mapas Clojure** (keywords) para facilitar uso
5. **Adicione try-catch** para tratamento de erros
6. **Teste cada função** individualmente antes de prosseguir

### Padrão de Implementação

```clojure
(defrecord PostgresRepository [db-spec]
  ProcessoRepository
  
  (find-all-processos [this tenant-id opts]
    (try
      (let [{:keys [page per-page status]} opts
            offset (* (dec page) per-page)]
        (jdbc/execute! db-spec
          ["SELECT ... WHERE tenant_id = ? ..." tenant-id ...]))
      (catch Exception e
        (log/error e "Erro ao listar processos")
        (throw e))))
)
```

---

## ⚠️ Pontos de Atenção

1. **Isolamento Multi-tenant:** Sempre filtrar por `tenant_id`
2. **Soft Delete:** Sempre filtrar `deleted_at IS NULL`
3. **Paginação:** Usar LIMIT e OFFSET em listagens
4. **Auditoria:** Registrar `created_by`, `updated_by`, `deleted_by`
5. **Histórico:** Chamar `add-historico!` após updates
6. **Performance:** Usar índices existentes nas queries
7. **Validação:** Verificar se processo/cliente existe antes de operar

---

## 🎯 Meta da Próxima Sessão

**Objetivo:** Completar Fase 1 (Tasks 4-6)

**Resultado esperado:**
- ✅ Repository PostgreSQL completo (25 funções implementadas)
- ✅ Todas as queries SQL funcionando
- ✅ Testes manuais no REPL passando

**Tempo estimado:** 3-4 horas

---

**Boa sorte! 🚀**

Qualquer dúvida, consulte:
- `RESUMO_SESSAO_04_11_2025.md` - Resumo completo da sessão
- `PROGRESSO_GESTAO_PROCESSOS.md` - Progresso detalhado
- `.kiro/specs/gestao-processos/design.md` - Design técnico completo
