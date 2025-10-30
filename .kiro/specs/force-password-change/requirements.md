# Requirements Document - Forçar Troca de Senha Temporária

## Introduction

Este documento define os requisitos para implementar a funcionalidade de forçar usuários a trocarem suas senhas temporárias no primeiro login, garantindo que apenas o usuário final conheça sua senha definitiva.

### Contexto

**Problema Atual:**
- Quando um tenant é criado, o super admin vê a senha temporária
- Usuário faz login com senha temporária e pode continuar usando ela indefinidamente
- Senha temporária pode ter sido vista por outras pessoas (email, WhatsApp, etc.)
- Não há garantia de que apenas o usuário final conhece a senha

**Solução Proposta:**
- Detectar quando usuário faz login com senha temporária
- Forçar redirecionamento para tela de "Criar Nova Senha"
- Bloquear acesso ao dashboard até que senha seja trocada
- Validar força da nova senha
- Registrar troca em audit log

### Objetivos

1. **Segurança:** Garantir que apenas o usuário final conhece sua senha
2. **Compliance:** Seguir melhores práticas de segurança (OWASP, NIST)
3. **Usabilidade:** Processo simples e claro para o usuário
4. **Auditoria:** Rastrear quando senhas temporárias são trocadas

---

## Requirements

### Requirement 1: Marcação de Senha Temporária

**User Story:** Como sistema, eu quero marcar quando um usuário tem senha temporária, para que eu possa forçar a troca no primeiro login.

#### Acceptance Criteria

1. WHEN tenant é criado THEN SHALL criar usuário master com flag `temporary_password: true`
2. WHEN flag é definida THEN SHALL ser armazenada na tabela `users`
3. WHEN super admin reseta senha manualmente THEN SHALL definir flag `temporary_password: true`
4. WHEN usuário troca senha temporária THEN SHALL remover flag (definir como `false`)
5. WHEN usuário é criado sem senha temporária THEN SHALL ter flag `temporary_password: false`
6. WHEN flag é consultada THEN SHALL ser incluída no JWT payload
7. WHEN JWT é gerado THEN SHALL incluir campo `temporary_password: boolean`

### Requirement 2: Detecção no Login

**User Story:** Como sistema, eu quero detectar quando usuário com senha temporária faz login, para que eu possa forçar a troca de senha.

#### Acceptance Criteria

1. WHEN usuário faz login THEN SHALL consultar flag `temporary_password` no banco
2. WHEN flag é `true` THEN SHALL incluir no JWT retornado
3. WHEN JWT é retornado THEN SHALL incluir campo `requires_password_change: true`
4. WHEN flag é `false` THEN SHALL permitir login normal
5. WHEN login é bem-sucedido com senha temporária THEN SHALL registrar em audit log
6. WHEN audit log é criado THEN SHALL incluir action "LOGIN_WITH_TEMPORARY_PASSWORD"
7. WHEN usuário faz login THEN SHALL retornar status indicando necessidade de troca

### Requirement 3: Middleware de Verificação

**User Story:** Como sistema, eu quero verificar em cada requisição se usuário precisa trocar senha, para que ele não consiga acessar outras páginas.

#### Acceptance Criteria

1. WHEN middleware é executado THEN SHALL verificar JWT do usuário
2. WHEN JWT contém `requires_password_change: true` THEN SHALL verificar rota atual
3. WHEN rota é `/change-password` THEN SHALL permitir acesso
4. WHEN rota é `/api/auth/change-password` THEN SHALL permitir acesso
5. WHEN rota é `/api/auth/logout` THEN SHALL permitir acesso
6. WHEN rota é qualquer outra THEN SHALL redirecionar para `/change-password`
7. WHEN redirecionamento ocorre THEN SHALL preservar mensagem de contexto

### Requirement 4: Página de Troca de Senha

**User Story:** Como usuário com senha temporária, eu quero uma página clara para criar minha nova senha, para que eu possa acessar o sistema com segurança.

#### Acceptance Criteria

1. WHEN usuário é redirecionado THEN SHALL exibir página "Criar Nova Senha"
2. WHEN página carrega THEN SHALL exibir mensagem: "Por segurança, você precisa criar uma nova senha"
3. WHEN formulário é exibido THEN SHALL ter campo "Senha Atual (Temporária)"
4. WHEN formulário é exibido THEN SHALL ter campo "Nova Senha"
5. WHEN formulário é exibido THEN SHALL ter campo "Confirmar Nova Senha"
6. WHEN usuário digita nova senha THEN SHALL exibir indicador de força da senha
7. WHEN formulário é exibido THEN SHALL ter botão "Criar Nova Senha"

### Requirement 5: Validação da Senha Atual

