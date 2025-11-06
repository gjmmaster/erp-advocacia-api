# Como Fazer Rollback para Versões Estáveis

## Versões Estáveis Disponíveis

### v1.0.0-gestao-processos (06/11/2025)
**Tag Git:** `v1.0.0-gestao-processos`  
**Commit:** `0e52581`  
**Branch:** `feat/clojure-multi-tenant-api`

**Funcionalidades:**
- ✅ Gestão completa de clientes (CRUD)
- ✅ Gestão completa de processos (CRUD)
- ✅ Soft delete com auditoria
- ✅ Histórico de alterações
- ✅ Paginação e filtros
- ✅ Interface completa no frontend

**Migrations Necessárias:**
- 001 até 006

---

## Como Fazer Rollback

### Opção 1: Rollback via Tag (Recomendado)

```bash
# 1. Ver todas as tags disponíveis
git tag -l

# 2. Fazer checkout da tag desejada
git checkout v1.0.0-gestao-processos

# 3. Criar uma nova branch a partir desta versão (opcional)
git checkout -b rollback-v1.0.0

# 4. Se quiser forçar o branch principal para esta versão
git checkout feat/clojure-multi-tenant-api
git reset --hard v1.0.0-gestao-processos
git push origin feat/clojure-multi-tenant-api --force
```

### Opção 2: Rollback via Commit Hash

```bash
# 1. Ver histórico de commits
git log --oneline

# 2. Fazer checkout do commit desejado
git checkout 0e52581

# 3. Criar uma nova branch a partir deste commit
git checkout -b rollback-gestao-processos

# 4. Se quiser forçar o branch principal
git checkout feat/clojure-multi-tenant-api
git reset --hard 0e52581
git push origin feat/clojure-multi-tenant-api --force
```

### Opção 3: Reverter Commits Específicos

```bash
# Reverter o último commit (mantém histórico)
git revert HEAD

# Reverter múltiplos commits
git revert HEAD~3..HEAD

# Push das reversões
git push origin feat/clojure-multi-tenant-api
```

---

## Rollback do Banco de Dados

### Importante ⚠️

O rollback de código **NÃO** reverte automaticamente as migrations do banco de dados. Você precisa fazer isso manualmente.

### Para v1.0.0-gestao-processos

Esta versão requer migrations 001 até 006. Se você fez rollback para uma versão anterior, pode precisar reverter migrations.

#### Reverter Migration 006 (Renomear vara)

```sql
ALTER TABLE processos RENAME COLUMN vara TO vara_tribunal;
```

#### Reverter Migration 005 (Tabelas de processos)

```sql
DROP TABLE IF EXISTS processo_historico CASCADE;
DROP TABLE IF EXISTS processo_documentos CASCADE;
DROP TABLE IF EXISTS processos CASCADE;
```

#### Reverter Migration 004 (Tabela de clientes)

```sql
DROP TABLE IF EXISTS clientes CASCADE;
```

**⚠️ ATENÇÃO:** Reverter migrations apaga dados! Faça backup antes!

---

## Verificar Versão Atual

### No Git

```bash
# Ver commit atual
git log -1 --oneline

# Ver tags no commit atual
git describe --tags

# Ver branch atual
git branch --show-current
```

### No Banco de Dados

```sql
-- Ver todas as tabelas
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Ver colunas de uma tabela específica
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'processos';
```

---

## Processo Completo de Rollback

### 1. Backup

```bash
# Fazer backup do código atual
git branch backup-$(date +%Y%m%d)

# Fazer backup do banco (via CockroachDB Console)
# Exportar dados importantes antes de reverter migrations
```

### 2. Rollback do Código

```bash
# Checkout da versão estável
git checkout v1.0.0-gestao-processos

# Criar branch de rollback
git checkout -b rollback-v1.0.0-$(date +%Y%m%d)
```

### 3. Rollback do Banco (se necessário)

Execute os comandos SQL de reversão apropriados no CockroachDB Console.

### 4. Deploy

```bash
# Push da versão de rollback
git push origin rollback-v1.0.0-$(date +%Y%m%d)

# Ou forçar o branch principal (cuidado!)
git checkout feat/clojure-multi-tenant-api
git reset --hard v1.0.0-gestao-processos
git push origin feat/clojure-multi-tenant-api --force
```

### 5. Verificação

1. Verificar se o deploy foi bem-sucedido no Render
2. Testar funcionalidades críticas
3. Verificar logs de erro
4. Confirmar que o banco está consistente

---

## Boas Práticas

### ✅ Fazer

- Sempre criar backup antes de rollback
- Documentar o motivo do rollback
- Testar em ambiente de desenvolvimento primeiro
- Comunicar a equipe sobre o rollback
- Verificar dependências entre código e banco

### ❌ Não Fazer

- Fazer rollback sem backup
- Reverter migrations sem entender o impacto
- Usar `--force` sem necessidade
- Fazer rollback em produção sem testar

---

## Troubleshooting

### Problema: "Detached HEAD state"

```bash
# Você está em um commit específico, não em um branch
# Solução: Criar um branch
git checkout -b nome-do-branch
```

### Problema: "Conflicts during rollback"

```bash
# Resolver conflitos manualmente
git status
# Editar arquivos conflitantes
git add .
git revert --continue
```

### Problema: "Migration incompatível"

```bash
# Verificar qual migration está aplicada
SELECT * FROM schema_migrations; -- se você tiver esta tabela

# Aplicar ou reverter migrations conforme necessário
```

---

## Contato

Para dúvidas sobre rollback:
- Consulte a documentação em `RELEASE_v1.0.0_GESTAO_PROCESSOS.md`
- Abra uma issue no GitHub
- Contate o time de desenvolvimento

---

**Última Atualização:** 06/11/2025  
**Mantido por:** Equipe de Desenvolvimento
