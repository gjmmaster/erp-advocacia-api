# Requirements Document - Impersonation e Reset de Senha

## Introduction

Este documento define os requisitos para implementar duas funcionalidades críticas de administração:

1. **Impersonation (Acessar Como):** Permitir que super admin acesse temporariamente a conta de qualquer usuário tenant sem precisar da senha
2. **Reset de Senha:** Duas opções - self-service por email e reset manual pelo super admin com senha temporária visível

### Contexto

**Problema Atual:**
- Super admin não consegue ver exatamente o que o usuário vê para dar suporte
- Usuários que esquecem senha precisam contatar suporte
- Não há forma de ajudar usuários que não sabem usar email

**Solução Proposta:**
- Impersonation seguro com audit trail completo
- Reset de senha self-service via email
- Reset de senha manual pelo super admin com senha temporária

### Objetivos

1. **Suporte Eficiente:** Super admin pode reproduzir problemas do usuário
2. **Autonomia:** Usuários podem resetar própria senha
3. **Flexibilidade:** Super admin pode ajudar usuários menos técnicos
4. **Segurança:** Todas as ações auditadas e transparentes
5. **Compliance:** Conforme LGPD/GDPR

---

## Requirements

### Requirement 1: Impersonation - Iniciar Sessão

**User Story:** Como super admin, eu quero acessar temporariamente a conta de um usuário tenant, para que eu possa ver exatamente o que ele vê e ajudá-lo com problemas.

#### Acceptance Criteria

1. WHEN super admin está no dashboard THEN SHALL ver botão "Acessar como" em cada tenant
2. WHEN super admin clica em "Acessar como" THEN SHALL validar que é super-admin
3. WHEN validação passa THEN SHALL gerar JWT especial com flag `impersonating: true`
4. WHEN JWT é gerado THEN SHALL incluir `impersonator-id` e `impersonator-email`
5. WHEN impersonation inicia THEN SHALL registrar em audit log com timestamp, IPs e detalhes
6. WHEN token é criado THEN SHALL ter expiração de 1 hora (mais curto que sessão normal)
7. WHEN impersonation é bem-sucedida THEN SHALL redirecionar para dashboard do tenant

### Requirement 2: Impersonation - Banner de Aviso

**User Story:** Como sistema, eu quero exibir banner visível durante impersonation, para que fique claro que é o super admin acessando.

#### Acceptance Criteria

1. WHEN usuário está em modo impersonation THEN SHALL exibir banner laranja no topo
2. WHEN banner é exibido THEN SHALL mostrar: "⚠️ MODO ADMINISTRADOR"
3. WHEN banner é exibido THEN SHALL mostrar email do usuário sendo impersonado
4. WHEN banner é exibido THEN SHALL mostrar nome do tenant
5. WHEN banner é exibido THEN SHALL ter botão "Voltar para Super Admin"
6. WHEN banner é exibido THEN SHALL ser fixo no topo (não desaparece com scroll)
7. WHEN usuário tenta fechar banner THEN SHALL não permitir (sempre visível)

### Requirement 3: Impersonation - Permissões e Limitações

**User Story:** Como sistema, eu quero que impersonation tenha as mesmas permissões do usuário original, para que o super admin veja exatamente o que o usuário vê.

#### Acceptance Criteria

1. WHEN super admin está impersonando THEN SHALL ter mesmas permissões do usuário
2. WHEN super admin está impersonando THEN SHALL ver mesmo tenant-id
3. WHEN super admin está impersonando THEN SHALL poder criar/editar/deletar dados
4. WHEN super admin cria dados THEN SHALL ser registrado como ação do usuário impersonado
5. WHEN super admin cria dados THEN SHALL audit log indicar que foi durante impersonation
6. WHEN super admin está impersonando THEN SHALL NÃO poder acessar rotas de super-admin
7. WHEN super admin está impersonando THEN SHALL NÃO poder impersonar outro usuário

