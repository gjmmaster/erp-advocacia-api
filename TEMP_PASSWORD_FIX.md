# Fix: Senha Temporária Não Aparecia no Modal

## 🐛 Problema

Quando o super admin criava um novo tenant, o modal exibia "N/A" em vez da senha temporária real.

## 🔍 Causa Raiz

A rota POST `/admin/tenants` estava chamando o handler **errado**:

```clojure
; ANTES (ERRADO)
["/tenants" {:post {:handler h/criar-tenant-handler}}]
```

O handler `criar-tenant-handler` **não existe** e estava retornando apenas os dados do tenant, sem o usuário e sem a senha temporária.

## ✅ Solução

Corrigido para usar o handler correto `provision-tenant-handler`:

```clojure
; DEPOIS (CORRETO)
["/tenants" {:post {:handler h/provision-tenant-handler}}]
```

O `provision-tenant-handler` retorna:
```json
{
  "tenant": {...},
  "user": {
    "email": "...",
    "temp_password": "..."
  },
  "temp_password": "...",  // ← Campo no nível raiz
  "email_sent": false,
  "message": "Tenant criado com sucesso."
}
```

## 📝 Logs que Identificaram o Problema

```
[API TENANTS POST] Resposta do backend: {
  "tenants/id": 1119670318710947800,
  "tenants/company_name": "TESTEHOJE",
  "tenants/subdomain": "testehoje",
  ...
}
[API TENANTS POST] data.temp_password: undefined  ← SEM SENHA!
[API TENANTS POST] data.user: undefined           ← SEM USUÁRIO!
```

## 🚀 Resultado

Agora quando criar um tenant, o modal exibirá:
- ✅ Nome do escritório
- ✅ Email do admin
- ✅ **Senha temporária visível**
- ✅ Botão "Copiar Senha"
- ✅ Aviso se email não foi enviado

## 🧪 Como Testar

1. Aguarde o deploy no Render (2-3 minutos)
2. Acesse: https://erp-advocacia-frontend.onrender.com/super-admin/dashboard
3. Clique em "Provisionar Novo Escritório"
4. Preencha os dados e crie
5. **Verifique que a senha aparece no modal!**

---

**Commit:** `b470649`  
**Data:** 29/10/2025  
**Arquivo modificado:** `src/juridico/api/core.clj`
