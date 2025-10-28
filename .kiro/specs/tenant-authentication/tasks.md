# Implementation Plan - Autenticação e Dashboard de Tenants

**Data:** 24 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Pronto para Implementação

---

## Overview

Este documento contém a lista de tarefas para implementar a autenticação e dashboard de tenants. As tarefas estão organizadas em ordem de execução e cada uma referencia os requisitos correspondentes.

---

## Tasks

- [x] 1. Backend - Endpoint de busca de tenant por subdomínio



  - Criar função `get-tenant-by-subdomain` no repository
  - Criar handler `get-tenant-by-subdomain-handler`
  - Adicionar rota `GET /api/tenants/by-subdomain/:subdomain`
  - Testar endpoint com curl



  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 2. Backend - Atualizar endpoint de login para validar tenant
  - Modificar `login-handler` para aceitar `subdomain` no body
  - Adicionar validação de que usuário pertence ao tenant



  - Adicionar validação de tenant ativo
  - Retornar erro específico se usuário não pertence ao tenant
  - Testar login com usuário de outro tenant
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 3. Backend - Endpoint de estatísticas do dashboard
  - Criar funções no repository:
    - `count-processos`
    - `count-clientes`
    - `count-operadores`



    - `count-processos-ativos`
  - Criar handler `get-dashboard-stats-handler`
  - Adicionar rota `GET /api/dashboard/stats/:tenant-id`
  - Adicionar middleware de autenticação na rota
  - Validar que usuário pertence ao tenant solicitado
  - Testar endpoint com token válido
  - _Requirements: 4.2, 4.3_

- [ ] 4. Frontend - Atualizar middleware para extrair subdomínio
  - Criar função `extractSubdomain` no middleware


  - Criar função `validateTenant` que chama backend
  - Adicionar lógica para redirecionar se não tem subdomínio
  - Adicionar headers `x-tenant-id` e `x-tenant-subdomain`
  - Tratar erro 404 (tenant não encontrado)
  - Tratar erro 403 (tenant inativo)
  - Testar com diferentes subdomínios
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 5. Frontend - Criar tipos TypeScript
  - Criar interface `Tenant` em `src/types/tenant.ts`
  - Criar interface `UserSession` em `src/types/auth.ts`
  - Criar interface `DashboardStats` em `src/types/dashboard.ts`
  - _Requirements: Todos_

- [ ] 6. Frontend - Página de login do tenant
  - Criar estrutura de pastas `src/app/[subdomain]/login/`
  - Criar `page.tsx` com formulário de login
  - Criar `login.module.css` com estilos
  - Adicionar validação client-side (e-mail, senha obrigatórios)
  - Adicionar estado de loading
  - Adicionar exibição de erros
  - Redirecionar para dashboard após login bem-sucedido
  - _Requirements: 2.1, 2.2, 2.3, 7.1, 7.2, 7.3_

- [ ] 7. Frontend - BFF API route de login
  - Criar `src/app/api/tenant/login/route.ts`
  - Extrair subdomain e tenant-id dos headers
  - Fazer requisição ao backend com subdomain
  - Validar que tenant-id do token corresponde ao subdomínio
  - Armazenar token em cookie HttpOnly usando `setSession`
  - Retornar dados do usuário (sem token)
  - Tratar erros apropriadamente
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

- [ ] 8. Frontend - Layout do tenant
  - Criar `src/components/tenant/DashboardLayout.tsx`
  - Adicionar header com nome do escritório
  - Adicionar menu de navegação lateral/superior
  - Adicionar botão de logout
  - Diferenciar menu para admin vs operador
  - Adicionar estilos responsivos
  - _Requirements: 4.3, 4.4, 4.5, 8.1, 8.2, 8.3, 8.4_

- [ ] 9. Frontend - Página de dashboard
  - Criar `src/app/[subdomain]/dashboard/page.tsx`
  - Verificar autenticação com `getSession`
  - Redirecionar para login se não autenticado
  - Renderizar layout com DashboardLayout
  - Renderizar componente DashboardStats
  - _Requirements: 4.1, 4.2, 6.1, 6.2, 6.3_

- [ ] 10. Frontend - Componente de estatísticas
  - Criar `src/components/tenant/DashboardStats.tsx`
  - Criar `DashboardStats.module.css`
  - Fazer requisição para `/api/tenant/dashboard/stats`
  - Exibir 4 cards com métricas
  - Adicionar skeleton loaders
  - Tratar erros de carregamento
  - _Requirements: 4.2, 7.4_

- [ ] 11. Frontend - BFF API route de stats
  - Criar `src/app/api/tenant/dashboard/stats/route.ts`
  - Verificar autenticação com `getSession`
  - Extrair tenant-id da sessão
  - Fazer requisição ao backend com Authorization header
  - Retornar dados das estatísticas
  - Tratar erros apropriadamente
  - _Requirements: 4.2, 6.1, 6.2, 6.3, 6.4_