### Requirement 4: Impersonation - Encerrar Sessão

**User Story:** Como super admin, eu quero encerrar impersonation facilmente, para que eu possa voltar ao meu dashboard de super admin.

#### Acceptance Criteria

1. WHEN super admin clica em "Voltar para Super Admin" THEN SHALL encerrar impersonation
2. WHEN impersonation encerra THEN SHALL invalidar token de impersonation
3. WHEN impersonation encerra THEN SHALL registrar em audit log
4. WHEN impersonation encerra THEN SHALL redirecionar para dashboard super admin
5. WHEN token de impersonation expira THEN SHALL encerrar automaticamente
6. WHEN token expira THEN SHALL registrar em audit log como "IMPERSONATE_EXPIRED"
7. WHEN impersonation encerra THEN SHALL restaurar sessão original do super admin

### Requirement 5: Impersonation - Audit Log

**User Story:** Como sistema, eu quero registrar todas as ações durante impersonation, para que haja transparência e rastreabilidade completa.

#### Acceptance Criteria

1. WHEN impersonation inicia THEN SHALL criar registro com action "IMPERSONATE_START"
2. WHEN registro é criado THEN SHALL incluir: impersonator_id, target_user_id, tenant_id, ip_address
3. WHEN ação é executada durante impersonation THEN SHALL registrar com flag impersonation
4. WHEN impersonation encerra THEN SHALL criar registro com action "IMPERSONATE_END"
5. WHEN audit log é consultado THEN SHALL ser possível filtrar por impersonation
6. WHEN tenant consulta audit log THEN SHALL ver que foi super admin acessando
7. WHEN super admin consulta audit log THEN SHALL ver histórico completo de impersonations

### Requirement 6: Reset de Senha - Self-Service por Email

**User Story:** Como usuário tenant, eu quero resetar minha própria senha via email, para que eu possa recuperar acesso sem depender do suporte.

#### Acceptance Criteria

1. WHEN usuário está na tela de login THEN SHALL ver link "Esqueci minha senha"
2. WHEN usuário clica no link THEN SHALL abrir formulário pedindo email
3. WHEN usuário submete email THEN SHALL validar que email existe no sistema
4. WHEN email existe THEN SHALL gerar token único de reset (UUID)
5. WHEN token é gerado THEN SHALL ter validade de 1 hora
6. WHEN token é gerado THEN SHALL enviar email com link de reset
7. WHEN email é enviado THEN SHALL incluir link: `/reset-password?token=<uuid>`

### Requirement 7: Reset de Senha - Validação do Token

**User Story:** Como sistema, eu quero validar tokens de reset de senha, para que apenas tokens válidos permitam mudança de senha.

#### Acceptance Criteria

1. WHEN usuário clica no link do email THEN SHALL validar token
2. WHEN token é válido THEN SHALL exibir formulário de nova senha
3. WHEN token é inválido THEN SHALL exibir erro "Token inválido ou expirado"
4. WHEN token já foi usado THEN SHALL exibir erro "Token já utilizado"
5. WHEN token expirou THEN SHALL exibir erro "Token expirado. Solicite novo reset"
6. WHEN formulário é exibido THEN SHALL pedir: nova senha e confirmação
7. WHEN senhas não coincidem THEN SHALL exibir erro "Senhas não coincidem"

### Requirement 8: Reset de Senha - Atualização da Senha

**User Story:** Como usuário, eu quero definir nova senha após reset, para que eu possa acessar minha conta novamente.

#### Acceptance Criteria

1. WHEN usuário submete nova senha THEN SHALL validar força da senha (mínimo 8 caracteres)
2. WHEN senha é válida THEN SHALL fazer hash bcrypt da senha
3. WHEN hash é criado THEN SHALL atualizar senha no banco de dados
4. WHEN senha é atualizada THEN SHALL invalidar token de reset
5. WHEN senha é atualizada THEN SHALL registrar em audit log
6. WHEN senha é atualizada THEN SHALL exibir mensagem de sucesso
7. WHEN sucesso é exibido THEN SHALL redirecionar para login após 3 segundos

