# Requirements Document - Admin Impersonation

## Introduction

Este documento define os requisitos para implementar a funcionalidade de **Impersonation**, permitindo que o super admin acesse temporariamente a conta de qualquer usuário tenant sem precisar da senha, para dar suporte eficiente e reproduzir problemas.

### Contexto

**Problema Atual:**
- Super admin não consegue ver exatamente o que o usuário tenant vê
- Difícil reproduzir problemas reportados pelos usuários
- Suporte técnico limitado sem acesso à perspectiva do usuário
- Não há forma de validar configurações específicas do tenant

**Solução Proposta:**
- Impersonation seguro com audit trail completo
- Banner visível indicando modo administrador
- Sessão temporária com expiração curta
- Todas as ações auditadas e rastreáveis

### Objetivos

1. **Suporte Eficiente:** Super admin pode reproduzir problemas do usuário
2. **Transparência:** Todas as ações durante impersonation são visíveis
3. **Segurança:** Sessão limitada, banner sempre visível, audit completo
4. **Compliance:** Conforme LGPD/GDPR com rastreabilidade total

---

## Requirements

### Requirement 1: Iniciar Impersonation

**User Story:** Como super admin, eu quero acessar temporariamente a conta de um usuário tenant, para que eu possa ver exatamente o que ele vê e ajudá-lo com problemas.

#### Acceptance Criteria

1. WHEN super admin está no dashboard THEN SHALL ver botão "Acessar como" em cada linha da tabela de tenants
2. WHEN super admin clica em "Acessar como" THEN SHALL abrir modal de confirmação
3. WHEN modal é exibido THEN SHALL mostrar nome do tenant e email do usuário
4. WHEN super admin confirma THEN SHALL validar que usuário é super-admin
5. WHEN validação passa THEN SHALL gerar JWT especial com flags de impersonation
6. WHEN JWT é gerado THEN SHALL incluir campos: `impersonating: true`, `impersonator_id`, `impersonator_email`, `original_user_id`
7. WHEN token é criado THEN SHALL ter expiração de 1 hora (mais curto que sessão normal)
8. WHEN impersonation inicia THEN SHALL registrar em audit log com action "IMPERSONATE_START"
9. WHEN registro é criado THEN SHALL incluir: impersonator_id, target_user_id, tenant_id, ip_address, timestamp
10. WHEN impersonation é bem-sucedida THEN SHALL redirecionar para dashboard do tenant

---

### Requirement 2: Banner de Impersonation

**User Story:** Como sistema, eu quero exibir banner visível durante impersonation, para que fique claro que é o super admin acessando e não o usuário real.

#### Acceptance Criteria

