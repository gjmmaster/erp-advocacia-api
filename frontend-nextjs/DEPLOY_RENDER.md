# 🚀 Deploy Next.js no Render - Guia Completo

**Data:** 16 de Outubro de 2025  
**Tipo de Serviço:** Web Service (não Static Site)

---

## 📋 Por que Web Service?

Next.js precisa de um servidor Node.js rodando porque tem:
- ✅ API Routes (`/api/*`)
- ✅ Middleware server-side
- ✅ Server-side rendering (SSR)
- ✅ Cookies HttpOnly

**Static Site** só funciona para sites 100% estáticos (HTML/CSS/JS).

---

## 🔧 Passo 1: Preparar o Repositório

### 1.1 Commit e Push

```bash
# Na raiz do projeto
git add frontend-nextjs/
git commit -m "feat: adicionar frontend Next.js BFF"
git push origin main
```

---

## 🌐 Passo 2: Criar Web Service no Render

### 2.1 Acessar Render Dashboard

1. Acesse: https://dashboard.render.com
2. Clique em **"New +"** → **"Web Service"**

### 2.2 Conectar Repositório

1. Selecione seu repositório Git
2. Clique em **"Connect"**

### 2.3 Configurar o Serviço

Preencha os campos:

| Campo | Valor |
|-------|-------|
| **Name** | `juridico-frontend-nextjs` |
| **Region** | `Oregon (US West)` |
| **Branch** | `main` |
| **Root Directory** | `frontend-nextjs` |
| **Runtime** | `Node` |
| **Build Command** | `npm install && npm run build` |
| **Start Command** | `npm start` |
| **Plan** | `Free` |

---

## 🔐 Passo 3: Configurar Variáveis de Ambiente

Na seção **"Environment Variables"**, adicione:

### 3.1 Variáveis Obrigatórias

```bash
# 1. Node Environment
NODE_ENV=production

# 2. Backend API URL (URL do seu backend Clojure no Render)
BACKEND_API_URL=https://seu-backend.onrender.com

# 3. JWT Secret (DEVE ser EXATAMENTE o mesmo do backend)
JWT_SECRET=sua-chave-secreta-jwt-aqui
```

### 3.2 Como Encontrar a URL do Backend

1. Vá para o dashboard do Render
2. Clique no seu serviço backend Clojure
3. Copie a URL (ex: `https://juridico-backend.onrender.com`)
4. Cole em `BACKEND_API_URL`

### 3.3 Como Encontrar o JWT_SECRET

**Opção 1: Verificar no Backend**
1. Vá para o serviço backend no Render
2. Clique em **"Environment"**
3. Procure por `JWT_SECRET`
4. Copie o valor

**Opção 2: Se não estiver definido**
- Use: `chave-padrao-para-desenvolvimento-segura`
- **⚠️ IMPORTANTE:** Defina a mesma chave no backend também!

---

## 🚀 Passo 4: Deploy

1. Clique em **"Create Web Service"**
2. Aguarde o build (5-10 minutos)
3. Render vai:
   - Instalar dependências (`npm install`)
   - Fazer build (`npm run build`)
   - Iniciar servidor (`npm start`)

---

## ✅ Passo 5: Verificar Deploy

### 5.1 Verificar Logs

1. No dashboard do Render, clique no serviço
2. Vá para **"Logs"**
3. Procure por:
   ```
   ✓ Ready in Xms
   ✓ Local: http://localhost:10000
   ```

### 5.2 Testar a Aplicação

1. Copie a URL do serviço (ex: `https://juridico-frontend-nextjs.onrender.com`)
2. Acesse: `https://juridico-frontend-nextjs.onrender.com/super-admin/login`
3. Tente fazer login

---

## 🐛 Troubleshooting

### Problema 1: "Build Failed"

**Erro:** `npm install failed`

**Solução:**
```bash
# Verificar se package.json está correto
# Verificar se todas as dependências estão listadas
```

### Problema 2: "Cannot connect to backend"

**Erro:** `ECONNREFUSED` ou `Timeout`

**Solução:**
1. Verificar se `BACKEND_API_URL` está correto
2. Verificar se backend está rodando
3. Verificar se backend aceita requisições do frontend

