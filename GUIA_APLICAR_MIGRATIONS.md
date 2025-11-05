# Guia: Como Aplicar as Migrations

**Data:** 04/11/2025  
**Objetivo:** Aplicar as migrations das tabelas de processos no CockroachDB

---

## 📋 Pré-requisitos

1. **Acesso ao CockroachDB**
   - Você precisa da URL de conexão do banco
   - Formato: `postgresql://usuario:senha@host:porta/database?sslmode=require`

2. **psql instalado** (cliente PostgreSQL)
   - Windows: https://www.postgresql.org/download/windows/
   - Mac: `brew install postgresql`
   - Linux: `sudo apt-get install postgresql-client`

---

## 🚀 Passo a Passo

### Opção 1: Via Script (Recomendado)

#### 1. Definir a variável de ambiente

**Windows (CMD):**
```cmd
set DATABASE_URL=postgresql://seu_usuario:sua_senha@seu_host:26257/defaultdb?sslmode=require
```

**Windows (PowerShell):**
```powershell
$env:DATABASE_URL="postgresql://seu_usuario:sua_senha@seu_host:26257/defaultdb?sslmode=require"
```

**Mac/Linux:**
```bash
export DATABASE_URL='postgresql://seu_usuario:sua_senha@seu_host:26257/defaultdb?sslmode=require'
```

#### 2. Executar os scripts

**Windows:**
```cmd
REM Aplicar tabela de clientes
run_migration_clientes.sh

REM Aplicar tabelas de processos
run_migration_processos.sh
```

**Mac/Linux:**
```bash
# Dar permissão de execução
chmod +x run_migration_clientes.sh
chmod +x run_migration_processos.sh

# Aplicar tabela de clientes
./run_migration_clientes.sh

# Aplicar tabelas de processos
./run_migration_processos.sh
```

---

### Opção 2: Via psql Direto

Se os scripts não funcionarem, você pode executar diretamente:

#### 1. Conectar ao banco

```bash
psql "postgresql://seu_usuario:sua_senha@seu_host:26257/defaultdb?sslmode=require"
```

#### 2. Executar as migrations

**Migration 004 - Clientes:**
```sql
-- Copie e cole o conteúdo de migrations/004_create_clientes_table.sql
-- Ou execute:
\i migrations/004_create_clientes_table.sql
```

**Migration 005 - Processos:**
```sql
-- Copie e cole o conteúdo de migrations/005_create_processos_tables.sql
-- Ou execute:
\i migrations/005_create_processos_tables.sql
```

---

### Opção 3: Via Interface Web do CockroachDB

1. **Acessar o Console do CockroachDB**
   - Faça login no console web do seu cluster
   - Vá para a aba "SQL"

2. **Executar Migration 004**
   - Abra o arquivo `migrations/004_create_clientes_table.sql`
   - Copie todo o conteúdo
   - Cole no editor SQL do console
   - Clique em "Run"

3. **Executar Migration 005**
   - Abra o arquivo `migrations/005_create_processos_tables.sql`
   - Copie todo o conteúdo
   - Cole no editor SQL do console
   - Clique em "Run"

---

## ✅ Verificar se Funcionou

Após aplicar as migrations, execute estas queries para verificar:

```sql
-- Listar todas as tabelas
SHOW TABLES;

-- Deve mostrar:
-- clientes
-- processos
-- processo_documentos
-- processo_historico
-- (+ tabelas existentes: tenants, users, legal_cases)

-- Verificar estrutura da tabela clientes
SHOW CREATE TABLE clientes;

-- Verificar estrutura da tabela processos
SHOW CREATE TABLE processos;

-- Verificar estrutura da tabela processo_documentos
SHOW CREATE TABLE processo_documentos;

-- Verificar estrutura da tabela processo_historico
SHOW CREATE TABLE processo_historico;

-- Verificar índices criados
SHOW INDEXES FROM processos;
SHOW INDEXES FROM clientes;
```

---

## 🔄 Rollback (Se Necessário)

Se algo der errado, você pode reverter as migrations:

### Rollback Migration 005 (Processos)

