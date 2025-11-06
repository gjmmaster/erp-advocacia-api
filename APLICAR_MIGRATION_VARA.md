# Como Aplicar a Migration de Renomeação da Coluna Vara

## Problema
O frontend está enviando o campo `vara`, mas o banco de dados tem a coluna `vara_tribunal`.

## Solução
Renomear a coluna `vara_tribunal` para `vara` no banco de dados.

## Passos para Aplicar

### Opção 1: Via SQL Direto no CockroachDB Console

1. Acesse o console do CockroachDB
2. Conecte ao seu cluster
3. Execute o seguinte comando SQL:

```sql
BEGIN;
ALTER TABLE processos RENAME COLUMN vara_tribunal TO vara;
COMMIT;
```

### Opção 2: Via Script Shell (se tiver cockroach CLI instalado)

1. Edite o arquivo `run_migration_rename_vara.sh`
2. Configure as variáveis de conexão:
   - `DB_HOST`: Host do seu CockroachDB
   - `DB_PORT`: Porta (geralmente 26257)
   - `DB_NAME`: Nome do banco (geralmente defaultdb)
   - `DB_USER`: Seu usuário

3. Execute o script:
```bash
chmod +x run_migration_rename_vara.sh
./run_migration_rename_vara.sh
```

### Opção 3: Via psql (PostgreSQL client)

```bash
psql "postgresql://user:password@host:26257/defaultdb?sslmode=require" \
  -f migrations/006_rename_vara_tribunal_to_vara.sql
```

## Verificação

Após aplicar a migration, verifique se a coluna foi renomeada:

```sql
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'processos' 
  AND column_name IN ('vara', 'vara_tribunal');
```

Deve retornar apenas `vara`.

## Impacto

- ✅ Nenhum dado será perdido
- ✅ A aplicação continuará funcionando normalmente
- ✅ Não há necessidade de reiniciar o backend

## Rollback (se necessário)

Se precisar reverter:

```sql
BEGIN;
ALTER TABLE processos RENAME COLUMN vara TO vara_tribunal;
COMMIT;
```
