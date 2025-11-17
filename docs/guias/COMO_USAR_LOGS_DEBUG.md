# Como Usar os Logs de Debug

## 📋 Logs Adicionados

Adicionamos logs detalhados em todos os pontos críticos do fluxo de clientes:

### 1. Middleware de Autenticação (`middleware.clj`)
```
=== [MIDDLEWARE] wrap-jwt-authentication INICIADO ===
[MIDDLEWARE] URI: /api/tenant/clientes
[MIDDLEWARE] Method: :post
[MIDDLEWARE] ✅ Token encontrado: eyJhbGciOiJIUzI1NiI...
[MIDDLEWARE] ✅ Token decodificado com sucesso
[MIDDLEWARE] Claims: {:tenant-id 123, :user-id 456, :role "master"}
[MIDDLEWARE] tenant-id: 123
[MIDDLEWARE] role: master
[MIDDLEWARE] Criando repo com tenant-id: 123
[MIDDLEWARE] ✅ Request preparado, chamando handler...
```

### 2. Handler de Listagem (`handlers/processos.clj`)
```
=== [HANDLER] list-clientes-handler INICIADO ===
[HANDLER] identity: {:tenant-id 123, :user-id 456}
[HANDLER] query-params: {page 1, per-page 20}
[HANDLER] tenant-id: 123
[HANDLER] opts: {:page 1, :per-page 20, :search nil}
[HANDLER] ✅ Clientes listados com sucesso
[HANDLER] Total encontrado: 5
```

### 3. Handler de Criação (`handlers/processos.clj`)
```
=== [HANDLER] create-cliente-handler INICIADO ===
[HANDLER] identity: {:tenant-id 123, :user-id 456}
[HANDLER] body-params: {:nome "João Silva", :cpf_cnpj "12345678901"}
[HANDLER] tenant-id extraído: 123
[HANDLER] nome: João Silva
[HANDLER] cpf_cnpj: 12345678901
[HANDLER] ✅ Validações OK, criando cliente...
[HANDLER] cliente-data preparado: {:nome "João Silva", :tenant_id 123}
[HANDLER] ✅ Cliente criado com sucesso!
[HANDLER] result: {:clientes/id 789, :clientes/nome "João Silva"}
```

### 4. Método de Listagem no DB (`db/postgres.clj`)
```
=== [DB] find-all-clientes INICIADO ===
[DB] tenant-id: 123
[DB] opts: {:page 1, :per-page 20, :search nil}
[DB] page: 1 per-page: 20 offset: 0
[DB] Sem filtro de busca
[DB] Executando COUNT query...
[DB] Total de clientes: 5
[DB] Executando SELECT query...
[DB] ✅ Query executada com sucesso!
[DB] Clientes retornados: 5
```

### 5. Método de Criação no DB (`db/postgres.clj`)
```
=== [DB] create-cliente! INICIADO ===
[DB] cliente-data recebido: {:nome "João Silva", :tenant_id 123}
[DB] tenant_id: 123
[DB] nome: João Silva
[DB] cpf_cnpj: 12345678901
[DB] email: joao@email.com
[DB] ✅ INSERT executado com sucesso!
[DB] Cliente criado com ID: 789
```

## 🔍 Como Visualizar os Logs

### No Render (Produção)

1. Acesse o dashboard do Render
2. Clique no serviço `erp-advocacia-api`
3. Vá na aba **"Logs"**
4. Os logs aparecem em tempo real

### Localmente

Se estiver rodando o backend localmente:
```bash
lein run
```

Os logs aparecerão no terminal.

## 🐛 Como Debugar um Problema

### Exemplo: Cliente não está sendo criado

1. **Tente criar um cliente no frontend**
2. **Vá nos logs do Render**
3. **Procure pela sequência de logs:**

```
=== [MIDDLEWARE] wrap-jwt-authentication INICIADO ===
```

Se você NÃO vê este log, o problema é:
- ❌ Frontend não está enviando a requisição
- ❌ Rota não existe
- ❌ Middleware não está sendo chamado

Se você vê este log mas ele para em:
```
[MIDDLEWARE] ❌ Token não encontrado no header Authorization
```

O problema é:
- ❌ Frontend não está enviando o token
- ❌ Cookie não está sendo lido corretamente

Se você vê:
```
[MIDDLEWARE] ✅ Request preparado, chamando handler...
=== [HANDLER] create-cliente-handler INICIADO ===
```

Mas para aqui, o problema é:
- ❌ Handler está travando
- ❌ Alguma validação está falhando

Se você vê:
```
[HANDLER] ✅ Validações OK, criando cliente...
=== [DB] create-cliente! INICIADO ===
```

Mas para aqui, o problema é:
- ❌ Erro no banco de dados
- ❌ Tabela não existe
- ❌ Permissões incorretas

Se você vê:
```
[DB] ❌ EXCEÇÃO no INSERT:
[DB] Mensagem: relation "clientes" does not exist
```

O problema é:
- ❌ Tabela não foi criada no banco
- ❌ Migration não foi aplicada

## 📊 Checklist de Debug

Use este checklist para identificar onde o código está travando:

- [ ] **Passo 1:** Logs do middleware aparecem?
  - ✅ Sim → Vá para Passo 2
  - ❌ Não → Problema na rota ou frontend

- [ ] **Passo 2:** Token foi decodificado?
  - ✅ Sim → Vá para Passo 3
  - ❌ Não → Problema de autenticação

- [ ] **Passo 3:** Handler foi chamado?
  - ✅ Sim → Vá para Passo 4
  - ❌ Não → Problema no roteamento

- [ ] **Passo 4:** Validações passaram?
  - ✅ Sim → Vá para Passo 5
  - ❌ Não → Dados inválidos

- [ ] **Passo 5:** Método do DB foi chamado?
  - ✅ Sim → Vá para Passo 6
  - ❌ Não → Problema no handler

- [ ] **Passo 6:** Query SQL foi executada?
  - ✅ Sim → Sucesso! 🎉
  - ❌ Não → Problema no banco de dados

## 🎯 Próximos Passos

Agora que os logs estão ativos:

1. **Tente cadastrar um cliente no frontend**
2. **Copie TODOS os logs que aparecem no Render**
3. **Me envie os logs**
4. **Vou identificar exatamente onde está travando**

---

**Commit:** `13a17bb`  
**Data:** 05/11/2025  
**Autor:** Kiro AI