**User Story:** Como sistema, eu quero validar que o usuário conhece a senha temporária, para que apenas o usuário legítimo possa trocar a senha.

#### Acceptance Criteria

1. WHEN usuário submete formulário THEN SHALL validar senha atual
2. WHEN senha atual é enviada THEN SHALL fazer requisição ao backend
3. WHEN backend recebe requisição THEN SHALL validar senha atual com bcrypt
4. WHEN senha atual está incorreta THEN SHALL retornar erro 401
5. WHEN erro é retornado THEN SHALL exibir mensagem "Senha atual incorreta"
6. WHEN senha atual está correta THEN SHALL prosseguir com validação da nova senha
7. WHEN validação falha 3 vezes THEN SHALL bloquear temporariamente (5 minutos)

### Requirement 6: Validação da Nova Senha

**User Story:** Como sistema, eu quero validar que a nova senha é forte, para que a conta do usuário seja segura.

#### Acceptance Criteria

1. WHEN nova senha é digitada THEN SHALL validar comprimento mínimo (8 caracteres)
2. WHEN senha tem menos de 8 caracteres THEN SHALL exibir erro "Senha deve ter no mínimo 8 caracteres"
3. WHEN senha é validada THEN SHALL verificar presença de letra maiúscula
4. WHEN senha é validada THEN SHALL verificar presença de letra minúscula
5. WHEN senha é validada THEN SHALL verificar presença de número
6. WHEN senha não atende critérios THEN SHALL exibir lista de requisitos não atendidos
7. WHEN nova senha é igual à temporária THEN SHALL exibir erro "Nova senha deve ser diferente da temporária"

### Requirement 7: Confirmação de Senha

**User Story:** Como usuário, eu quero confirmar minha nova senha, para que eu não cometa erros de digitação.

#### Acceptance Criteria

1. WHEN usuário digita confirmação THEN SHALL comparar com nova senha
2. WHEN senhas não coincidem THEN SHALL exibir erro "As senhas não coincidem"
3. WHEN erro é exibido THEN SHALL destacar campo de confirmação em vermelho
4. WHEN senhas coincidem THEN SHALL remover erro
5. WHEN senhas coincidem THEN SHALL habilitar botão "Criar Nova Senha"
6. WHEN botão está desabilitado THEN SHALL exibir tooltip explicando por quê
7. WHEN validação passa THEN SHALL exibir ícone de sucesso verde

### Requirement 8: Indicador de Força da Senha

**User Story:** Como usuário, eu quero ver a força da minha senha em tempo real, para que eu possa criar uma senha segura.

#### Acceptance Criteria

1. WHEN usuário digita nova senha THEN SHALL calcular força da senha
2. WHEN senha tem < 8 caracteres THEN SHALL exibir "Fraca" em vermelho
3. WHEN senha tem 8-11 caracteres e 2 tipos THEN SHALL exibir "Média" em amarelo
4. WHEN senha tem 12+ caracteres e 3 tipos THEN SHALL exibir "Forte" em verde
5. WHEN senha tem 16+ caracteres e 4 tipos THEN SHALL exibir "Muito Forte" em verde escuro
6. WHEN força é calculada THEN SHALL exibir barra de progresso visual
7. WHEN senha é forte THEN SHALL exibir mensagem encorajadora

### Requirement 9: Atualização da Senha

**User Story:** Como sistema, eu quero atualizar a senha do usuário de forma segura, para que a nova senha seja armazenada corretamente.

#### Acceptance Criteria

1. WHEN usuário submete formulário válido THEN SHALL enviar requisição ao backend
2. WHEN backend recebe requisição THEN SHALL validar senha atual novamente
3. WHEN senha atual é válida THEN SHALL fazer hash bcrypt da nova senha
4. WHEN hash é criado THEN SHALL usar cost factor 12 (segurança adequada)
5. WHEN hash é criado THEN SHALL atualizar campo `password_hash` no banco
6. WHEN senha é atualizada THEN SHALL definir `temporary_password: false`
7. WHEN atualização é bem-sucedida THEN SHALL retornar status 200

### Requirement 10: Geração de Novo JWT

**User Story:** Como sistema, eu quero gerar novo JWT após troca de senha, para que o usuário possa acessar o sistema normalmente.

#### Acceptance Criteria

1. WHEN senha é atualizada THEN SHALL gerar novo JWT
2. WHEN JWT é gerado THEN SHALL incluir `temporary_password: false`
3. WHEN JWT é gerado THEN SHALL incluir `requires_password_change: false`
4. WHEN JWT é gerado THEN SHALL ter expiração normal (7 dias)
5. WHEN JWT é gerado THEN SHALL ser retornado ao frontend
6. WHEN frontend recebe JWT THEN SHALL armazenar em cookie HttpOnly
7. WHEN cookie é definido THEN SHALL substituir JWT antigo