- [ ] 12. Frontend - Funcionalidade de logout
  - Criar `src/app/api/tenant/logout/route.ts`
  - Remover cookie de autenticação usando `clearSession`
  - Retornar sucesso
  - Adicionar botão de logout no layout
  - Redirecionar para login após logout
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [ ] 13. Frontend - Atualizar middleware para proteger rotas do tenant
  - Adicionar validação de autenticação para rotas `/[subdomain]/dashboard`
  - Verificar que tenant-id do token corresponde ao subdomínio
  - Redirecionar para login se não autenticado
  - Retornar 403 se tenant-id não corresponde
  - Adicionar matcher para rotas do tenant
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 14. Testes - Backend
  - Testar endpoint de busca de tenant por subdomínio
  - Testar login com subdomain válido
  - Testar login com usuário de outro tenant
  - Testar endpoint de stats com autenticação
  - Testar endpoint de stats sem autenticação
  - _Requirements: Todos_

- [ ] 15. Testes - Frontend
  - Testar extração de subdomínio do middleware
  - Testar página de login (formulário, validação, erros)
  - Testar fluxo completo de login
  - Testar carregamento de dashboard
  - Testar logout
  - Testar proteção de rotas
  - _Requirements: Todos_

- [ ] 16. Testes - Integração
  - Testar fluxo completo: acesso via subdomínio → login → dashboard → logout
  - Testar acesso com subdomínio inválido
  - Testar acesso com tenant inativo
  - Testar acesso de usuário de outro tenant
  - Testar expiração de token
  - _Requirements: Todos_

- [ ] 17. Documentação
  - Criar README.md na pasta da spec
  - Documentar como testar localmente com subdomínios
  - Documentar configuração de DNS/hosts
  - Documentar variáveis de ambiente necessárias
  - _Requirements: N/A_

- [ ] 18. Deploy e Validação
  - Configurar wildcard DNS (*.seudominio.com)
  - Fazer deploy do backend atualizado
  - Fazer deploy do frontend atualizado
  - Testar em produção com tenant real
  - Validar todos os fluxos em produção
  - _Requirements: Todos_

---

## Ordem de Execução Recomendada

### Fase 1: Backend (Tasks 1-3)
**Tempo estimado:** 1 dia

1. Endpoint de busca de tenant
2. Atualizar login para validar tenant
3. Endpoint de estatísticas

### Fase 2: Frontend - Infraestrutura (Tasks 4-5)
**Tempo estimado:** 0.5 dia

4. Atualizar middleware
5. Criar tipos TypeScript

### Fase 3: Frontend - Login (Tasks 6-7)
**Tempo estimado:** 1 dia

6. Página de login
7. BFF API route de login

### Fase 4: Frontend - Dashboard (Tasks 8-11)
**Tempo estimado:** 1.5 dias

8. Layout do tenant
9. Página de dashboard
10. Componente de estatísticas
11. BFF API route de stats

### Fase 5: Frontend - Logout e Proteção (Tasks 12-13)
**Tempo estimado:** 0.5 dia

12. Funcionalidade de logout
13. Proteção de rotas

### Fase 6: Testes (Tasks 14-16)
**Tempo estimado:** 1 dia

14. Testes backend
15. Testes frontend
16. Testes de integração

### Fase 7: Documentação e Deploy (Tasks 17-18)
**Tempo estimado:** 0.5 dia

17. Documentação
18. Deploy e validação

**Tempo Total Estimado:** 6-7 dias

---

## Notas de Implementação

### Testando Localmente com Subdomínios

Para testar localmente, você precisa configurar subdomínios no arquivo hosts:

**Windows:** `C:\Windows\System32\drivers\etc\hosts`
**Mac/Linux:** `/etc/hosts`

```
127.0.0.1 escritorio-silva.localhost
127.0.0.1 escritorio-santos.localhost
```

Depois acesse: `http://escritorio-silva.localhost:3001`

### Variáveis de Ambiente

**Backend:**
```bash
JWT_SECRET=<secret-forte>
DATABASE_URL=<cockroachdb-url>
```

**Frontend:**
```bash
NEXT_PUBLIC_DOMAIN=localhost  # ou seudominio.com em produção
BACKEND_URL=http://localhost:3000
JWT_SECRET=<mesmo-secret-do-backend>
```

### Configuração de DNS em Produção

Configure um registro wildcard no seu provedor de DNS:

```
Type: A
Name: *
Value: <IP-do-servidor-nextjs>
TTL: 3600
```

Ou se usar Render/Vercel, configure CNAME:

```
Type: CNAME
Name: *
Value: <seu-app>.onrender.com
TTL: 3600
```

---

## Checklist de Conclusão

Antes de considerar esta fase completa, verifique:

- [ ] Todos os 18 tasks foram concluídos
- [ ] Todos os testes passam
- [ ] Documentação está atualizada
- [ ] Sistema funciona em produção
- [ ] Subdomínios funcionam corretamente
- [ ] Login funciona para diferentes tenants
- [ ] Dashboard exibe dados corretos
- [ ] Logout funciona corretamente
- [ ] Rotas estão protegidas
- [ ] Não há erros no console
- [ ] Performance está adequada (< 2s para dashboard)

---

**Documento criado em:** 24/10/2025  
**Última atualização:** 24/10/2025  
**Status:** Pronto para Implementação