1. WHEN usuário está em modo impersonation THEN SHALL exibir banner laranja no topo da página
2. WHEN banner é exibido THEN SHALL mostrar ícone de aviso "⚠️"
3. WHEN banner é exibido THEN SHALL mostrar texto "MODO ADMINISTRADOR"
4. WHEN banner é exibido THEN SHALL mostrar email do usuário sendo impersonado
5. WHEN banner é exibido THEN SHALL mostrar nome do tenant
6. WHEN banner é exibido THEN SHALL ter botão "Voltar para Super Admin"
7. WHEN banner é exibido THEN SHALL ser fixo no topo (position: fixed, z-index alto)
8. WHEN página faz scroll THEN SHALL banner permanecer visível
9. WHEN usuário tenta fechar banner THEN SHALL não permitir (sempre visível)
10. WHEN banner é exibido THEN SHALL ter cor laranja chamativa (#ff6b35 ou similar)

---

### Requirement 3: Permissões Durante Impersonation

**User Story:** Como sistema, eu quero que impersonation tenha as mesmas permissões do usuário original, para que o super admin veja exatamente o que o usuário vê.

#### Acceptance Criteria

1. WHEN super admin está impersonando THEN SHALL ter mesmas permissões do usuário impersonado
2. WHEN super admin está impersonando THEN SHALL ver mesmo tenant_id do usuário
3. WHEN super admin está impersonando THEN SHALL poder criar/editar/deletar dados
4. WHEN super admin cria dados THEN SHALL ser registrado como ação do usuário impersonado
5. WHEN super admin cria dados THEN SHALL audit log indicar flag `during_impersonation: true`
6. WHEN super admin está impersonando THEN SHALL NÃO poder acessar rotas de super-admin
7. WHEN super admin tenta acessar `/super-admin/*` THEN SHALL bloquear e mostrar erro
8. WHEN super admin está impersonando THEN SHALL NÃO poder impersonar outro usuário
9. WHEN super admin está impersonando THEN SHALL poder acessar todas as rotas do tenant

---

### Requirement 4: Encerrar Impersonation

**User Story:** Como super admin, eu quero encerrar impersonation facilmente, para que eu possa voltar ao meu dashboard de super admin.

#### Acceptance Criteria

1. WHEN super admin clica em "Voltar para Super Admin" THEN SHALL encerrar impersonation
2. WHEN impersonation encerra THEN SHALL invalidar token de impersonation
3. WHEN impersonation encerra THEN SHALL registrar em audit log com action "IMPERSONATE_END"
4. WHEN registro é criado THEN SHALL incluir: impersonator_id, target_user_id, duration, timestamp
5. WHEN impersonation encerra THEN SHALL redirecionar para dashboard super admin
6. WHEN token de impersonation expira (1 hora) THEN SHALL encerrar automaticamente
7. WHEN token expira THEN SHALL registrar em audit log com action "IMPERSONATE_EXPIRED"
8. WHEN token expira THEN SHALL redirecionar para login com mensagem "Sessão de impersonation expirada"
9. WHEN impersonation encerra THEN SHALL restaurar sessão original do super admin

---

### Requirement 5: Audit Log Completo

**User Story:** Como sistema, eu quero registrar todas as ações durante impersonation, para que haja transparência e rastreabilidade completa.

#### Acceptance Criteria

1. WHEN impersonation inicia THEN SHALL criar registro com action "IMPERSONATE_START"
2. WHEN registro é criado THEN SHALL incluir campos: impersonator_id, impersonator_email, target_user_id, target_email, tenant_id, ip_address, user_agent, timestamp
3. WHEN ação é executada durante impersonation THEN SHALL registrar com flag `during_impersonation: true`
4. WHEN ação é registrada THEN SHALL incluir `impersonator_id` no registro
5. WHEN impersonation encerra THEN SHALL criar registro com action "IMPERSONATE_END"
6. WHEN registro de fim é criado THEN SHALL incluir duração da sessão
7. WHEN audit log é consultado THEN SHALL ser possível filtrar por impersonation
8. WHEN tenant consulta audit log THEN SHALL ver que foi super admin acessando
9. WHEN super admin consulta audit log THEN SHALL ver histórico completo de impersonations
10. WHEN audit log é exibido THEN SHALL mostrar ícone especial para ações durante impersonation

---

### Requirement 6: Segurança e Validações

**User Story:** Como sistema, eu quero garantir que impersonation seja seguro e auditável, para que não haja abuso ou acesso não autorizado.

#### Acceptance Criteria

1. WHEN usuário tenta iniciar impersonation THEN SHALL validar que é super-admin
2. WHEN usuário não é super-admin THEN SHALL retornar erro 403 "Acesso negado"
3. WHEN token de impersonation é gerado THEN SHALL ter expiração de 1 hora
4. WHEN token de impersonation é gerado THEN SHALL NÃO ser renovável
5. WHEN token expira THEN SHALL forçar logout e redirecionar para login
6. WHEN super admin está impersonando THEN SHALL NÃO poder deletar o próprio tenant
7. WHEN super admin está impersonando THEN SHALL NÃO poder alterar configurações críticas de segurança
8. WHEN impersonation é iniciada THEN SHALL validar que tenant existe e está ativo
9. WHEN tenant está inativo THEN SHALL bloquear impersonation com erro "Tenant inativo"

---

### Requirement 7: Interface do Super Admin

**User Story:** Como super admin, eu quero interface clara para iniciar impersonation, para que seja fácil acessar como qualquer tenant.

#### Acceptance Criteria

1. WHEN super admin está no dashboard THEN SHALL ver coluna "Ações" na tabela de tenants
2. WHEN coluna é exibida THEN SHALL ter botão "Acessar como" com ícone de usuário
3. WHEN botão é clicado THEN SHALL abrir modal de confirmação
4. WHEN modal é exibido THEN SHALL mostrar: "Você está prestes a acessar como [Nome do Tenant]"
5. WHEN modal é exibido THEN SHALL mostrar email do usuário admin do tenant
6. WHEN modal é exibido THEN SHALL ter botão "Cancelar" e "Confirmar"
7. WHEN super admin confirma THEN SHALL iniciar impersonation
8. WHEN super admin cancela THEN SHALL fechar modal sem ação

---

## Non-Functional Requirements

### Security

1. **Token de Impersonation:** Expiração de 1 hora, não renovável
2. **Audit Trail:** Registro completo de todas as ações
3. **Validação:** Apenas super-admin pode impersonar
4. **Banner:** Sempre visível, não pode ser removido
5. **Isolamento:** Não pode acessar rotas de super-admin durante impersonation

### Performance

1. **Transição:** Iniciar impersonation em < 2 segundos
2. **Banner:** Renderização instantânea, sem flicker
3. **Audit Log:** Registro assíncrono, não bloqueia ações

### Usability

1. **Banner:** Sempre visível, não intrusivo
2. **Botão:** Fácil de encontrar e usar
3. **Feedback:** Imediato em todas as ações
4. **Mensagens:** Claras e acionáveis

### Compliance

1. **LGPD:** Transparência total em impersonation
2. **GDPR:** Direito de saber quem acessou dados
3. **Audit Trail:** Retenção de 2 anos mínimo
4. **Notificação:** Tenant pode ver no audit log

---

## Success Criteria

A implementação será considerada bem-sucedida quando:

1. ✅ Super admin pode impersonar qualquer usuário tenant
2. ✅ Banner de impersonation sempre visível durante sessão
3. ✅ Todas as ações durante impersonation são auditadas
4. ✅ Super admin pode encerrar impersonation facilmente
5. ✅ Token expira automaticamente após 1 hora
6. ✅ Audit log registra início, fim e expiração
7. ✅ Tenant pode ver no audit log quando foi impersonado
8. ✅ Sistema bloqueia acesso a rotas de super-admin durante impersonation
9. ✅ Interface é intuitiva e fácil de usar
10. ✅ Sistema está conforme LGPD/GDPR

---

## Out of Scope

O que NÃO será feito nesta implementação:

❌ Impersonation de super admin por outro super admin
❌ Impersonation de operadores (apenas admin do tenant)
❌ Gravação de tela durante impersonation
❌ Notificação em tempo real para o tenant
❌ Limite de tempo configurável (fixo em 1 hora)
❌ Renovação de token de impersonation
❌ Impersonation via API (apenas via interface web)

---

## Risks and Mitigations

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Abuso de impersonation | Baixa | Alto | Audit log completo + banner visível |
| Token não expira | Baixa | Alto | Validação no backend + middleware |
| Banner pode ser removido | Baixa | Médio | CSS com !important + validação no backend |
| Acesso a rotas de super-admin | Baixa | Alto | Middleware bloqueia rotas |
| Audit log não registra | Média | Alto | Testes automatizados + validação |

---

## Dependencies

### Externas
- Nenhuma dependência externa nova

### Internas
- Sistema de autenticação existente (JWT)
- Banco de dados CockroachDB
- Tabela de audit_log
- Middleware de autenticação
- Dashboard de super admin

---

## Timeline Estimate

- **Backend - Endpoints:** 2-3 horas
- **Backend - Audit Log:** 1-2 horas
- **Frontend - Botão e Modal:** 1-2 horas
- **Frontend - Banner:** 2-3 horas
- **Middleware - Validações:** 1-2 horas
- **Testes e Validação:** 2-3 horas

**Total:** 9-15 horas (1-2 dias de trabalho)

---

## Technical Notes

### JWT Structure Durante Impersonation

```json
{
  "user_id": "uuid-do-tenant-admin",
  "email": "admin@tenant.com",
  "tenant_id": "uuid-do-tenant",
  "role": "tenant_admin",
  "impersonating": true,
  "impersonator_id": "uuid-do-super-admin",
  "impersonator_email": "superadmin@sistema.com",
  "original_user_id": "uuid-do-super-admin",
  "exp": 1730000000,
  "iat": 1729996400
}
```

### Audit Log Schema

```sql
CREATE TABLE audit_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  action VARCHAR(100) NOT NULL,
  user_id UUID NOT NULL,
  tenant_id UUID,
  impersonator_id UUID,
  during_impersonation BOOLEAN DEFAULT FALSE,
  ip_address VARCHAR(45),
  user_agent TEXT,
  details JSONB,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_audit_impersonation ON audit_log(impersonator_id) WHERE impersonator_id IS NOT NULL;
CREATE INDEX idx_audit_during_impersonation ON audit_log(during_impersonation) WHERE during_impersonation = TRUE;
```

---

**Documento criado em:** 30 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Aguardando Aprovação
