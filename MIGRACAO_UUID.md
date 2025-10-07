# 🔄 Migração de BIGINT para UUID

## Problema Identificado

O JavaScript não consegue lidar com precisão com números BIGINT maiores que `2^53 - 1` (9.007.199.254.740.991). Isso causava perda de precisão nos IDs, fazendo com que:

- **ID no banco**: `1113159344693608449`
- **ID no JavaScript**: `1113159344693608400`

Resultado: Operações de edição e exclusão falhavam com erro 404 (Not Found).

## Solução: Migrar para UUID

UUIDs são strings, então não há perda de precisão no JavaScript.

## Passos para Migração

### 1. Fazer Backup do Banco de Dados

**IMPORTANTE**: Faça backup antes de executar qualquer migração!

```bash
# Se estiver usando CockroachDB/PostgreSQL
pg_dump $DATABASE_URL > backup_antes_migracao.sql
```

### 2. Executar o Script de Migração

Execute o arquivo `migration_bigint_to_uuid.sql` no seu banco de dados:

```bash
psql $DATABASE_URL < migration_bigint_to_uuid.sql
```

Ou conecte ao banco e execute manualmente:

```bash
psql $DATABASE_URL
# Cole o conteúdo do arquivo migration_bigint_to_uuid.sql
```

### 3. Verificar a Migração

Após executar o script, verifique se tudo está correto:

```sql
-- Verificar estrutura das tabelas
\d tenants
\d users
\d legal_cases

-- Verificar alguns registros
SELECT id, company_name FROM tenants LIMIT 3;
SELECT id, email, tenant_id FROM users LIMIT 3;
SELECT id, tenant_id FROM legal_cases LIMIT 3;
```

Os IDs agora devem ser UUIDs como: `550e8400-e29b-41d4-a716-446655440000`

### 4. Deploy do Código Atualizado

O código já foi atualizado para trabalhar com UUIDs. Faça o deploy:

```bash
git add .
git commit -m "Migração: BIGINT para UUID nos IDs"
git push origin feat/clojure-multi-tenant-api
```

### 5. Testar

Após o deploy:

1. Acesse o dashboard do super admin
2. Tente listar os escritórios
3. Tente editar um escritório
4. Tente deletar um escritório

Tudo deve funcionar perfeitamente agora!

## O que foi Alterado no Código

### Backend (Clojure)

**Arquivo**: `src/juridico/api/db/postgres.clj`

- `listar-tenants`: Retorna IDs como strings UUID
- `obter-tenant-por-id`: Aceita UUID string e converte para java.util.UUID
- `atualizar-tenant`: Aceita UUID string e converte para java.util.UUID
- `deletar-tenant`: Aceita UUID string e converte para java.util.UUID

**Arquivo**: `src/juridico/api/handlers.clj`

- Removido `Long/parseLong` dos handlers de tenant
- IDs agora são tratados como strings UUID

### Frontend (React)

Nenhuma alteração necessária! O frontend já trabalhava com IDs como strings.

## Rollback

Se algo der errado e você precisar voltar:

1. Restaure o backup:
```bash
psql $DATABASE_URL < backup_antes_migracao.sql
```

2. Reverta o código:
```bash
git revert HEAD
git push origin feat/clojure-multi-tenant-api
```

## Benefícios da Migração

✅ **Sem perda de precisão**: UUIDs são strings, JavaScript lida perfeitamente  
✅ **Padrão da indústria**: UUIDs são amplamente usados em APIs REST  
✅ **Segurança**: UUIDs são mais difíceis de adivinhar que IDs sequenciais  
✅ **Escalabilidade**: UUIDs podem ser gerados em qualquer lugar sem conflitos  

## Notas Importantes

- **CockroachDB** suporta UUIDs nativamente com `gen_random_uuid()`
- **PostgreSQL** também suporta UUIDs nativamente
- UUIDs ocupam 16 bytes vs 8 bytes do BIGINT, mas a diferença é negligível
- A migração preserva todos os dados e relacionamentos

## Suporte

Se tiver problemas durante a migração:

1. Verifique os logs do Render
2. Verifique se o script foi executado completamente
3. Verifique se há erros no console do navegador
4. Se necessário, restaure o backup e tente novamente

---

**Data da Migração**: 07/10/2025  
**Versão**: 1.0  
**Status**: Pronto para execução
