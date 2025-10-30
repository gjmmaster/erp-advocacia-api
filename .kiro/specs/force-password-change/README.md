# Spec: Forçar Troca de Senha Temporária

**Status:** ✅ Aprovado - Pronto para Implementação  
**Prioridade:** Alta (Segurança)  
**Estimativa:** 9-13 horas (1-2 dias)  
**Data de Criação:** 30 de Outubro de 2025

---

## 📋 Overview

Esta spec implementa a funcionalidade de forçar usuários a trocarem suas senhas temporárias no primeiro login, garantindo que apenas o usuário final conheça sua senha definitiva.

### Problema

Atualmente, quando um tenant é criado:
- Super admin vê a senha temporária
- Usuário pode continuar usando senha temporária indefinidamente
- Senha pode ter sido vista por outras pessoas (email, WhatsApp)
- Não há garantia de que apenas o usuário final conhece a senha

### Solução

- Detectar quando usuário faz login com senha temporária
- Forçar redirecionamento para tela de "Criar Nova Senha"
- Bloquear acesso ao dashboard até que senha seja trocada
- Validar força da nova senha
- Registrar troca em audit log

---

## 📁 Arquivos da Spec

- **requirements.md** - 15 requirements detalhados com acceptance criteria
- **design.md** - Design técnico completo com arquitetura e componentes
- **tasks.md** - 19 tasks de implementação passo a passo
- **README.md** - Este arquivo

---

## 🎯 Objetivos

1. **Segurança:** Garantir que apenas o usuário final conhece sua senha
2. **Compliance:** Seguir melhores práticas (OWASP, NIST)
3. **Usabilidade:** Processo simples e claro
4. **Auditoria:** Rastrear todas as trocas de senha

---

## 🏗️ Componentes Principais

### Backend (Clojure)
- Migration: Adicionar coluna `temporary_password` na tabela `users`
- Handler: `change-password-handler` para processar troca
- Rota: POST `/api/auth/change-password`
- Rate limiting: 3 tentativas / 5 minutos
- Audit log: Registrar todas as ações

### Frontend (Next.js)
- Middleware: Verificar flag e redirecionar
- API Route: `/api/auth/change-password` (BFF)
- Página: `/change-password` com formulário
- Componente: `PasswordStrengthIndicator`
- Validação: Client-side em tempo real

---

## 🚀 Como Começar

### 1. Revisar Documentação

Leia os documentos na seguinte ordem:
1. `requirements.md` - Entender o que será implementado
2. `design.md` - Entender como será implementado
3. `tasks.md` - Ver o plano de implementação

### 2. Preparar Ambiente

```bash
# Backend
cd ~/erp-advocacia-api
git checkout -b feat/force-password-change

# Frontend
cd frontend-nextjs
npm install
```

### 3. Começar Implementação

Abra o arquivo `tasks.md` e:
1. Comece pela Task 1 (Database Migration)
2. Execute cada task em ordem
3. Marque como completo quando terminar
4. Teste antes de prosseguir para próxima task

### 4. Executar Tasks

Você pode usar o Kiro para executar as tasks:
1. Abra `tasks.md` no editor
2. Clique em "Start task" ao lado de cada task
3. Kiro irá implementar a task automaticamente
4. Revise o código gerado
5. Teste e valide

---

## 📊 Progresso

### Fase 1: Database e Backend (4-6 horas)
- [ ] Task 1: Database Migration
- [ ] Task 2: Atualizar Provision Handler
- [ ] Task 3: Atualizar Login Handler
- [ ] Task 4: Implementar Change Password Handler
- [ ] Task 5: Adicionar Rota
- [ ] Task 6: Rate Limiting
- [ ] Task 7: Email

### Fase 2: Frontend (3-4 horas)
- [ ] Task 8: Atualizar Middleware
- [ ] Task 9: Criar API Route
- [ ] Task 10: Criar Página
- [ ] Task 11: Password Strength Indicator
- [ ] Task 12: Estilos CSS
- [ ] Task 13: Acessibilidade

### Fase 3: Finalização (2-3 horas)
- [ ] Task 14: Audit Log
- [ ] Task 15: Testes de Integração (opcional)
- [ ] Task 16: Testes E2E (opcional)
- [ ] Task 17: Documentação
- [ ] Task 18: Deploy Staging
- [ ] Task 19: Deploy Produção

---

## ✅ Checklist de Validação

Após implementação, validar:

### Funcionalidade
- [ ] Novo tenant criado tem `temporary_password: true`
- [ ] Login com senha temporária redireciona para `/change-password`
- [ ] Não é possível acessar dashboard sem trocar senha
- [ ] Validação de senha forte funciona
- [ ] Indicador de força da senha é claro
- [ ] Troca de senha bem-sucedida gera novo JWT
- [ ] Dashboard carrega normalmente após troca

### Segurança
- [ ] Senha é hasheada com bcrypt cost 12
- [ ] Rate limiting funciona (3 tentativas / 5 min)
- [ ] Novo JWT não tem flag `temporary_password`
- [ ] Audit log registra todas as ações

### UX
- [ ] Mensagens de erro são claras
- [ ] Feedback em tempo real funciona
- [ ] Tela de sucesso é exibida
- [ ] Redirecionamento é suave

### Acessibilidade
- [ ] Navegação por teclado funciona
- [ ] Screen reader anuncia erros
- [ ] Contraste de cores adequado (WCAG AA)
- [ ] Labels e aria-* apropriados

---

## 🐛 Troubleshooting

### Problema: Migration falha

**Solução:**
```sql
-- Verificar se coluna já existe
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'temporary_password';

-- Se existir, dropar e recriar
ALTER TABLE users DROP COLUMN temporary_password;
-- Executar migration novamente
```

### Problema: Middleware não redireciona

**Solução:**
- Verificar que JWT contém campo `temporary_password`
- Verificar logs do middleware
- Testar com `console.log` no middleware
- Validar que cookie está sendo lido corretamente

### Problema: Validação de senha não funciona

**Solução:**
- Verificar regex de validação
- Testar função `validatePassword` isoladamente
- Verificar que estado está sendo atualizado
- Usar React DevTools para inspecionar estado

---

## 📚 Recursos

### Documentação Relacionada
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [NIST Digital Identity Guidelines](https://pages.nist.gov/800-63-3/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

### Código de Referência
- Spec de Impersonation: `.kiro/specs/impersonation-password-reset/`
- Spec de Tenant Auth: `.kiro/specs/tenant-authentication/`
- Handler de Login: `src/juridico/api/handlers.clj`

---

## 🎉 Próximos Passos

Após completar esta spec:

1. **Testar em produção** por 48h
2. **Monitorar métricas:**
   - Taxa de sucesso (meta: > 95%)
   - Tempo médio (meta: < 5s)
   - Tentativas falhadas (meta: < 5%)

3. **Implementar próxima feature:**
   - Impersonation (super admin acessar como tenant)
   - Reset de senha self-service
   - Dashboard com dados reais

---

## 📞 Suporte

Se tiver dúvidas ou problemas:

1. Consulte `design.md` para detalhes técnicos
2. Consulte `tasks.md` para ordem de implementação
3. Revise specs relacionadas em `.kiro/specs/`
4. Verifique documentação em `STATUS_E_PROXIMOS_PASSOS.md`

---

**Boa implementação! 🚀**

