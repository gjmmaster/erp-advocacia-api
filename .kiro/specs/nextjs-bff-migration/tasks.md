# Tasks - Migração para Next.js BFF

## Fase 1: Setup e Preparação (2-3 horas)

### Task 1.1: Criar Projeto Next.js
- [ ] Criar novo diretório `frontend-nextjs/`
- [ ] Executar `npx create-next-app@latest frontend-nextjs --typescript --app --tailwind --eslint`
- [ ] Configurar opções:
  - TypeScript: Yes
  - ESLint: Yes
  - Tailwind CSS: Yes (opcional, pode usar CSS atual)
  - `src/` directory: Yes
  - App Router: Yes
  - Import alias: Yes (@/*)
- [ ] Testar que o projeto inicia: `npm run dev`

### Task 1.2: Configurar Estrutura de Pastas
- [ ] Criar estrutura conforme design:
  ```
  src/
  ├── app/
  │   ├── api/
  │   ├── super-admin/
  │   └── layout.tsx
  ├── components/
  ├── lib/
  ├── types/
  └── middleware.ts
  ```

### Task 1.3: Instalar Dependências
- [ ] Instalar dependências necessárias:
  ```bash
  npm install jose
  npm install @types/node --save-dev
  ```
- [ ] Remover dependências não necessárias (axios, jwt-decode)

### Task 1.4: Configurar Variáveis de Ambiente
- [ ] Criar `.env.local`:
  ```
  BACKEND_API_URL=http://localhost:3000
  JWT_SECRET=<mesmo-valor-do-backend>
  NODE_ENV=development
  ```
- [ ] Criar `.env.production`:
  ```
  BACKEND_API_URL=https://seu-backend.onrender.com
  JWT_SECRET=<mesmo-valor-do-backend>
  NODE_ENV=production
  ```
- [ ] Adicionar `.env*.local` ao `.gitignore`

---

## Fase 2: Migração de Componentes (6-8 horas)

### Task 2.1: Migrar Componentes Base
- [ ] Copiar e adaptar `CreateTenantModal.jsx` → `CreateTenantModal.tsx`
  - Adicionar tipos TypeScript
  - Remover axios, usar fetch
  - Chamar API routes do Next.js
- [ ] Copiar e adaptar `EditTenantModal.jsx` → `EditTenantModal.tsx`
- [ ] Copiar e adaptar `TenantsTable.jsx` → `TenantsTable.tsx`
- [ ] Testar componentes isoladamente

### Task 2.2: Migrar Páginas
- [ ] Criar `app/super-admin/login/page.tsx`
  - Migrar lógica de `SuperAdminLoginPage.jsx`
  - Usar Server Actions ou API route
  - Implementar cookies HttpOnly
- [ ] Criar `app/super-admin/dashboard/page.tsx`
  - Migrar lógica de `SuperAdminDashboardPage.jsx`
  - Usar Server Components quando possível
  - Implementar loading states

### Task 2.3: Migrar Estilos
- [ ] Copiar arquivos CSS:
  - `Modal.css` → `src/styles/modal.css`
  - `Dashboard.css` → `src/styles/dashboard.css`
- [ ] Ou converter para Tailwind CSS (opcional)
- [ ] Importar estilos nos componentes corretos

### Task 2.4: Criar Types TypeScript
- [ ] Criar `src/types/tenant.ts`:
  ```typescript
  export interface Tenant {
    id: string;
    company_name: string;
    subdomain: string;
    operator_limit: number;
    created_at: string;
  }
  ```
- [ ] Criar `src/types/user.ts`
- [ ] Criar `src/types/api.ts`

---

## Fase 3: Implementar BFF (4-6 horas)

### Task 3.1: Criar API Route de Login
- [ ] Criar `src/app/api/auth/login/route.ts`
- [ ] Implementar lógica:
  1. Receber credenciais do cliente
  2. Fazer proxy para backend Clojure
  3. Receber JWT do backend
  4. Criar cookies HttpOnly
  5. Retornar sucesso (sem token no body)
- [ ] Testar com curl/Postman

### Task 3.2: Criar API Route de Refresh
- [ ] Criar `src/app/api/auth/refresh/route.ts`
- [ ] Implementar lógica:
  1. Ler refresh token do cookie
  2. Validar com backend
  3. Gerar novo access token
  4. Atualizar cookies
  5. Retornar sucesso
- [ ] Testar renovação automática

### Task 3.3: Criar API Route de Logout
- [ ] Criar `src/app/api/auth/logout/route.ts`
- [ ] Implementar lógica:
  1. Limpar todos os cookies
  2. Invalidar sessão (se aplicável)
  3. Retornar sucesso
- [ ] Testar logout completo

### Task 3.4: Criar API Routes de Admin
- [ ] Criar `src/app/api/admin/tenants/route.ts`
  - GET: listar tenants
  - POST: criar tenant
- [ ] Criar `src/app/api/admin/tenants/[id]/route.ts`
  - GET: obter tenant
  - PUT: atualizar tenant
  - DELETE: deletar tenant
- [ ] Adicionar autenticação em todas as rotas

### Task 3.5: Criar Biblioteca de Fetch
- [ ] Criar `src/lib/api.ts`:
  ```typescript
  export async function fetchBackend(
    endpoint: string,
    options?: RequestInit
  ) {
    // Lógica de fetch com tratamento de erros
  }
  ```
- [ ] Adicionar retry logic
- [ ] Adicionar timeout
- [ ] Adicionar logging

---

## Fase 4: Implementar Autenticação (3-4 horas)

### Task 4.1: Criar Middleware de Autenticação
- [ ] Criar `src/middleware.ts`
- [ ] Implementar verificação de cookies
- [ ] Redirecionar não autenticados para login
- [ ] Proteger rotas `/super-admin/dashboard`

### Task 4.2: Criar Utilitários de Auth
- [ ] Criar `src/lib/auth.ts`:
  - `verifyToken()`
  - `createToken()`
  - `getSession()`
  - `setSession()`
- [ ] Usar biblioteca `jose` para JWT

### Task 4.3: Implementar Server Actions (Opcional)
- [ ] Criar `src/app/actions/auth.ts`
- [ ] Implementar login como Server Action
- [ ] Implementar logout como Server Action
- [ ] Testar em formulários

---

## Fase 5: Testes e Validação (4-6 horas)

### Task 5.1: Testes de Integração
- [ ] Testar fluxo completo de login
- [ ] Testar renovação automática de token
- [ ] Testar logout
- [ ] Testar acesso não autorizado
- [ ] Testar CRUD de tenants

### Task 5.2: Testes de Segurança
- [ ] Verificar que tokens não aparecem no browser
- [ ] Verificar cookies HttpOnly
- [ ] Verificar cookies Secure (em HTTPS)
- [ ] Testar proteção CSRF
- [ ] Testar rate limiting

### Task 5.3: Testes de Performance
- [ ] Medir tempo de carregamento inicial
- [ ] Medir tempo de navegação entre páginas
- [ ] Verificar tamanho do bundle
- [ ] Otimizar se necessário

### Task 5.4: Testes de Compatibilidade
- [ ] Testar em Chrome
- [ ] Testar em Firefox
- [ ] Testar em Safari
- [ ] Testar em Edge
- [ ] Testar em mobile

---

## Fase 6: Deploy e Configuração (2-3 horas)

### Task 6.1: Configurar Build
- [ ] Configurar `next.config.js`:
  ```javascript
  module.exports = {
    output: 'standalone',
    env: {
      BACKEND_API_URL: process.env.BACKEND_API_URL,
    },
  }
  ```
- [ ] Testar build local: `npm run build`
- [ ] Testar preview: `npm run start`

### Task 6.2: Deploy no Vercel (Recomendado)
- [ ] Criar conta no Vercel
- [ ] Conectar repositório GitHub
- [ ] Configurar variáveis de ambiente:
  - `BACKEND_API_URL`
  - `JWT_SECRET`
  - `NODE_ENV=production`
- [ ] Fazer deploy
- [ ] Testar em produção

### Task 6.3: Deploy no Render (Alternativa)
- [ ] Criar novo Web Service
- [ ] Configurar:
  - Build Command: `npm run build`
  - Start Command: `npm run start`
  - Root Directory: `frontend-nextjs`
- [ ] Configurar variáveis de ambiente
- [ ] Fazer deploy
- [ ] Testar em produção

### Task 6.4: Configurar Domínio
- [ ] Configurar DNS para apontar para Next.js
- [ ] Configurar SSL/HTTPS
- [ ] Testar acesso via domínio
- [ ] Atualizar CORS no backend se necessário

---

## Fase 7: Migração Final e Cleanup (2-3 horas)

### Task 7.1: Validação Final
- [ ] Testar todos os fluxos em produção
- [ ] Verificar logs de erro
- [ ] Monitorar performance
- [ ] Coletar feedback inicial

### Task 7.2: Documentação
- [ ] Atualizar README.md
- [ ] Documentar variáveis de ambiente
- [ ] Documentar processo de deploy
- [ ] Criar guia de troubleshooting

### Task 7.3: Cleanup do Projeto Antigo
- [ ] Fazer backup do frontend Vite
- [ ] Renomear `frontend/` para `frontend-vite-backup/`
- [ ] Renomear `frontend-nextjs/` para `frontend/`
- [ ] Atualizar scripts de deploy
- [ ] Atualizar documentação

### Task 7.4: Monitoramento
- [ ] Configurar logging (Vercel Analytics ou similar)
- [ ] Configurar alertas de erro
- [ ] Configurar monitoramento de performance
- [ ] Criar dashboard de métricas

---

## Checklist de Validação Final

### Funcionalidade
- [ ] Login funciona corretamente
- [ ] Dashboard carrega tenants
- [ ] Criar tenant funciona
- [ ] Editar tenant funciona
- [ ] Deletar tenant funciona
- [ ] Logout funciona
- [ ] Renovação automática de token funciona

### Segurança
- [ ] Tokens não aparecem no localStorage
- [ ] Cookies são HttpOnly
- [ ] Cookies são Secure (HTTPS)
- [ ] CSRF protection ativa
- [ ] Rate limiting funciona
- [ ] Rotas protegidas funcionam

### Performance
- [ ] Tempo de carregamento < 3s
- [ ] Navegação entre páginas < 1s
- [ ] Bundle size otimizado
- [ ] Imagens otimizadas

### Compatibilidade
- [ ] Funciona em todos os browsers
- [ ] Funciona em mobile
- [ ] Funciona em diferentes resoluções
- [ ] Acessibilidade básica

### Deploy
- [ ] Deploy automático funciona
- [ ] Variáveis de ambiente configuradas
- [ ] SSL/HTTPS ativo
- [ ] Domínio configurado
- [ ] Logs acessíveis

---

## Estimativa de Tempo Total

| Fase | Tempo Estimado | Prioridade |
|------|----------------|------------|
| Fase 1: Setup | 2-3 horas | Alta |
| Fase 2: Componentes | 6-8 horas | Alta |
| Fase 3: BFF | 4-6 horas | Alta |
| Fase 4: Auth | 3-4 horas | Alta |
| Fase 5: Testes | 4-6 horas | Média |
| Fase 6: Deploy | 2-3 horas | Alta |
| Fase 7: Cleanup | 2-3 horas | Baixa |
| **TOTAL** | **23-33 horas** | - |

**Distribuição recomendada:** 4-5 dias de trabalho (6-8 horas/dia)

---

## Notas Importantes

### Para IAs/Assistentes
- Cada task é independente e pode ser executada isoladamente
- Código de exemplo está disponível no design.md
- Sempre verificar tipos TypeScript
- Sempre testar após cada task
- Consultar documentação oficial do Next.js 14

### Para Desenvolvedores
- Fazer commits frequentes
- Testar em ambiente local antes de deploy
- Manter backup do código antigo
- Documentar decisões importantes
- Pedir ajuda se travar em alguma task

### Troubleshooting Comum
- **Erro de CORS:** Configurar no backend Clojure
- **Cookies não funcionam:** Verificar domínio e HTTPS
- **Build falha:** Verificar variáveis de ambiente
- **Performance ruim:** Usar Server Components
- **TypeScript errors:** Adicionar tipos corretos

---

**Documento criado:** 10 de Outubro de 2025  
**Última atualização:** 10 de Outubro de 2025  
**Status:** Pronto para Implementação
