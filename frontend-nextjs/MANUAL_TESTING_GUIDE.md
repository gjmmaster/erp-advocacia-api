# Guia de Testes Manuais - Next.js BFF

**Data:** 16 de Outubro de 2025  
**Versão:** 1.0

---

## 🎯 Objetivo

Este guia fornece instruções passo a passo para testar manualmente todas as funcionalidades do sistema Next.js BFF.

---

## 🚀 Pré-requisitos

### 1. Ambiente Configurado
- [ ] Node.js 18+ instalado
- [ ] Backend Clojure rodando em `http://localhost:3000`
- [ ] Frontend Next.js rodando em `http://localhost:3001`
- [ ] Variáveis de ambiente configuradas (`.env.local`)

### 2. Comandos de Inicialização
```bash
# Terminal 1: Backend Clojure
cd /path/to/backend
lein run

# Terminal 2: Frontend Next.js
cd frontend-nextjs
npm install
npm run dev
```

---

## 📋 Checklist de Testes

### ✅ Teste 1: Inicialização e Redirecionamento

**Objetivo:** Verificar que a aplicação inicia corretamente e redireciona para login.

**Passos:**
1. Abra o navegador em `http://localhost:3001`
2. Verifique que você é redirecionado automaticamente para `/super-admin/login`

**Resultado Esperado:**
- ✅ Redirecionamento automático funciona
- ✅ Página de login é exibida
- ✅ Não há erros no console

---

### ✅ Teste 2: Login com Credenciais Inválidas

**Objetivo:** Verificar tratamento de erros de autenticação.

**Passos:**
1. Na página de login, insira:
   - Email: `wrong@email.com`
   - Senha: `wrongpassword`
2. Clique em "Entrar"

**Resultado Esperado:**
- ✅ Mensagem de erro é exibida: "Credenciais inválidas"
- ✅ Formulário permanece habilitado
- ✅ Usuário permanece na página de login
- ✅ Não há erros no console

---

### ✅ Teste 3: Login com Credenciais Válidas

**Objetivo:** Verificar autenticação bem-sucedida.

**Passos:**
1. Na página de login, insira:
   - Email: `super@admin.com` (ou suas credenciais)
   - Senha: sua senha correta
2. Clique em "Entrar"

**Resultado Esperado:**
- ✅ Redirecionamento para `/super-admin/dashboard`
- ✅ Dashboard é exibido com lista de tenants
- ✅ Cookies `access_token` e `refresh_token` são criados (verificar DevTools > Application > Cookies)
- ✅ Cookies têm flag `HttpOnly` e `Secure`

---

### ✅ Teste 4: Proteção de Rotas

**Objetivo:** Verificar que rotas protegidas não são acessíveis sem autenticação.

**Passos:**
1. Faça logout (se estiver logado)
2. Tente acessar diretamente `http://localhost:3001/super-admin/dashboard`

**Resultado Esperado:**
- ✅ Redirecionamento automático para `/super-admin/login`
- ✅ Mensagem ou indicação de que precisa fazer login

---

### ✅ Teste 5: Listar Tenants

**Objetivo:** Verificar que a lista de tenants é carregada corretamente.

**Passos:**
1. Faça login no sistema
2. Observe a tabela de tenants no dashboard

**Resultado Esperado:**
- ✅ Tabela é exibida com colunas: ID, Nome, Subdomínio, Limite de Operadores, Criado em, Ações
- ✅ Dados dos tenants são exibidos corretamente
- ✅ Datas estão formatadas (DD/MM/YYYY HH:mm)
- ✅ Botões "Editar" e "Deletar" estão visíveis

**Se não houver tenants:**
- ✅ Mensagem "Nenhum escritório encontrado" é exibida

---

### ✅ Teste 6: Criar Novo Tenant

**Objetivo:** Verificar criação de tenant.

**Passos:**
1. No dashboard, clique em "➕ Novo Escritório"
2. Preencha o formulário:
   - Nome: `Escritório Teste`
   - Email: `teste@escritorio.com`
   - Limite de Operadores: `5`
3. Clique em "Criar"

**Resultado Esperado:**
- ✅ Modal fecha automaticamente
- ✅ Novo tenant aparece na tabela
- ✅ Mensagem de sucesso é exibida (se implementada)
- ✅ Tabela é atualizada automaticamente

**Validações:**
- ✅ Não permite criar sem nome
- ✅ Não permite criar sem email
- ✅ Valida formato de email

---

### ✅ Teste 7: Editar Tenant

**Objetivo:** Verificar edição de tenant.

**Passos:**
1. Na tabela, clique em "✏️ Editar" em um tenant
2. Modifique os dados:
   - Nome: `Escritório Teste Editado`
   - Limite de Operadores: `10`
3. Clique em "Salvar"

**Resultado Esperado:**
- ✅ Modal fecha automaticamente
- ✅ Dados atualizados aparecem na tabela
- ✅ Mensagem de sucesso é exibida (se implementada)

---

### ✅ Teste 8: Deletar Tenant

**Objetivo:** Verificar exclusão de tenant.

**Passos:**
1. Na tabela, clique em "🗑️ Deletar" em um tenant
2. Confirme a exclusão no prompt