### Requirement 11: Redirecionamento Pós-Troca

**User Story:** Como usuário, eu quero ser redirecionado para o dashboard após trocar senha, para que eu possa começar a usar o sistema.

#### Acceptance Criteria

1. WHEN senha é trocada com sucesso THEN SHALL exibir mensagem de sucesso
2. WHEN mensagem é exibida THEN SHALL mostrar "Senha alterada com sucesso!"
3. WHEN mensagem é exibida THEN SHALL ter ícone de sucesso verde
4. WHEN sucesso é confirmado THEN SHALL aguardar 2 segundos
5. WHEN tempo passa THEN SHALL redirecionar para `/dashboard`
6. WHEN redirecionamento ocorre THEN SHALL usar novo JWT
7. WHEN dashboard carrega THEN SHALL funcionar normalmente (sem bloqueios)

### Requirement 12: Audit Log

**User Story:** Como sistema, eu quero registrar todas as trocas de senha temporária, para que haja rastreabilidade completa.

#### Acceptance Criteria

1. WHEN senha temporária é criada THEN SHALL registrar "TEMPORARY_PASSWORD_CREATED"
2. WHEN usuário faz login com senha temporária THEN SHALL registrar "LOGIN_WITH_TEMPORARY_PASSWORD"
3. WHEN usuário acessa página de troca THEN SHALL registrar "PASSWORD_CHANGE_PAGE_ACCESSED"
4. WHEN senha é trocada THEN SHALL registrar "TEMPORARY_PASSWORD_CHANGED"
5. WHEN registro é criado THEN SHALL incluir: user_id, tenant_id, ip_address, user_agent, timestamp
6. WHEN tentativa falha THEN SHALL registrar "PASSWORD_CHANGE_FAILED" com motivo
7. WHEN audit log é consultado THEN SHALL ser possível filtrar por tipo de evento

### Requirement 13: Notificação por Email

**User Story:** Como usuário, eu quero receber email confirmando troca de senha, para que eu saiba que minha conta está segura.

#### Acceptance Criteria

1. WHEN senha é trocada THEN SHALL enviar email de confirmação
2. WHEN email é enviado THEN SHALL incluir data/hora da troca
3. WHEN email é enviado THEN SHALL incluir endereço IP de origem
4. WHEN email é enviado THEN SHALL incluir navegador/dispositivo usado
5. WHEN email é enviado THEN SHALL incluir link "Não fui eu - reportar"
6. WHEN usuário clica em reportar THEN SHALL bloquear conta imediatamente
7. WHEN conta é bloqueada THEN SHALL notificar super admin

### Requirement 14: Tratamento de Erros

**User Story:** Como usuário, eu quero mensagens de erro claras, para que eu saiba como corrigir problemas.

#### Acceptance Criteria

1. WHEN erro de rede ocorre THEN SHALL exibir "Erro de conexão. Tente novamente"
2. WHEN senha atual incorreta THEN SHALL exibir "Senha atual incorreta"
3. WHEN nova senha fraca THEN SHALL exibir requisitos não atendidos
4. WHEN senhas não coincidem THEN SHALL exibir "As senhas não coincidem"
5. WHEN erro 500 ocorre THEN SHALL exibir "Erro no servidor. Contate o suporte"
6. WHEN sessão expira THEN SHALL redirecionar para login com mensagem
7. WHEN erro é exibido THEN SHALL ter botão "Tentar Novamente"

### Requirement 15: Acessibilidade

**User Story:** Como usuário com deficiência, eu quero que a página de troca de senha seja acessível, para que eu possa usar o sistema.

#### Acceptance Criteria

1. WHEN página carrega THEN SHALL ter labels apropriados em todos os campos
2. WHEN erro ocorre THEN SHALL ser anunciado por screen readers
3. WHEN formulário é navegado THEN SHALL funcionar apenas com teclado (Tab, Enter)
4. WHEN campo tem erro THEN SHALL ter aria-invalid="true"
5. WHEN senha é visível/oculta THEN SHALL ter aria-label descritivo
6. WHEN botão está desabilitado THEN SHALL ter aria-disabled="true"
7. WHEN página carrega THEN SHALL ter contraste adequado (WCAG AA)

---

## Non-Functional Requirements

### Security

1. **Senha Temporária:** Deve ser trocada obrigatoriamente no primeiro login
2. **Hash:** Bcrypt com cost factor 12
3. **Validação:** Senha mínima de 8 caracteres com complexidade
4. **Rate Limiting:** Máximo 3 tentativas de troca por 5 minutos
5. **Audit Trail:** Registro completo de todas as tentativas