**Adicionar CORS no backend:**
```clojure
;; No backend Clojure, adicionar:
(def cors-config
  {:allowed-origins ["https://juridico-frontend-nextjs.onrender.com"]
   :allowed-methods [:get :post :put :delete]
   :allowed-headers ["Content-Type" "Authorization"]})
```

### Problema 3: "Unauthorized após login"

**Erro:** Token inválido

**Solução:**
1. Verificar se `JWT_SECRET` é EXATAMENTE igual no frontend e backend
2. Limpar cookies do navegador
3. Tentar login novamente

### Problema 4: "Application Error"

**Erro:** Servidor não inicia

**Solução:**
1. Verificar logs no Render
2. Verificar se `Start Command` está correto: `npm start`
3. Verificar se porta está configurada: `next start -p $PORT`

---

## 🔒 Passo 6: Configurar HTTPS e Domínio (Opcional)

### 6.1 HTTPS Automático

Render fornece HTTPS automaticamente! ✅

### 6.2 Domínio Customizado

1. No dashboard do Render, clique no serviço
2. Vá para **"Settings"** → **"Custom Domain"**
3. Adicione seu domínio (ex: `app.seudominio.com`)
4. Configure DNS conforme instruções

---

## 📊 Passo 7: Monitoramento

### 7.1 Verificar Saúde do Serviço

Render monitora automaticamente:
- ✅ Uptime
- ✅ Response time
- ✅ Memory usage
- ✅ CPU usage

### 7.2 Configurar Alertas

1. Vá para **"Settings"** → **"Notifications"**
2. Configure alertas por email

---

## 🔄 Passo 8: Atualizações Futuras

### 8.1 Deploy Automático

Render faz deploy automático quando você faz push:

```bash
# Fazer mudanças no código
git add .
git commit -m "feat: nova funcionalidade"
git push origin main

# Render detecta e faz deploy automaticamente!
```

### 8.2 Deploy Manual

1. No dashboard do Render
2. Clique em **"Manual Deploy"** → **"Deploy latest commit"**

---

## 📋 Checklist Final

Antes de considerar o deploy completo:

- [ ] Serviço criado como **Web Service** (não Static Site)
- [ ] Root Directory configurado: `frontend-nextjs`
- [ ] Build Command: `npm install && npm run build`
- [ ] Start Command: `npm start`
- [ ] Variável `NODE_ENV=production`
- [ ] Variável `BACKEND_API_URL` configurada
- [ ] Variável `JWT_SECRET` configurada (igual ao backend)
- [ ] Build completou com sucesso
- [ ] Aplicação está acessível via URL
- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] CRUD de tenants funciona

---

## 🎯 URLs Importantes

Após o deploy, você terá:

```
Frontend Next.js:  https://juridico-frontend-nextjs.onrender.com
Login:             https://juridico-frontend-nextjs.onrender.com/super-admin/login
Dashboard:         https://juridico-frontend-nextjs.onrender.com/super-admin/dashboard

Backend Clojure:   https://seu-backend.onrender.com
```

---

## 💡 Dicas de Performance

### Free Tier do Render

O plano gratuito tem limitações:
- ⚠️ Serviço "dorme" após 15 minutos de inatividade
- ⚠️ Primeiro acesso após "dormir" demora ~30 segundos
- ⚠️ 750 horas/mês de uptime

### Melhorar Performance

**Opção 1: Upgrade para Paid Plan**
- $7/mês por serviço
- Sem "sleep"
- Mais recursos

**Opção 2: Keep-Alive**
- Usar serviço como UptimeRobot para fazer ping a cada 10 minutos
- Mantém serviço "acordado"

---

## 🆘 Suporte

Se tiver problemas:

1. **Verificar Logs:** Dashboard → Logs
2. **Documentação Render:** https://render.com/docs/deploy-nextjs
3. **Suporte Render:** https://render.com/support
4. **Documentação Next.js:** https://nextjs.org/docs/deployment

---

## ✅ Conclusão

Seu frontend Next.js BFF está deployado no Render! 🎉

**Próximo Passo:** Testar todas as funcionalidades em produção

---

**Boa sorte com o deploy! 🚀**