**Resultado Esperado:**
- ✅ Tenant é removido da tabela
- ✅ Tabela é atualizada automaticamente
- ✅ Mensagem de sucesso é exibida (se implementada)

---

### ✅ Teste 9: Renovação Automática de Token

**Objetivo:** Verificar que o token é renovado automaticamente.

**Passos:**
1. Faça login no sistema
2. Abra DevTools > Application > Cookies
3. Observe o cookie `access_token`
4. Aguarde 14 minutos (ou modifique o tempo de expiração para testar mais rápido)
5. Faça uma ação no dashboard (ex: abrir modal)

**Resultado Esperado:**
- ✅ Token é renovado automaticamente
- ✅ Novo `access_token` é criado
- ✅ Usuário não é deslogado
- ✅ Não há interrupção na experiência

---

### ✅ Teste 10: Logout

**Objetivo:** Verificar que o logout funciona corretamente.

**Passos:**
1. No dashboard, clique em "Sair" (se implementado) ou:
2. Faça uma requisição manual para `/api/auth/logout`
3. Tente acessar `/super-admin/dashboard`

**Resultado Esperado:**
- ✅ Cookies são removidos
- ✅ Redirecionamento para `/super-admin/login`
- ✅ Não é possível acessar rotas protegidas

---

### ✅ Teste 11: Timeout de Conexão

**Objetivo:** Verificar tratamento de timeout.

**Passos:**
1. Pare o backend Clojure
2. Tente fazer login

**Resultado Esperado:**
- ✅ Mensagem de erro: "Servidor indisponível" ou "Timeout"
- ✅ Formulário permanece habilitado
- ✅ Não há crash da aplicação

---

### ✅ Teste 12: Validação de Formulários

**Objetivo:** Verificar validações client-side.

**Passos:**
1. Tente criar tenant sem preencher campos obrigatórios
2. Tente inserir email inválido
3. Tente inserir limite de operadores negativo

**Resultado Esperado:**
- ✅ Validações HTML5 funcionam
- ✅ Mensagens de erro são claras
- ✅ Formulário não é enviado com dados inválidos

---

### ✅ Teste 13: Responsividade

**Objetivo:** Verificar que a interface funciona em diferentes tamanhos de tela.

**Passos:**
1. Abra DevTools > Toggle Device Toolbar
2. Teste em diferentes resoluções:
   - Mobile (375px)
   - Tablet (768px)
   - Desktop (1920px)

**Resultado Esperado:**
- ✅ Layout se adapta corretamente
- ✅ Tabela é scrollável em mobile
- ✅ Modals são responsivos
- ✅ Botões são clicáveis em touch

---

### ✅ Teste 14: Performance

**Objetivo:** Verificar que a aplicação é rápida.

**Passos:**
1. Abra DevTools > Network
2. Faça login
3. Observe os tempos de resposta

**Resultado Esperado:**
- ✅ Login < 1 segundo
- ✅ Carregamento de tenants < 500ms
- ✅ Criação de tenant < 1 segundo
- ✅ Sem requisições duplicadas

---

### ✅ Teste 15: Segurança

**Objetivo:** Verificar implementações de segurança.

**Passos:**
1. Abra DevTools > Application > Cookies
2. Verifique os cookies `access_token` e `refresh_token`

**Resultado Esperado:**
- ✅ Cookies têm flag `HttpOnly` (não acessíveis via JavaScript)
- ✅ Cookies têm flag `Secure` (apenas HTTPS em produção)
- ✅ Cookies têm flag `SameSite=Strict`
- ✅ Token não aparece em nenhuma resposta JSON

---

## 🐛 Troubleshooting

### Problema: "Cannot connect to backend"
**Solução:**
1. Verifique se o backend está rodando: `curl http://localhost:3000/health`
2. Verifique a variável `NEXT_PUBLIC_BACKEND_URL` no `.env.local`

### Problema: "Unauthorized" após login
**Solução:**
1. Limpe os cookies do navegador
2. Verifique se o JWT_SECRET é o mesmo no backend e frontend
3. Verifique logs do backend

### Problema: Modals não abrem
**Solução:**
1. Verifique console do navegador por erros
2. Verifique se os componentes foram importados corretamente

### Problema: Tabela não carrega
**Solução:**
1. Verifique se há tenants no banco de dados
2. Verifique logs da API route `/api/admin/tenants`
3. Verifique permissões do usuário

---

## ✅ Checklist Final

Antes de considerar os testes completos, verifique:

- [ ] Todos os 15 testes passaram
- [ ] Não há erros no console do navegador
- [ ] Não há warnings no terminal do Next.js
- [ ] Cookies estão configurados corretamente
- [ ] Performance está aceitável
- [ ] Interface é responsiva
- [ ] Validações funcionam
- [ ] Tratamento de erros está correto

---

## 📊 Relatório de Testes

Após completar os testes, preencha:

**Data:** _______________  
**Testador:** _______________  
**Ambiente:** [ ] Dev [ ] Staging [ ] Produção

**Resultados:**
- Testes Passados: _____ / 15
- Testes Falhados: _____ / 15
- Bugs Encontrados: _____

**Observações:**
_______________________________________
_______________________________________
_______________________________________

---

**Próximo Passo:** Se todos os testes passaram, você está pronto para o deploy! 🚀
