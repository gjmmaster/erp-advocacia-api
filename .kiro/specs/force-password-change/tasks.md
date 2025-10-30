# Implementation Plan - Forçar Troca de Senha Temporária

**Feature:** Forçar usuários a trocarem senha temporária no primeiro login  
**Estimativa Total:** 9-13 horas (1-2 dias)  
**Prioridade:** Alta (Segurança)

---

## Task Overview

Este plano implementa a funcionalidade de forçar troca de senha temporária seguindo uma abordagem incremental e testável. Cada task é independente e pode ser validada antes de prosseguir.

---

## Tasks

- [x] 1. Database Migration e Setup


  - Criar migration para adicionar coluna `temporary_password`
  - Criar índice para performance
  - Atualizar usuários existentes se necessário
  - Validar migration em ambiente local
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_



- [ ] 1.1 Criar arquivo de migration SQL
  - Criar arquivo `migrations/add_temporary_password_column.sql`
  - Adicionar comando ALTER TABLE para coluna `temporary_password BOOLEAN DEFAULT false`
  - Adicionar comando CREATE INDEX para `idx_users_temporary_password`
  - Adicionar comando UPDATE para marcar usuários existentes (se aplicável)
  - Testar migration em banco local

  - _Requirements: 1.1, 1.2_

- [ ] 1.2 Executar migration em staging
  - Fazer backup do banco de staging
  - Executar migration
  - Validar que coluna foi criada corretamente


  - Validar que índice foi criado
  - Verificar logs para erros
  - _Requirements: 1.1, 1.2_

- [ ] 2. Backend - Atualizar Provision Tenant Handler
  - Modificar handler de criação de tenant para marcar senha como temporária



  - Atualizar função `provision-tenant-handler` em `src/juridico/api/handlers.clj`
  - Definir `temporary_password: true` ao criar usuário master
  - Adicionar log indicando senha temporária criada
  - Testar criação de tenant localmente
  - _Requirements: 1.1, 1.6_



- [ ] 3. Backend - Atualizar Login Handler
  - Modificar handler de login para incluir flag no JWT
  - Atualizar função de geração de JWT em `src/juridico/api/auth/jwt.clj`
  - Incluir campo `temporary-password` no payload do JWT
  - Incluir campo `requires-password-change` no payload
  - Criar audit log entry "LOGIN_WITH_TEMPORARY_PASSWORD"
  - Testar login com usuário de senha temporária
  - _Requirements: 2.1, 2.2, 2.3, 2.5, 2.6_

- [ ] 4. Backend - Implementar Change Password Handler
  - Criar novo namespace `juridico.api.handlers.password`


  - Implementar função `validate-password-strength`
  - Implementar função `change-password-handler`
  - Validar senha atual com bcrypt
  - Validar força da nova senha
  - Fazer hash da nova senha (bcrypt cost 12)
  - Atualizar banco de dados (password_hash e temporary_password)
  - Gerar novo JWT sem flag de senha temporária
  - Criar audit log entry "TEMPORARY_PASSWORD_CHANGED"

  - _Requirements: 5.1-5.7, 6.1-6.7, 9.1-9.7, 10.1-10.7, 12.4_

- [ ] 4.1 Implementar validação de senha
  - Criar função `validate-password-strength` que verifica:
    - Comprimento mínimo 8 caracteres
    - Presença de letra maiúscula
    - Presença de letra minúscula
    - Presença de número
  - Retornar true/false
  - Adicionar testes unitários


  - _Requirements: 6.1-6.6_

- [ ] 4.2 Implementar handler principal
  - Criar função `change-password-handler`
  - Extrair dados do request (current_password, new_password, confirm_password)
  - Buscar usuário no banco pelo user-id do JWT

  - Validar senha atual com `hashers/check`
  - Validar força da nova senha
  - Validar que senhas coincidem
  - Validar que nova senha é diferente da atual
  - Retornar erros apropriados para cada caso
  - _Requirements: 5.1-5.7, 6.1-6.7, 7.1-7.7_

