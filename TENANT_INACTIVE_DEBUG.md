# Debug: Erro "Escritório inativo"

## 🐛 Problema

Ao tentar fazer login com um tenant recém-criado, o sistema retorna erro 403:

```
[TENANT LOGIN] Resposta do backend: 403
[TENANT LOGIN] Erro do backend: { error: 'Escritório inativo.' }
```

## 🔍 Investigação

### Código do Handler

```clojure
(if (:tenant_active user)
  ;; Login permitido
  ...
  ;; Tenant inativo
  {:status 403 :body {:error "Escritório inativo."}})
```

### Query SQL

```sql
SELECT u.*, t.company_name as tenant_name, t.is_active as tenant_active
FROM users u
JOIN tenants t ON u.tenant_id = t.id
WHERE LOWER(u.email) = LOWER(?)
```

## 🤔 Possíveis Causas

1. **Namespace no resultado:** O campo pode estar vindo como `:tenants/is_active` em vez de `:tenant_active`
2. **Valor NULL:** O campo `is_active` pode estar NULL no banco
3. **Tipo de dado:** O campo pode estar vindo como string "true" em vez de boolean

## 🔧 Debug Adicionado

Adicionados logs para verificar:
```clojure
(println "[POSTGRES] encontrar-usuario-por-email-global result:" result)
(println "[POSTGRES] tenant_active value:" (:tenant_active result))
(println "[POSTGRES] tenants/is_active value:" (:tenants/is_active result))
```

## 🧪 Próximos Passos

1. Aguardar deploy
2. Tentar fazer login novamente
3. Verificar logs do backend
4. Identificar qual campo está sendo retornado
5. Corrigir o código conforme necessário

---

**Commit:** `f832991`  
**Data:** 29/10/2025  
**Status:** 🔍 INVESTIGANDO
