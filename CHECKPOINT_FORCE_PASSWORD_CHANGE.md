# 🔖 Checkpoint - Implementação de Forçar Troca de Senha Temporária

**Data de Criação:** 30 de Outubro de 2025  
**Status:** 🔄 Em Implementação  
**Feature:** Forçar Troca de Senha Temporária no Primeiro Login

---

## 📍 Onde Estamos

### ✅ Concluído

1. **Spec Completa Criada**
   - ✅ Requirements document (15 requirements detalhados)
   - ✅ Design document (arquitetura completa)
   - ✅ Tasks document (19 tasks de implementação)
   - ✅ README da spec

2. **Documentação Atualizada**
   - ✅ Este checkpoint criado
   - ✅ Referências cruzadas com documentação existente

### 🔄 Em Andamento

**Próximo Passo:** Começar implementação pela Task 1 (Database Migration)

### ⏳ Pendente

- Todas as 19 tasks de implementação
- Testes
- Deploy

---

## 📁 Localização dos Arquivos da Spec

```
.kiro/specs/force-password-change/
├── README.md          ← Visão geral e guia de início
├── requirements.md    ← 15 requirements com acceptance criteria
├── design.md         ← Design técnico completo
└── tasks.md          ← 19 tasks de implementação (COMEÇAR AQUI)
```

---

## 🎯 Objetivo da Feature

**Problema:**
- Usuários criados com senha temporária podem continuar usando ela indefinidamente
- Senha temporária pode ter sido vista por outras pessoas
- Não há garantia de que apenas o usuário final conhece a senha

**Solução:**
- Forçar troca de senha no primeiro login
- Bloquear acesso ao dashboard até troca ser concluída
- Validar força da nova senha
- Registrar tudo em audit log

---

## 📋 Plano de Implementação

### Fase 1: Database e Backend (4-6 horas)

**Tasks 1-7:**
- [ ] 1. Database Migration - Adicionar coluna `temporary_password`
- [ ] 2. Atualizar Provision Tenant Handler
- [ ] 3. Atualizar Login Handler (incluir flag no JWT)
- [ ] 4. Implementar Change Password Handler
  - [ ] 4.1 Validação de senha
  - [ ] 4.2 Handler principal
  - [ ] 4.3 Atualização no banco
  - [ ] 4.4 Geração de novo JWT
  - [ ] 4.5 Testes unitários (opcional)
- [ ] 5. Adicionar Rota POST `/api/auth/change-password`
- [ ] 6. Implementar Rate Limiting (3 tentativas / 5 min)
- [ ] 7. Implementar Envio de Email de confirmação

### Fase 2: Frontend (3-4 horas)

**Tasks 8-13:**
- [ ] 8. Atualizar Middleware (verificar flag e redirecionar)
- [ ] 9. Criar API Route `/api/auth/change-password`
- [ ] 10. Criar Página `/change-password`
  - [ ] 10.1 Estrutura básica
  - [ ] 10.2 Formulário HTML
  - [ ] 10.3 Validação client-side
  - [ ] 10.4 Submit do formulário
  - [ ] 10.5 Tela de sucesso
- [ ] 11. Criar Password Strength Indicator
  - [ ] 11.1 Cálculo de força
  - [ ] 11.2 UI do indicador
- [ ] 12. Criar Estilos CSS
- [ ] 13. Implementar Acessibilidade (WCAG AA)

### Fase 3: Finalização (2-3 horas)

**Tasks 14-19:**
- [ ] 14. Implementar Audit Log completo
- [ ] 15. Testes de Integração (opcional)
- [ ] 16. Testes E2E (opcional)
- [ ] 17. Documentação
- [ ] 18. Deploy em Staging
- [ ] 19. Deploy em Produção

---

## 🚀 Como Continuar de Onde Paramos

### Opção 1: Implementação Automática com Kiro

```bash
# 1. Abrir o arquivo de tasks
code .kiro/specs/force-password-change/tasks.md

# 2. No Kiro, dizer:
"Execute a Task 1 da spec force-password-change"

# 3. Kiro irá:
# - Ler requirements.md e design.md
# - Implementar a task
# - Marcar como completa
# - Você revisa e testa

# 4. Repetir para cada task
```

### Opção 2: Implementação Manual

```bash
# 1. Ler documentação
cat .kiro/specs/force-password-change/README.md
cat .kiro/specs/force-password-change/requirements.md
cat .kiro/specs/force-password-change/design.md

# 2. Abrir tasks.md
code .kiro/specs/force-password-change/tasks.md

# 3. Começar pela Task 1
# - Criar arquivo de migration SQL
# - Executar no banco local
# - Testar
# - Marcar como completa

# 4. Seguir ordem sequencial
```

### Opção 3: Implementação Híbrida (Recomendado)