- [ ] 4.3 Implementar atualização no banco
  - Fazer hash da nova senha com bcrypt cost 12
  - Atualizar campo `password_hash` na tabela users
  - Atualizar campo `temporary_password` para false
  - Usar transação para garantir atomicidade
  - Retornar usuário atualizado
  - _Requirements: 9.1-9.7_



- [ ] 4.4 Implementar geração de novo JWT
  - Gerar novo JWT com `temporary-password: false`
  - Incluir `requires-password-change: false`
  - Manter mesmos dados do usuário (id, email, role, tenant-id)
  - Definir expiração normal (7 dias)
  - Retornar token no response

  - _Requirements: 10.1-10.7_

- [ ]* 4.5 Adicionar testes unitários
  - Testar validação de senha forte
  - Testar validação de senha fraca
  - Testar senha atual incorreta
  - Testar senhas que não coincidem

  - Testar nova senha igual à atual
  - Testar troca bem-sucedida
  - _Requirements: 5.1-5.7, 6.1-6.7_

- [ ] 5. Backend - Adicionar Rota de Change Password
  - Adicionar rota POST `/api/auth/change-password` em `src/juridico/api/core.clj`
  - Aplicar middleware de autenticação


  - Aplicar middleware de rate limiting (3 tentativas / 5 min)
  - Conectar ao handler `change-password-handler`
  - Testar rota com curl/Postman
  - _Requirements: 4.1-4.7, 5.5_

- [ ] 6. Backend - Implementar Rate Limiting
  - Criar namespace `juridico.api.rate-limit`
  - Implementar função `check-rate-limit` para change-password
  - Limitar a 3 tentativas por 5 minutos por usuário
  - Retornar erro 429 quando limite excedido
  - Adicionar log quando rate limit é atingido


  - _Requirements: 5.7_

- [ ] 7. Backend - Implementar Envio de Email
  - Criar função `send-password-changed-notification!` em `juridico.api.email`
  - Email deve incluir: data/hora, IP, navegador/dispositivo
  - Email deve incluir link "Não fui eu - reportar"
  - Enviar email após troca bem-sucedida
  - Não bloquear response aguardando email (async)
  - _Requirements: 13.1-13.7_



- [ ] 8. Frontend - Atualizar Middleware
  - Modificar `frontend-nextjs/src/middleware.ts`
  - Adicionar verificação de `temporary_password` no JWT
  - Adicionar verificação de `requires_password_change` no JWT
  - Redirecionar para `/change-password` se flag for true
  - Permitir acesso apenas a rotas específicas:
    - `/change-password`
    - `/api/auth/change-password`
    - `/api/auth/logout`

  - Preservar mensagem de contexto no redirecionamento
  - Testar middleware localmente
  - _Requirements: 3.1-3.7_

- [ ] 9. Frontend - Criar API Route
  - Criar arquivo `frontend-nextjs/src/app/api/auth/change-password/route.ts`
  - Implementar handler POST
  - Extrair token do cookie

  - Validar campos obrigatórios
  - Validar que senhas coincidem (client-side)
  - Fazer proxy para backend
  - Atualizar cookie com novo token se sucesso
  - Retornar response apropriado
  - _Requirements: 4.1-4.7, 10.6, 10.7_

- [ ] 10. Frontend - Criar Página de Change Password
  - Criar arquivo `frontend-nextjs/src/app/change-password/page.tsx`

  - Criar componente funcional com estado
  - Adicionar campos: current_password, new_password, confirm_password
  - Adicionar botões de mostrar/ocultar senha
  - Implementar validação client-side
  - Implementar submit do formulário
  - Exibir mensagem de sucesso
  - Redirecionar para dashboard após 2 segundos

  - _Requirements: 4.1-4.7, 7.1-7.7, 11.1-11.7, 14.1-14.7_

- [ ] 10.1 Criar estrutura básica da página
  - Criar componente com 'use client'
  - Adicionar estado para formData (3 campos)
  - Adicionar estado para errors
  - Adicionar estado para loading
  - Adicionar estado para success
  - Adicionar estado para showPasswords (3 toggles)

  - _Requirements: 4.1-4.7_