### Requirement 9: Reset de Senha - Super Admin Manual

**User Story:** Como super admin, eu quero resetar senha de um usuário manualmente, para que eu possa ajudar usuários que não conseguem usar email.

#### Acceptance Criteria

1. WHEN super admin está no dashboard THEN SHALL ver botão "Resetar Senha" em cada tenant
2. WHEN super admin clica em "Resetar Senha" THEN SHALL abrir modal de confirmação
3. WHEN super admin confirma THEN SHALL gerar senha temporária aleatória (12 caracteres)
4. WHEN senha é gerada THEN SHALL incluir: letras maiúsculas, minúsculas, números e símbolos
5. WHEN senha é gerada THEN SHALL fazer hash e salvar no banco
6. WHEN senha é salva THEN SHALL exibir senha temporária em modal (VISÍVEL)
7. WHEN modal exibe senha THEN SHALL ter botão "Copiar Senha"

### Requirement 10: Reset de Senha - Senha Temporária

**User Story:** Como super admin, eu quero ver a senha temporária gerada, para que eu possa enviá-la ao usuário por WhatsApp ou telefone.

#### Acceptance Criteria

1. WHEN senha temporária é exibida THEN SHALL mostrar em texto grande e legível
2. WHEN senha é exibida THEN SHALL ter botão "Copiar para Área de Transferência"
3. WHEN botão copiar é clicado THEN SHALL copiar senha e mostrar feedback "Copiado!"
4. WHEN modal é exibido THEN SHALL ter aviso: "Anote esta senha. Ela não será exibida novamente"
5. WHEN modal é fechado THEN SHALL confirmar: "Tem certeza? Senha não será exibida novamente"
6. WHEN senha é gerada THEN SHALL marcar usuário com flag `temporary_password: true`
7. WHEN usuário faz login com senha temporária THEN SHALL forçar mudança de senha

### Requirement 11: Reset de Senha - Forçar Mudança

**User Story:** Como sistema, eu quero forçar usuário a mudar senha temporária no primeiro login, para que a senha seja conhecida apenas pelo usuário.

#### Acceptance Criteria

1. WHEN usuário faz login com senha temporária THEN SHALL validar flag `temporary_password`
2. WHEN flag é true THEN SHALL redirecionar para tela "Criar Nova Senha"
3. WHEN tela é exibida THEN SHALL pedir: senha atual (temporária), nova senha, confirmação
4. WHEN usuário submete THEN SHALL validar senha atual
5. WHEN senha atual é válida THEN SHALL validar nova senha (mínimo 8 caracteres)
6. WHEN nova senha é válida THEN SHALL fazer hash e atualizar
7. WHEN senha é atualizada THEN SHALL remover flag `temporary_password`

### Requirement 12: Reset de Senha - Audit Log

**User Story:** Como sistema, eu quero registrar todos os resets de senha, para que haja rastreabilidade completa.

#### Acceptance Criteria

1. WHEN reset self-service é solicitado THEN SHALL registrar "PASSWORD_RESET_REQUESTED"
2. WHEN token é usado THEN SHALL registrar "PASSWORD_RESET_COMPLETED"
3. WHEN super admin reseta senha THEN SHALL registrar "PASSWORD_RESET_BY_ADMIN"
4. WHEN registro é criado THEN SHALL incluir: user_id, admin_id (se aplicável), ip_address, timestamp
5. WHEN usuário muda senha temporária THEN SHALL registrar "TEMPORARY_PASSWORD_CHANGED"
6. WHEN audit log é consultado THEN SHALL ser possível filtrar por tipo de reset
7. WHEN tenant consulta audit log THEN SHALL ver histórico de resets de senha

### Requirement 13: Notificações de Segurança

**User Story:** Como usuário, eu quero ser notificado quando minha senha for alterada, para que eu saiba se houve acesso não autorizado.