```bash
# Backend: Deixar Kiro implementar
"Execute as Tasks 1-7 da spec force-password-change"

# Frontend: Implementar manualmente (mais controle sobre UI)
# Tasks 8-13

# Finalização: Deixar Kiro ajudar
"Execute as Tasks 14-19 da spec force-password-change"
```

---

## 📚 Documentação de Referência

### Specs Relacionadas

1. **Tenant Authentication** (implementada)
   - Localização: `.kiro/specs/tenant-authentication/`
   - Relevante: Como login funciona atualmente
   - Ver: `requirements.md`, `design.md`

2. **Impersonation e Password Reset** (planejada)
   - Localização: `.kiro/specs/impersonation-password-reset/`
   - Relevante: Requirement 11 sobre trocar senha temporária
   - Ver: `requirements.md`

3. **Next.js BFF Migration** (implementada)
   - Localização: `.kiro/specs/nextjs-bff-migration/`
   - Relevante: Como middleware e API routes funcionam
   - Ver: `design.md`

### Documentação do Projeto

1. **Status e Próximos Passos**
   - Arquivo: `STATUS_E_PROXIMOS_PASSOS.md`
   - Mostra: Roadmap completo do projeto
   - Feature atual: Item 1 da Prioridade Alta

2. **Leia-me Primeiro**
   - Arquivo: `LEIA_ME_PRIMEIRO.md`
   - Mostra: Visão geral do projeto
   - Status: Sistema 100% funcional em produção

3. **Resumo Executivo**
   - Arquivo: `RESUMO_EXECUTIVO.md`
   - Mostra: Arquitetura e tecnologias
   - Útil: Entender contexto geral

### Código Existente Relevante

**Backend (Clojure):**
```
src/juridico/api/
├── core.clj              ← Rotas (adicionar nova rota aqui)
├── handlers.clj          ← Handlers (referência para novo handler)
├── auth/
│   └── jwt.clj          ← JWT (modificar para incluir flag)
└── db/
    └── postgres.clj     ← Database (adicionar queries)
```

**Frontend (Next.js):**
```
frontend-nextjs/src/
├── middleware.ts         ← Modificar para verificar flag
├── app/
│   ├── api/auth/        ← Adicionar nova API route aqui
│   └── login/           ← Referência para nova página
└── components/          ← Adicionar novo componente aqui
```

---

## 🔍 Detalhes Técnicos Importantes

### Database Migration

```sql
-- Arquivo: migrations/add_temporary_password_column.sql
ALTER TABLE users 
ADD COLUMN temporary_password BOOLEAN DEFAULT false;

CREATE INDEX idx_users_temporary_password 
ON users(temporary_password) 
WHERE temporary_password = true;

-- Marcar usuários existentes (se necessário)
UPDATE users 
SET temporary_password = true 
WHERE created_at >= '2025-10-29' 
AND role = 'master';
```

### JWT Payload (Atualizado)

```clojure
;; Antes
{:user-id 123
 :email "user@example.com"
 :role "master"
 :tenant-id 456}

;; Depois (com senha temporária)
{:user-id 123
 :email "user@example.com"
 :role "master"
 :tenant-id 456
 :temporary-password true          ;; ← NOVO
 :requires-password-change true}   ;; ← NOVO
```

### Middleware Logic (Next.js)

```typescript
// Verificar se precisa trocar senha
if (payload.temporary_password === true || 
    payload.requires_password_change === true) {
  
  // Permitir apenas estas rotas
  const allowedRoutes = [
    '/change-password',
    '/api/auth/change-password',
    '/api/auth/logout'
  ];

  if (!allowedRoutes.some(route => pathname.startsWith(route))) {
    // Redirecionar para troca de senha
    return NextResponse.redirect(new URL('/change-password', request.url));
  }
}
```

### API Endpoint

```
POST /api/auth/change-password

Request:
{
  "current_password": "TempPass123",
  "new_password": "MyNewP@ss123",
  "confirm_password": "MyNewP@ss123"
}

Response (Success):
{
  "success": true,
  "message": "Senha alterada com sucesso",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (Error):
{
  "success": false,
  "error": "Senha atual incorreta"
}
```

---

## ✅ Checklist de Validação

Após implementar cada fase, validar:

### Fase 1 (Backend)
- [ ] Coluna `temporary_password` existe no banco
- [ ] Índice foi criado
- [ ] Novos tenants têm `temporary_password: true`
- [ ] Login retorna JWT com flag
- [ ] Handler de change password funciona
- [ ] Rota POST `/api/auth/change-password` responde
- [ ] Rate limiting funciona (testar 4 tentativas)
- [ ] Email de confirmação é enviado

### Fase 2 (Frontend)
- [ ] Middleware redireciona para `/change-password`
- [ ] Não é possível acessar `/dashboard` sem trocar senha
- [ ] Página `/change-password` carrega
- [ ] Formulário valida em tempo real
- [ ] Indicador de força da senha funciona
- [ ] Submit envia dados corretamente
- [ ] Tela de sucesso aparece
- [ ] Redirecionamento para dashboard funciona

