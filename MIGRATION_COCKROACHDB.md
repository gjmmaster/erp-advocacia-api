# 🐛 Migration para CockroachDB - Troca de Senha Temporária

**Data:** 30 de Outubro de 2025  
**Feature:** Adicionar coluna `temporary_password`  
**Banco:** CockroachDB

---

## 🚀 Executar Migration no CockroachDB Console

### Opção 1: Via CockroachDB Cloud Console (Recomendado)

1. **Acessar o Console:**
   - Acesse: https://cockroachlabs.cloud/
   - Faça login na sua conta
   - Selecione seu cluster

2. **Abrir SQL Shell:**
   - Clique em "SQL Shell" no menu lateral
   - OU clique em "Connect" → "SQL Shell"

3. **Executar os Comandos SQL:**

```sql
-- 1. Adicionar coluna temporary_password
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS temporary_password BOOLEAN DEFAULT false;

-- 2. Criar índice para performance
CREATE INDEX IF NOT EXISTS idx_users_temporary_password 
ON users(temporary_password) 
WHERE temporary_password = true;

-- 3. Verificar que coluna foi criada
SHOW COLUMNS FROM users;

-- 4. Verificar que índice foi criado
SHOW INDEXES FROM users;

-- 5. Contar usuários com senha temporária (deve ser 0 inicialmente)
SELECT COUNT(*) as users_with_temp_password 
FROM users 
WHERE temporary_password = true;
```

4. **Validar:**
   - Você deve ver a coluna `temporary_password` na lista
   - Você deve ver o índice `idx_users_temporary_password`
   - A contagem deve ser 0

---

### Opção 2: Via Linha de Comando (cockroach sql)

Se você tem o CLI do CockroachDB instalado:

```bash
# Conectar ao cluster
cockroach sql --url "sua-connection-string"

# Executar os comandos SQL acima
```

---

### Opção 3: Copiar e Colar Tudo de Uma Vez

Copie e cole este bloco completo no SQL Shell:

```sql
-- Migration: Add temporary_password column
-- Feature: Force Password Change on First Login
-- Date: 2025-10-30

BEGIN;

-- Add column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS temporary_password BOOLEAN DEFAULT false;

-- Create index
CREATE INDEX IF NOT EXISTS idx_users_temporary_password 
ON users(temporary_password) 
WHERE temporary_password = true;

-- Verify
SELECT 
    column_name, 
    data_type, 
    column_default, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name = 'temporary_password';

COMMIT;
```

---

## ✅ Verificação Pós-Migration

Execute estes comandos para confirmar que tudo funcionou:

```sql
-- 1. Ver estrutura da tabela users
SHOW COLUMNS FROM users;
-- Deve mostrar: temporary_password | BOOL | false | YES

-- 2. Ver índices da tabela users
SHOW INDEXES FROM users;
-- Deve mostrar: idx_users_temporary_password

-- 3. Testar inserção (opcional)
-- Criar usuário de teste com senha temporária
INSERT INTO users (tenant_id, email, password_hash, role, temporary_password)
VALUES (1, 'test@example.com', 'hash-aqui', 'master', true);

-- Verificar
SELECT id, email, temporary_password FROM users WHERE email = 'test@example.com';

-- Deletar teste (se criou)
DELETE FROM users WHERE email = 'test@example.com';
```

---

## 🔄 Rollback (Se Necessário)

Se algo der errado e você precisar reverter:

```sql
-- Remover índice
DROP INDEX IF EXISTS idx_users_temporary_password;

-- Remover coluna
ALTER TABLE users DROP COLUMN IF EXISTS temporary_password;
```

---

## 📝 Comandos Úteis do CockroachDB

```sql
-- Ver todas as tabelas
SHOW TABLES;

-- Ver estrutura completa da tabela users
SHOW CREATE TABLE users;

-- Ver todos os índices
SHOW INDEXES FROM users;

-- Ver informações do banco
SELECT version();

-- Ver conexões ativas
SHOW SESSIONS;
```

---

## 🎯 Próximos Passos Após Migration

1. **Verificar que migration funcionou:**
   ```sql
   SHOW COLUMNS FROM users;
   ```

2. **Testar criação de tenant:**
   - Acesse o super admin
   - Crie novo tenant
   - Verifique que `temporary_password = true`

3. **Testar fluxo completo:**
   - Fazer login com senha temporária
   - Verificar redirecionamento para `/change-password`
   - Trocar senha
   - Verificar que `temporary_password = false`

---

## 🐛 Troubleshooting

### Erro: "column already exists"

```sql
-- Verificar se coluna já existe
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name = 'temporary_password';

-- Se existir, pode pular a migration ou dropar e recriar
ALTER TABLE users DROP COLUMN temporary_password;
-- Depois executar migration novamente
```

### Erro: "index already exists"

```sql
-- Verificar índices existentes
SHOW INDEXES FROM users;

-- Se existir, pode pular ou dropar e recriar
DROP INDEX idx_users_temporary_password;
-- Depois executar migration novamente
```

### Erro de Permissão

Se você receber erro de permissão:
- Verifique que está usando o usuário correto (admin)
- Verifique que tem permissões de ALTER TABLE
- Entre em contato com o administrador do cluster

---

## 📊 Monitoramento

Após migration, você pode monitorar:

```sql
-- Quantos usuários têm senha temporária
SELECT COUNT(*) as total_temp_passwords
FROM users 
WHERE temporary_password = true;

-- Listar usuários com senha temporária
SELECT id, email, role, created_at
FROM users 
WHERE temporary_password = true
ORDER BY created_at DESC;

-- Quantos usuários já trocaram senha
SELECT COUNT(*) as total_changed
FROM users 
WHERE temporary_password = false
AND role IN ('master', 'operador');
```

---

## 🔒 Segurança

**Importante:**
- ✅ Migration é segura (apenas adiciona coluna)
- ✅ Não afeta dados existentes
- ✅ Não causa downtime
- ✅ Pode ser revertida facilmente
- ✅ Índice melhora performance

**Recomendações:**
- Fazer backup antes (CockroachDB faz automaticamente)
- Executar em horário de baixo tráfego (se possível)
- Monitorar logs após migration
- Testar com tenant de teste primeiro

---

## 📞 Suporte

### CockroachDB
- Documentação: https://www.cockroachlabs.com/docs/
- Console: https://cockroachlabs.cloud/
- Suporte: support@cockroachlabs.com

### Projeto
- Ver: `IMPLEMENTACAO_COMPLETA_FORCE_PASSWORD.md`
- Ver: `CHECKPOINT_FORCE_PASSWORD_CHANGE.md`

---

## ✅ Checklist

- [ ] Acessei CockroachDB Console
- [ ] Abri SQL Shell
- [ ] Executei comando ALTER TABLE
- [ ] Executei comando CREATE INDEX
- [ ] Verifiquei com SHOW COLUMNS
- [ ] Verifiquei com SHOW INDEXES
- [ ] Testei criação de tenant
- [ ] Testei fluxo de troca de senha
- [ ] Verifiquei que temporary_password muda para false

---

**Migration pronta para executar! 🚀**

**Tempo estimado:** 2-3 minutos