#### Acceptance Criteria

1. WHEN senha é resetada (self-service) THEN SHALL enviar email de confirmação
2. WHEN senha é resetada por admin THEN SHALL enviar email notificando usuário
3. WHEN email é enviado THEN SHALL incluir: data/hora, IP, método (self-service ou admin)
4. WHEN senha temporária é criada THEN SHALL enviar email notificando
5. WHEN senha temporária é mudada THEN SHALL enviar email de confirmação
6. WHEN email é enviado THEN SHALL incluir link para "Não fui eu - reportar"
7. WHEN usuário reporta THEN SHALL bloquear conta e notificar super admin

---

## Non-Functional Requirements

### Security

1. **Impersonation Token:** Expiração curta (1 hora), não renovável
2. **Reset Token:** UUID v4, válido por 1 hora, uso único
3. **Senha Temporária:** 12+ caracteres, complexa, forçar mudança
4. **Audit Trail:** Registro completo de todas as ações
5. **Rate Limiting:** Máximo 3 tentativas de reset por hora por email

### Performance

1. **Impersonation:** Transição < 2 segundos
2. **Email de Reset:** Enviado em < 5 segundos
3. **Geração de Senha:** < 1 segundo

### Usability

1. **Banner de Impersonation:** Sempre visível, não intrusivo
2. **Formulários:** Validação em tempo real
3. **Mensagens de Erro:** Claras e acionáveis
4. **Feedback:** Imediato em todas as ações

### Compliance

1. **LGPD:** Transparência em impersonation e resets
2. **GDPR:** Direito de saber quem acessou dados
3. **Audit Trail:** Retenção de 2 anos mínimo

---

## Success Criteria

A implementação será considerada bem-sucedida quando:

1. ✅ Super admin pode impersonar qualquer usuário
2. ✅ Banner de impersonation sempre visível
3. ✅ Todas as ações durante impersonation são auditadas
4. ✅ Usuários podem resetar senha via email
5. ✅ Super admin pode gerar senha temporária
6. ✅ Senha temporária é visível para super admin
7. ✅ Usuário é forçado a mudar senha temporária
8. ✅ Todos os resets são registrados em audit log
9. ✅ Emails de notificação são enviados
10. ✅ Sistema está conforme LGPD/GDPR

---

## Out of Scope

O que NÃO será feito nesta implementação:

❌ Autenticação de dois fatores (2FA)
❌ Biometria ou autenticação avançada
❌ Histórico de senhas anteriores
❌ Política de expiração de senha
❌ Login social (Google, Facebook, etc.)
❌ Impersonation de super admin por outro super admin
❌ Reset de senha via SMS

---

## Risks and Mitigations

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Abuso de impersonation | Baixa | Alto | Audit log completo + banner visível |
| Senha temporária interceptada | Média | Alto | Forçar mudança no primeiro login |
| Email de reset não chega | Média | Médio | Opção de reset manual por admin |
| Token de reset vazado | Baixa | Alto | Expiração curta (1h) + uso único |
| Super admin perde senha temporária | Média | Baixo | Pode gerar nova senha |

---

## Dependencies

### Externas
- Serviço de email (SMTP ou SendGrid)
- Biblioteca de geração de senhas seguras
- Biblioteca bcrypt para hash

### Internas
- Sistema de autenticação existente
- Banco de dados PostgreSQL
- Tabela de audit_log
- Sistema de JWT

---

## Timeline Estimate

- **Fase 1 - Impersonation Backend:** 4-6 horas
- **Fase 2 - Impersonation Frontend:** 3-4 horas
- **Fase 3 - Reset Self-Service:** 4-6 horas
- **Fase 4 - Reset Manual Admin:** 3-4 horas
- **Fase 5 - Testes e Audit:** 4-6 horas

**Total:** 18-26 horas (3-4 dias de trabalho)

---

**Documento criado em:** 29 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Aguardando Aprovação
