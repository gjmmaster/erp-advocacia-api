# Tasks: Admin Impersonation

**Feature:** Super Admin Impersonation  
**Estimativa Total:** 9-13 horas

---

## 📋 Backend Tasks (4-6 horas)

### Task 1: Criar Handler de Impersonation Start
**Arquivo:** `src/juridico/api/handlers/impersonation.clj`  
**Estimativa:** 1.5 horas

- [ ] Criar namespace `juridico.api.handlers.impersonation`
- [ ] Implementar `start-impersonation-handler`
  - [ ] Validar que usuário é super-admin
  - [ ] Buscar tenant target no banco
  - [ ] Validar que tenant existe e está ativo
  - [ ] Gerar JWT especial com claims:
    - `user-id`: ID do tenant
    - `email`: Email do tenant
    - `role`: "tenant"
    - `tenant-id`: ID do tenant
    - `impersonating`: true
    - `impersonator-id`: ID do super admin
    - `impersonator-email`: Email do super admin
  - [ ] Expiração curta (1 hora)
  - [ ] Retornar token

### Task 2: Criar Handler de Impersonation Stop
**Arquivo:** `src/juridico/api/handlers/impersonation.clj`  
**Estimativa:** 1 hora

- [ ] Implementar `stop-impersonation-handler`
  - [ ] Validar que está em modo impersonation
  - [ ] Extrair `impersonator-id` do JWT
  - [ ] Buscar dados do super admin
  - [ ] Gerar novo JWT normal do super admin
  - [ ] Retornar token

### Task 3: Criar Audit Log
**Arquivo:** `src/juridico/api/handlers/impersonation.clj`  
**Estimativa:** 1 hora

- [ ] Criar função `log-impersonation-event`
- [ ] Registrar eventos:
  - `IMPERSONATE_START`
  - `IMPERSONATE_STOP`
  - `IMPERSONATE_EXPIRED`
- [ ] Incluir dados:
  - impersonator_id
  - target_user_id
  - tenant_id
  - ip_address
  - timestamp

### Task 4: Adicionar Rotas
**Arquivo:** `src/juridico/api/routes.clj`  
**Estimativa:** 30 minutos

- [ ] Adicionar rota POST `/api/admin/impersonate/:user-id`
- [ ] Adicionar rota POST `/api/admin/stop-impersonate`
- [ ] Proteger com middleware de autenticação
- [ ] Validar role super-admin

---

## 🎨 Frontend Tasks (3-4 horas)

### Task 5: Criar API Routes
**Arquivos:** 
- `frontend-nextjs/src/app/api/admin/impersonate/[userId]/route.ts`
- `frontend-nextjs/src/app/api/admin/stop-impersonate/route.ts`

**Estimativa:** 1 hora

- [ ] Criar route handler para start impersonation
  - [ ] Chamar backend `/api/admin/impersonate/:user-id`
  - [ ] Receber novo JWT
  - [ ] Atualizar cookie
  - [ ] Retornar sucesso
- [ ] Criar route handler para stop impersonation
  - [ ] Chamar backend `/api/admin/stop-impersonate`
  - [ ] Receber JWT do super admin
  - [ ] Atualizar cookie
  - [ ] Retornar sucesso

### Task 6: Adicionar Botão "Acessar Como" no Dashboard Admin
**Arquivo:** `frontend-nextjs/src/app/admin/dashboard/page.tsx`  
**Estimativa:** 1 hora

- [ ] Adicionar coluna "Ações" na tabela de tenants
- [ ] Adicionar botão "Acessar Como" em cada linha
- [ ] Criar modal de confirmação
  - [ ] Mostrar nome e email do tenant
  - [ ] Avisar sobre audit log
  - [ ] Botões: "Cancelar" e "Confirmar"
- [ ] Implementar função `handleImpersonate`
  - [ ] Chamar API `/api/admin/impersonate/[userId]`
  - [ ] Redirecionar para `/dashboard` (tenant)
  - [ ] Mostrar toast de sucesso

### Task 7: Criar Banner de Impersonation
**Arquivo:** `frontend-nextjs/src/components/ImpersonationBanner.tsx`  
**Estimativa:** 1 hora

