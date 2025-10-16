# ✅ Checklist de Deploy - Render

## 📋 Antes de Começar

- [ ] Código commitado no Git
- [ ] Backend Clojure já deployado no Render
- [ ] Conta no Render criada

---

## 🚀 Deploy no Render

### Passo 1: Criar Serviço
- [ ] Acessar https://dashboard.render.com
- [ ] Clicar em "New +" → "Web Service" (NÃO Static Site!)
- [ ] Conectar repositório Git

### Passo 2: Configurar Serviço

**Opção A: Docker (Recomendado - Padronizado)**
- [ ] **Name:** `juridico-frontend-nextjs`
- [ ] **Region:** `Oregon (US West)`
- [ ] **Branch:** `main`
- [ ] **Root Directory:** `frontend-nextjs`
- [ ] **Runtime:** `Docker`
- [ ] **Dockerfile Path:** `frontend-nextjs/Dockerfile`
- [ ] **Plan:** `Free`

**Opção B: Node.js (Alternativa)**
- [ ] **Name:** `juridico-frontend-nextjs`
- [ ] **Region:** `Oregon (US West)`
- [ ] **Branch:** `main`
- [ ] **Root Directory:** `frontend-nextjs`
- [ ] **Runtime:** `Node`
- [ ] **Build Command:** `npm install && npm run build`
- [ ] **Start Command:** `npm start`
- [ ] **Plan:** `Free`

### Passo 3: Variáveis de Ambiente
- [ ] `NODE_ENV` = `production`
- [ ] `BACKEND_API_URL` = URL do seu backend (ex: `https://seu-backend.onrender.com`)
- [ ] `JWT_SECRET` = Mesma chave do backend

### Passo 4: Deploy
- [ ] Clicar em "Create Web Service"
- [ ] Aguardar build (5-10 minutos)
- [ ] Verificar logs para erros

---

## ✅ Verificação Pós-Deploy

### Testes Básicos
- [ ] Aplicação está acessível (URL do Render)
- [ ] Página de login carrega
- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] Lista de tenants aparece
- [ ] Criar tenant funciona
- [ ] Editar tenant funciona
- [ ] Deletar tenant funciona
- [ ] Logout funciona

### Verificações Técnicas
- [ ] HTTPS está ativo (cadeado verde)
- [ ] Cookies estão sendo criados
- [ ] Não há erros no console do navegador
- [ ] API Routes funcionam (`/api/auth/login`, etc)
- [ ] Middleware protege rotas

---

## 🐛 Se Algo Der Errado

### Build Failed
- [ ] Verificar logs no Render
- [ ] Verificar se `package.json` está correto
- [ ] Verificar se Root Directory está correto

### Cannot Connect to Backend
- [ ] Verificar se `BACKEND_API_URL` está correto
- [ ] Verificar se backend está rodando
- [ ] Adicionar CORS no backend para aceitar frontend

### Unauthorized
- [ ] Verificar se `JWT_SECRET` é igual no frontend e backend
- [ ] Limpar cookies do navegador
- [ ] Tentar login novamente

---

## 📊 Status

**Data do Deploy:** _______________  
**URL do Frontend:** _______________  
**Status:** [ ] Sucesso [ ] Com Problemas

**Notas:**
_______________________________________
_______________________________________