```bash
psql "$DATABASE_URL" -f migrations/005_rollback_processos_tables.sql
```

Ou via SQL:
```sql
DROP TABLE IF EXISTS processo_historico;
DROP TABLE IF EXISTS processo_documentos;
DROP TABLE IF EXISTS processos;
```

### Rollback Migration 004 (Clientes)

```bash
psql "$DATABASE_URL" -f migrations/004_rollback_clientes_table.sql
```

Ou via SQL:
```sql
DROP TABLE IF EXISTS clientes;
```

---

## 📝 Conteúdo das Migrations

### Migration 004 - Tabela Clientes

Cria a tabela `clientes` com:
- 10 campos (id, tenant_id, nome, cpf_cnpj, email, telefone, endereco, timestamps)
- Soft delete (deleted_at)
- 3 índices otimizados
- FK para tenants com CASCADE

### Migration 005 - Tabelas de Processos

Cria 3 tabelas:

1. **processos** (20 campos)
   - Dados completos do processo
   - Auditoria (created_by, updated_by, deleted_by)
   - 7 índices
   - Soft delete

2. **processo_documentos** (9 campos)
   - Metadados de arquivos
   - 3 índices
   - FK para processos

3. **processo_historico** (8 campos)
   - Auditoria imutável
   - 3 índices
   - Timeline de eventos

---

## ⚠️ Avisos Importantes

1. **Backup Primeiro**
   - Sempre faça backup do banco antes de aplicar migrations em produção
   - CockroachDB tem backup automático, mas é bom confirmar

2. **Ambiente de Teste**
   - Se possível, teste as migrations em um banco de desenvolvimento primeiro
   - Valide que tudo funciona antes de aplicar em produção

3. **Ordem das Migrations**
   - Execute SEMPRE na ordem: 004 → 005
   - A migration 005 depende da 004 (tabela clientes)

4. **Não Interromper**
   - Deixe as migrations completarem
   - Não feche o terminal durante a execução

5. **Verificar Logs**
   - Os scripts mostram mensagens de sucesso/erro
   - Leia atentamente para identificar problemas

---

## 🐛 Troubleshooting

### Erro: "relation already exists"

**Causa:** Tabela já foi criada anteriormente

**Solução:** 
- Verificar se tabela existe: `SHOW TABLES;`
- Se existe e está vazia, pode dropar e recriar
- Se tem dados, não aplicar a migration novamente

### Erro: "permission denied"

**Causa:** Usuário não tem permissão para criar tabelas

**Solução:**
- Verificar se está usando o usuário correto
- Usuário precisa ter permissão de CREATE TABLE

### Erro: "connection refused"

**Causa:** Não consegue conectar ao banco

**Solução:**
- Verificar se DATABASE_URL está correta
- Verificar se o cluster está online
- Verificar firewall/rede

### Erro: "syntax error"

**Causa:** SQL incompatível ou erro no arquivo

**Solução:**
- Verificar se está usando CockroachDB (não MySQL)
- Verificar se arquivo de migration está completo
- Copiar e colar manualmente via console web

---

## 📞 Precisa de Ajuda?

Se encontrar problemas:

1. **Verificar logs do script**
   - Os scripts mostram mensagens detalhadas
   - Copie a mensagem de erro completa

2. **Verificar console do CockroachDB**
   - Acesse o console web
   - Vá para "SQL Activity" para ver queries executadas
   - Vá para "Databases" para ver tabelas criadas

3. **Testar conexão**
   ```bash
   psql "$DATABASE_URL" -c "SELECT version();"
   ```

---

## ✅ Checklist Final

Antes de fazer deploy do código:

- [ ] Migration 004 aplicada com sucesso
- [ ] Migration 005 aplicada com sucesso
- [ ] Tabelas criadas: clientes, processos, processo_documentos, processo_historico
- [ ] Índices criados corretamente
- [ ] Foreign keys funcionando
- [ ] Queries de teste executadas com sucesso
- [ ] Backup do banco feito (se produção)

---

**Criado em:** 04/11/2025  
**Status:** Pronto para uso  
**Próximo passo:** Aplicar migrations → Deploy do código

