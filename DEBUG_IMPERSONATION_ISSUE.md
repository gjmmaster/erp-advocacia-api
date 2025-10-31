# Debug: Problema com Rota de Master User

## Problema Atual
A rota `/admin/tenants/:id/master-user` não está sendo encontrada no backend.

## Evidências
1. Frontend faz requisição para `/api/admin/tenants/1120204013871398913/master-user`
2. Middleware do frontend passa a requisição para o backend
3. Backend retorna "ROTA NÃO ENCONTRADA" com `URI: /` (vazio)
4. Handler `get-tenant-master-user-handler` nunca é chamado (não há logs)

## Configuração Atual

### Backend (core.clj)
```clojure
["/admin"
 ["/tenants/:id/master-user" {:middleware [...]
                              :get {:handler h/get-tenant-master-user-handler}}]
 ["/tenants/:id" {:middleware [...]
                  :get {:handler h/obter-tenant-handler}
                  :put {:handler h/atualizar-tenant-handler}
                  :delete {:handler h/deletar-tenant-handler}}]]
```

### Frontend (route.ts)
```typescript
const response = await fetch(`${API_URL}/admin/tenants/${params.id}/master-user`, {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  },
});
```

## Possíveis Causas
1. ❌ Sintaxe de rota incorreta no Reitit
2. ❌ Ordem das rotas (rota específica deve vir antes)
3. ❌ Middleware bloqueando a requisição
4. ❌ CORS ou problema de rede
5. ❌ Problema com o not-found-handler

## Próximos Passos
1. Verificar se o backend está recebendo a requisição corretamente
2. Adicionar logs no início do router para ver todas as rotas registradas
3. Testar a rota diretamente com curl/Postman
4. Verificar se há conflito com outras rotas

## Workaround Temporário
Podemos implementar uma solução alternativa:
- Buscar todos os usuários do tenant e filtrar pelo role "master" no frontend
- Ou adicionar o master_user_id diretamente na tabela tenants
