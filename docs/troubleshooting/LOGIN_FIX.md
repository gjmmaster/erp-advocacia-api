# Fix: Erro ao Fazer Login de Tenant

## 🐛 Problema

Ao tentar fazer login com um tenant recém-criado, o sistema retornava erro 500:

```
ERROR: column "t.active" does not exist
```

## 🔍 Causa Raiz

A query SQL em `encontrar-usuario-por-email-global` estava usando o nome de coluna **errado**:

```sql
-- ANTES (ERRADO)
SELECT u.*, t.company_name as tenant_name, t.active as tenant_active
FROM users u
JOIN tenants t ON u.tenant_id = t.id
WHERE LOWER(u.email) = LOWER(?)
```

A coluna na tabela `tenants` é `is_active`, não `active`.

## ✅ Solução

Corrigido o nome da coluna:

```sql
-- DEPOIS (CORRETO)
SELECT u.*, t.company_name as tenant_name, t.is_active as tenant_active
FROM users u
JOIN tenants t ON u.tenant_id = t.id
WHERE LOWER(u.email) = LOWER(?)
```

## 📝 Stack Trace

```
org.postgresql.util.PSQLException: ERROR: column "t.active" does not exist
at juridico.api.db.postgres.PostgresRepository.encontrar_usuario_por_email_global(postgres.clj:73)
at juridico.api.handlers$login_auto_discover_handler.invokeStatic(handlers.clj:171)
```

## 🚀 Resultado

Agora o login de tenants funciona corretamente:
1. ✅ Auto-descoberta do tenant pelo email
2. ✅ Validação de senha
3. ✅ Geração de JWT
4. ✅ Redirecionamento para dashboard

## 🧪 Como Testar

1. Aguarde o deploy no Render (2-3 minutos)
2. Acesse: https://erp-advocacia-frontend.onrender.com/login
3. Use o email e senha do tenant criado:
   - Email: jmmaster.dev@gmail.com
   - Senha: KZM1bYZ2YVu7
4. **Verifique que o login funciona!**

---

**Commit:** `ba4e89d`  
**Data:** 29/10/2025  
**Arquivo modificado:** `src/juridico/api/db/postgres.clj` (linha 74)