### Performance

1. **Validação:** Feedback em tempo real (< 100ms)
2. **Atualização:** Troca de senha completa em < 2 segundos
3. **Redirecionamento:** Transição suave para dashboard

### Usability

1. **Clareza:** Mensagens claras sobre o que fazer
2. **Feedback:** Indicador de força da senha em tempo real
3. **Prevenção de Erros:** Validação antes de submeter
4. **Confirmação:** Mensagem de sucesso clara

### Compliance

1. **OWASP:** Seguir recomendações de gestão de senhas
2. **NIST:** Seguir guidelines de autenticação
3. **LGPD:** Transparência sobre troca de senha

---

## Success Criteria

A implementação será considerada bem-sucedida quando:

1. ✅ Usuário com senha temporária é forçado a trocar no primeiro login
2. ✅ Usuário não consegue acessar dashboard sem trocar senha
3. ✅ Validação de senha forte funciona corretamente
4. ✅ Indicador de força da senha é claro e útil
5. ✅ Troca de senha é registrada em audit log
6. ✅ Email de confirmação é enviado
7. ✅ Novo JWT é gerado sem flag de senha temporária
8. ✅ Usuário é redirecionado para dashboard após troca
9. ✅ Página é acessível (WCAG AA)
10. ✅ Tratamento de erros é robusto

---

## Out of Scope

O que NÃO será feito nesta implementação:

❌ Histórico de senhas anteriores (prevenir reuso)
❌ Política de expiração de senha (trocar a cada X dias)
❌ Autenticação de dois fatores (2FA)
❌ Recuperação de senha via SMS
❌ Validação contra dicionário de senhas comuns
❌ Requisitos de senha personalizáveis por tenant
❌ Biometria ou autenticação avançada

---

## Risks and Mitigations

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Usuário esquece senha temporária | Média | Médio | Super admin pode gerar nova senha |
| Usuário não recebe email | Média | Baixo | Senha temporária ainda funciona |
| Validação muito restritiva | Baixa | Médio | Requisitos baseados em padrões (NIST) |
| Performance na validação | Baixa | Baixo | Validação client-side + debounce |
| Usuário fecha página durante troca | Média | Baixo | Pode tentar novamente no próximo login |

---

## Dependencies

### Externas
- Biblioteca bcrypt para hash de senhas
- Biblioteca de validação de força de senha (zxcvbn ou similar)
- Serviço de email (SMTP ou SendGrid)

### Internas
- Sistema de autenticação existente (JWT)
- Banco de dados PostgreSQL
- Tabela `users` com campo `temporary_password`
- Tabela `audit_log`
- Middleware de autenticação Next.js

---

## Database Changes

### Alteração na Tabela `users`

```sql
-- Adicionar coluna para marcar senha temporária
ALTER TABLE users 
ADD COLUMN temporary_password BOOLEAN DEFAULT false;

-- Atualizar usuários existentes criados com senha temporária
UPDATE users 
SET temporary_password = true 
WHERE created_at >= '2025-10-29' -- Data em que feature de senha temporária foi implementada
AND role = 'master'; -- Apenas usuários master criados por super admin
```

### Índice para Performance

```sql
-- Índice para consultas rápidas de usuários com senha temporária
CREATE INDEX idx_users_temporary_password 
ON users(temporary_password) 
WHERE temporary_password = true;
```

---

## API Endpoints

### POST /api/auth/change-password

**Request:**
```json
{
  "current_password": "KZM1bYZ2YVu7",
  "new_password": "MyNewSecureP@ss123",
  "confirm_password": "MyNewSecureP@ss123"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Senha alterada com sucesso",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Error):**
```json
{
  "success": false,
  "error": "Senha atual incorreta"
}
```

---

## Timeline Estimate

- **Fase 1 - Database Migration:** 1 hora
- **Fase 2 - Backend API:** 2-3 horas
- **Fase 3 - Frontend Page:** 3-4 horas
- **Fase 4 - Middleware:** 1-2 horas
- **Fase 5 - Testes e Validação:** 2-3 horas

**Total:** 9-13 horas (1-2 dias de trabalho)

---

## Testing Strategy

### Unit Tests
- Validação de força de senha
- Hash de senha com bcrypt
- Comparação de senhas

### Integration Tests
- Fluxo completo de troca de senha
- Middleware bloqueando rotas
- Geração de novo JWT

### E2E Tests
- Login com senha temporária
- Redirecionamento forçado
- Troca de senha bem-sucedida
- Acesso ao dashboard após troca

### Security Tests
- Tentativas de bypass do middleware
- Validação de senha fraca
- Rate limiting

---

**Documento criado em:** 30 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Aguardando Aprovação