- [ ] 10.2 Implementar formulário HTML
  - Criar form com onSubmit
  - Adicionar campo "Senha Atual (Temporária)"
  - Adicionar campo "Nova Senha"

  - Adicionar campo "Confirmar Nova Senha"
  - Cada campo com label, input e botão de mostrar/ocultar
  - Adicionar botão "Criar Nova Senha"
  - Aplicar estilos CSS
  - _Requirements: 4.1-4.7_

- [ ] 10.3 Implementar validação client-side
  - Criar função `validatePassword` que verifica requisitos
  - Validar em tempo real (onChange)
  - Exibir erros abaixo de cada campo

  - Desabilitar botão submit se houver erros
  - Adicionar tooltip explicando por que botão está desabilitado
  - _Requirements: 6.1-6.7, 7.1-7.7_

- [ ] 10.4 Implementar submit do formulário
  - Criar função `handleSubmit`
  - Prevenir default do form
  - Validar todos os campos

  - Fazer POST para `/api/auth/change-password`
  - Tratar response de sucesso
  - Tratar response de erro
  - Exibir loading durante requisição
  - _Requirements: 4.1-4.7, 14.1-14.7_

- [ ] 10.5 Implementar tela de sucesso
  - Exibir mensagem "Senha alterada com sucesso!"
  - Exibir ícone de sucesso verde

  - Exibir mensagem "Redirecionando para o dashboard..."
  - Aguardar 2 segundos
  - Redirecionar para `/dashboard` usando router.push
  - _Requirements: 11.1-11.7_

- [ ] 11. Frontend - Criar Password Strength Indicator
  - Criar arquivo `frontend-nextjs/src/components/PasswordStrengthIndicator.tsx`
  - Implementar cálculo de força da senha
  - Exibir barra de progresso visual
  - Exibir label (Fraca/Média/Forte/Muito Forte)

  - Exibir lista de requisitos com checkmarks
  - Usar cores apropriadas (vermelho/amarelo/verde)
  - Atualizar em tempo real conforme usuário digita
  - _Requirements: 8.1-8.7_

- [ ] 11.1 Implementar cálculo de força
  - Criar função que calcula score baseado em:
    - Comprimento (8+, 12+, 16+)
    - Tipos de caracteres (maiúscula, minúscula, número, especial)

  - Determinar nível (weak/medium/strong/very-strong)
  - Determinar label e cor
  - Usar useMemo para performance
  - _Requirements: 8.1-8.7_

- [ ] 11.2 Implementar UI do indicador
  - Criar barra de progresso com fill dinâmico
  - Exibir label com cor apropriada
  - Exibir lista de requisitos:
    - ✓ Mínimo 8 caracteres
    - ✓ Uma letra maiúscula
    - ✓ Uma letra minúscula
    - ✓ Um número
  - Aplicar estilos CSS
  - _Requirements: 8.1-8.7_

- [ ] 12. Frontend - Criar Estilos CSS
  - Criar arquivo `frontend-nextjs/src/app/change-password/page.module.css`
  - Estilizar container e card
  - Estilizar formulário e campos
  - Estilizar botões (submit, mostrar/ocultar)
  - Estilizar mensagens de erro
  - Estilizar tela de sucesso
  - Garantir responsividade (mobile-first)
  - Garantir contraste adequado (WCAG AA)
  - _Requirements: 15.7_

- [ ] 13. Frontend - Implementar Acessibilidade
  - Adicionar labels apropriados em todos os campos

  - Adicionar aria-invalid nos campos com erro
  - Adicionar aria-describedby ligando erros aos campos
  - Adicionar aria-label nos botões de mostrar/ocultar
  - Adicionar role="alert" nas mensagens de erro
  - Garantir navegação por teclado (Tab, Enter)
  - Testar com screen reader
  - _Requirements: 15.1-15.7_