### Fase 3 (Finalização)
- [ ] Audit log registra todas as ações
- [ ] Testes passam (se implementados)
- [ ] Documentação atualizada
- [ ] Deploy em staging bem-sucedido
- [ ] Testes manuais em staging OK
- [ ] Deploy em produção bem-sucedido
- [ ] Monitoramento ativo

---

## 🐛 Troubleshooting Comum

### Problema: Migration falha

```bash
# Verificar se coluna já existe
psql $DATABASE_URL -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'temporary_password';"

# Se existir, dropar e recriar
psql $DATABASE_URL -c "ALTER TABLE users DROP COLUMN temporary_password;"
# Executar migration novamente
```

### Problema: JWT não contém flag

```clojure
;; Verificar em src/juridico/api/auth/jwt.clj
;; Adicionar log temporário
(log/info "JWT payload:" payload)

;; Verificar que temporary-password está sendo incluído
```

### Problema: Middleware não redireciona

```typescript
// Adicionar logs temporários em middleware.ts
console.log('[MIDDLEWARE] JWT payload:', payload);
console.log('[MIDDLEWARE] temporary_password:', payload.temporary_password);
console.log('[MIDDLEWARE] pathname:', pathname);
```

### Problema: Validação de senha não funciona

```typescript
// Testar função isoladamente
const password = "Test123";
const errors = validatePassword(password);
console.log('Validation errors:', errors);
```

---

## 📊 Métricas de Sucesso

Após deploy, monitorar:

- **Taxa de sucesso:** > 95% das trocas bem-sucedidas
- **Tempo médio:** < 5 segundos do início ao fim
- **Tentativas falhadas:** < 5% do total
- **Usuários com senha temporária:** Deve diminuir ao longo do tempo

---

## 🔄 Rollback Plan

Se algo der errado:

```bash
# 1. Reverter código
git revert <commit-hash>
git push origin main

# 2. Reverter migration (se necessário)
psql $DATABASE_URL -c "ALTER TABLE users DROP COLUMN temporary_password;"
psql $DATABASE_URL -c "DROP INDEX idx_users_temporary_password;"

# 3. Validar sistema
curl https://seu-dominio.com/api/health
```

---

## 📞 Comandos Úteis

### Backend

```bash
# Desenvolvimento local
cd ~/erp-advocacia-api
lein run

# Executar migration
psql $DATABASE_URL -f migrations/add_temporary_password_column.sql

# Ver logs
tail -f logs/app.log

# Testar endpoint
curl -X POST http://localhost:3000/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"current_password":"old","new_password":"New123","confirm_password":"New123"}'
```

### Frontend

```bash
# Desenvolvimento local
cd frontend-nextjs
npm run dev

# Build
npm run build

# Ver logs do middleware
# Adicionar console.log e verificar no terminal
```

### Database

```bash
# Conectar ao banco
psql $DATABASE_URL

# Ver estrutura da tabela users
\d users

# Ver usuários com senha temporária
SELECT id, email, temporary_password FROM users WHERE temporary_password = true;

# Marcar usuário como senha temporária (para teste)
UPDATE users SET temporary_password = true WHERE email = 'test@example.com';
```

---

## 🎯 Próximos Passos Após Esta Feature

Após completar "Forçar Troca de Senha Temporária":

1. **Impersonation** (6-8 horas)
   - Super admin acessar como tenant
   - Spec: `.kiro/specs/impersonation-password-reset/`

2. **Reset de Senha Self-Service** (6-8 horas)
   - Usuário resetar própria senha via email
   - Spec: `.kiro/specs/impersonation-password-reset/`

3. **Dashboard com Dados Reais** (8-12 horas)
   - Estatísticas e gráficos
   - Nova spec a ser criada

---

## 📝 Notas Importantes

1. **Testes marcados como opcionais (*)** podem ser pulados se houver pressão de tempo
2. **Validação manual** é obrigatória antes de deploy em produção
3. **Backup do banco** antes de executar migration em produção
4. **Monitorar logs** por 24h após deploy
5. **Documentar problemas** encontrados para melhorar processo

---

## 🎉 Quando Retomar

**Para retomar de onde parou:**

1. Abra este arquivo: `CHECKPOINT_FORCE_PASSWORD_CHANGE.md`
2. Leia a seção "Como Continuar de Onde Paramos"
3. Abra `.kiro/specs/force-password-change/tasks.md`
4. Veja qual task está marcada como próxima
5. Diga ao Kiro: "Continue a implementação da spec force-password-change"

**Kiro irá:**
- Ler este checkpoint
- Ler a spec completa
- Ver qual task está pendente
- Continuar de onde parou

---

**Documento criado em:** 30 de Outubro de 2025  
**Última atualização:** 30 de Outubro de 2025  
**Status:** 📋 Pronto para Implementação

**Boa implementação! 🚀**