- [ ] Criar componente `ImpersonationBanner`
- [ ] Design:
  - [ ] Fixo no topo (position: fixed, top: 0)
  - [ ] Cor laranja chamativa (#FF6B35)
  - [ ] Ícone de alerta
  - [ ] Texto: "⚠️ MODO ADMINISTRADOR - Acessando como: [email]"
  - [ ] Botão: "Voltar para Super Admin"
- [ ] Implementar função `handleStopImpersonation`
  - [ ] Chamar API `/api/admin/stop-impersonate`
  - [ ] Redirecionar para `/admin/dashboard`
  - [ ] Mostrar toast de sucesso

### Task 8: Atualizar Layout do Tenant
**Arquivo:** `frontend-nextjs/src/app/dashboard/layout.tsx`  
**Estimativa:** 30 minutos

- [ ] Importar `ImpersonationBanner`
- [ ] Verificar se JWT tem flag `impersonating: true`
- [ ] Renderizar banner se estiver em modo impersonation
- [ ] Ajustar padding do conteúdo (para não ficar atrás do banner)

### Task 9: Atualizar Middleware
**Arquivo:** `frontend-nextjs/src/middleware.ts`  
**Estimativa:** 30 minutos

- [ ] Detectar flag `impersonating: true` no JWT
- [ ] Durante impersonation:
  - [ ] Permitir acesso a rotas de tenant
  - [ ] Bloquear acesso a rotas de super-admin
  - [ ] Permitir rota `/api/admin/stop-impersonate`

---

## 🧪 Testing Tasks (2-3 horas)

### Task 10: Testes Manuais
**Estimativa:** 1.5 horas

- [ ] Teste 1: Start Impersonation
  - [ ] Login como super admin
  - [ ] Clicar em "Acessar Como" em um tenant
  - [ ] Confirmar modal
  - [ ] Verificar redirecionamento para dashboard tenant
  - [ ] Verificar banner laranja visível
  - [ ] Verificar que vê dados do tenant

- [ ] Teste 2: Durante Impersonation
  - [ ] Tentar acessar `/admin/dashboard` (deve bloquear)
  - [ ] Navegar pelo dashboard do tenant
  - [ ] Verificar que banner permanece visível
  - [ ] Verificar audit log no banco

- [ ] Teste 3: Stop Impersonation
  - [ ] Clicar em "Voltar para Super Admin"
  - [ ] Verificar redirecionamento para `/admin/dashboard`
  - [ ] Verificar que banner desapareceu
  - [ ] Verificar que voltou para super admin
  - [ ] Verificar audit log no banco

- [ ] Teste 4: Expiração
  - [ ] Aguardar 1 hora (ou modificar expiração para 1 minuto)
  - [ ] Verificar que JWT expira
  - [ ] Verificar redirecionamento para login
  - [ ] Verificar audit log

### Task 11: Testes de Segurança
**Estimativa:** 1 hora

- [ ] Tentar impersonate sem ser super admin (deve falhar)
- [ ] Tentar impersonate tenant inexistente (deve falhar)
- [ ] Tentar acessar rotas admin durante impersonation (deve bloquear)
- [ ] Verificar que JWT tem expiração curta
- [ ] Verificar que todas as ações são auditadas

### Task 12: Testes de Edge Cases
**Estimativa:** 30 minutos

- [ ] Impersonate tenant que está com senha temporária
- [ ] Impersonate tenant inativo
- [ ] Stop impersonation sem estar impersonando
- [ ] Múltiplos impersonates seguidos

---

## 📝 Documentation Tasks (1 hora)

### Task 13: Criar Documentação
**Estimativa:** 1 hora

- [ ] Criar `IMPLEMENTACAO_IMPERSONATION.md`
  - [ ] Como funciona
  - [ ] Fluxo completo
  - [ ] Segurança
  - [ ] Audit log
  - [ ] Troubleshooting
- [ ] Atualizar `STATUS_E_PROXIMOS_PASSOS.md`
- [ ] Atualizar `README.md` principal

---

## 📊 Checklist de Conclusão

- [ ] Todas as tasks backend completas
- [ ] Todas as tasks frontend completas
- [ ] Todos os testes passando
- [ ] Documentação criada
- [ ] Code review feito
- [ ] Deploy em produção
- [ ] Validação em produção

---

## 🎯 Ordem de Implementação Recomendada

1. **Backend primeiro** (Tasks 1-4)
   - Criar handlers
   - Adicionar rotas
   - Testar com Postman/curl

2. **Frontend depois** (Tasks 5-9)
   - Criar API routes
   - Adicionar botão
   - Criar banner
   - Atualizar middleware

3. **Testes** (Tasks 10-12)
   - Testes manuais
   - Testes de segurança
   - Edge cases

4. **Documentação** (Task 13)
   - Criar guia completo

---

**Próximo Passo:** Começar Task 1 - Criar Handler de Impersonation Start