- [ ] 14. Backend - Implementar Audit Log
  - Criar função `log-password-change!` em `juridico.api.audit`
  - Registrar "TEMPORARY_PASSWORD_CREATED" ao criar tenant
  - Registrar "LOGIN_WITH_TEMPORARY_PASSWORD" no login
  - Registrar "PASSWORD_CHANGE_PAGE_ACCESSED" ao acessar página
  - Registrar "TEMPORARY_PASSWORD_CHANGED" ao trocar senha
  - Registrar "PASSWORD_CHANGE_FAILED" em tentativas falhadas


  - Incluir: user_id, tenant_id, ip_address, user_agent, timestamp
  - _Requirements: 12.1-12.7_

- [ ]* 15. Testes de Integração
  - Criar arquivo de teste `test/integration/change_password_test.clj`
  - Testar fluxo completo: login → redirect → change → dashboard
  - Testar validação de senha atual incorreta
  - Testar validação de senha fraca
  - Testar validação de senhas que não coincidem
  - Testar geração de novo JWT
  - Testar atualização do cookie
  - _Requirements: Todos_

- [ ]* 16. Testes E2E
  - Criar arquivo `e2e/change-password.spec.ts` (Playwright)
  - Testar fluxo completo no browser
  - Testar redirecionamento forçado
  - Testar validação em tempo real
  - Testar indicador de força da senha
  - Testar mensagem de sucesso
  - Testar acesso ao dashboard após troca
  - _Requirements: Todos_

- [ ] 17. Documentação
  - Atualizar README.md com informações sobre senha temporária
  - Documentar fluxo de troca de senha
  - Documentar API endpoint `/api/auth/change-password`
  - Atualizar email de boas-vindas com aviso sobre troca obrigatória
  - Criar guia para usuários finais
  - _Requirements: Todos_

- [ ] 18. Deploy em Staging
  - Fazer backup do banco de staging
  - Executar migration no banco de staging
  - Deploy do backend em staging
  - Deploy do frontend em staging
  - Testar fluxo completo em staging
  - Validar logs e métricas
  - _Requirements: Todos_

- [ ] 19. Validação Final e Deploy em Produção
  - Revisar checklist de deployment
  - Fazer backup do banco de produção
  - Executar migration no banco de produção
  - Deploy do backend em produção
  - Deploy do frontend em produção
  - Testar fluxo completo em produção
  - Monitorar logs por 24h
  - Validar métricas (taxa de sucesso, tempo médio)
  - _Requirements: Todos_

---

## Testing Checklist

### Manual Testing

- [ ] Criar novo tenant e verificar que senha é marcada como temporária
- [ ] Fazer login com senha temporária
- [ ] Verificar redirecionamento para `/change-password`
- [ ] Tentar acessar `/dashboard` e verificar que redireciona de volta
- [ ] Preencher formulário com senha fraca e verificar erro
- [ ] Preencher formulário com senhas que não coincidem e verificar erro
- [ ] Preencher formulário com senha atual incorreta e verificar erro
- [ ] Preencher formulário corretamente e verificar sucesso
- [ ] Verificar que novo JWT não tem flag de senha temporária
- [ ] Verificar que dashboard carrega normalmente após troca
- [ ] Verificar que audit log foi criado
- [ ] Verificar que email de confirmação foi enviado

### Automated Testing

- [ ] Executar testes unitários do backend
- [ ] Executar testes unitários do frontend
- [ ] Executar testes de integração
- [ ] Executar testes E2E
- [ ] Validar cobertura de testes (meta: 70%+)

---

## Rollback Plan

Se algo der errado durante o deploy:

1. **Reverter código:**
   ```bash
   git revert <commit-hash>
   git push origin main
   ```

2. **Reverter migration (se necessário):**
   ```sql
   ALTER TABLE users DROP COLUMN temporary_password;
   DROP INDEX idx_users_temporary_password;
   ```

3. **Validar sistema:**
   - Login funciona
   - Dashboard carrega
   - Sem erros nos logs

---

## Success Metrics

Após implementação, monitorar:

- **Taxa de sucesso:** > 95% das trocas bem-sucedidas
- **Tempo médio:** < 5 segundos do início ao fim
- **Tentativas falhadas:** < 5% do total
- **Usuários com senha temporária:** Deve diminuir ao longo do tempo

---

**Documento criado em:** 30 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Pronto para Implementação

