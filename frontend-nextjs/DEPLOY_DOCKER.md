# 🐳 Deploy Next.js com Docker no Render

**Data:** 16 de Outubro de 2025  
**Método:** Docker (padronizado com backend)

---

## 🎯 Por que Docker?

✅ **Padronização:** Backend e frontend usam Docker  
✅ **Consistência:** Mesmo ambiente em dev e produção  
✅ **Isolamento:** Cada serviço em seu container  
✅ **Facilidade:** Deploy simplificado  

---

## 📁 Arquivos Criados

```
frontend-nextjs/
├── Dockerfile           ← Build do Next.js
└── .dockerignore        ← Arquivos ignorados

raiz/
└── docker-compose.yml   ← Orquestração (dev local)
```

---

## 🚀 Deploy no Render com Docker

### Opção 1: Deploy Separado (Recomendado)

#### Passo 1: Criar Web Service

1. Acesse: https://dashboard.render.com
2. Clique em **"New +"** → **"Web Service"**
3. Conecte seu repositório

#### Passo 2: Configurar Serviço

| Campo | Valor |
|-------|-------|
| **Name** | `juridico-frontend-nextjs` |
| **Region** | `Oregon (US West)` |
| **Branch** | `main` |
| **Root Directory** | `frontend-nextjs` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `frontend-nextjs/Dockerfile` |
| **Docker Command** | (deixar vazio, usa CMD do Dockerfile) |
| **Plan** | `Free` |

#### Passo 3: Variáveis de Ambiente

```bash
NODE_ENV=production
BACKEND_API_URL=https://seu-backend.onrender.com
JWT_SECRET=sua-chave-secreta-jwt-aqui
PORT=10000
```

**⚠️ Importante:** Render usa porta 10000 internamente, mas o Dockerfile expõe 3001.

#### Passo 4: Deploy

1. Clique em **"Create Web Service"**
2. Render vai:
   - Detectar o Dockerfile
   - Fazer build da imagem
   - Iniciar o container
3. Aguarde 5-10 minutos

---

### Opção 2: Docker Compose (Dev Local)

Para rodar localmente com Docker:

```bash
# Na raiz do projeto
docker-compose up --build

# Acessar:
# Backend:  http://localhost:3000
# Frontend: http://localhost:3001
```

---

## 🔧 Configuração do Dockerfile

### Multi-Stage Build

O Dockerfile usa 2 estágios:

**Estágio 1: Builder**
```dockerfile
FROM node:18-alpine AS builder
# Instala dependências
# Faz build do Next.js
```

**Estágio 2: Runtime**
```dockerfile
FROM node:18-alpine
# Copia apenas arquivos necessários
# Usa usuário não-root (segurança)
# Inicia aplicação
```

### Benefícios

✅ **Imagem menor:** ~150MB (vs ~500MB sem multi-stage)  
✅ **Mais seguro:** Usuário não-root  
✅ **Mais rápido:** Cache de layers  

---

## 🐛 Troubleshooting

### Problema 1: "Build Failed"

**Erro:** `npm install failed`

**Solução:**
```bash
# Testar build localmente
cd frontend-nextjs
docker build -t juridico-frontend .

# Verificar logs
docker logs juridico-frontend
```

### Problema 2: "Cannot connect to backend"

**Erro:** `ECONNREFUSED`

**Solução:**

**No Render (produção):**
```bash
# Use URL pública do backend
BACKEND_API_URL=https://seu-backend.onrender.com
```

**No Docker Compose (local):**
```bash
# Use nome do serviço
BACKEND_API_URL=http://backend:3000
```

### Problema 3: "Port already in use"

**Erro:** `Port 3001 is already allocated`

**Solução:**
```bash
# Parar containers
docker-compose down

# Ou mudar porta no docker-compose.yml
ports:
  - "3002:3001"  # Porta externa:interna
```

### Problema 4: "Permission denied"

**Erro:** Problemas de permissão

**Solução:**
```bash
# Rebuild sem cache
docker-compose build --no-cache

# Ou limpar tudo
docker system prune -a
```

---

## 📊 Comparação: Docker vs Render.yaml

| Aspecto | Docker | Render.yaml |
|---------|--------|-------------|
| **Padronização** | ✅ Igual ao backend | ❌ Diferente |
| **Portabilidade** | ✅ Roda em qualquer lugar | ❌ Só no Render |
| **Dev Local** | ✅ Fácil com docker-compose | ❌ Precisa npm |
| **Build Time** | ~5-7 min | ~3-5 min |
| **Tamanho Imagem** | ~150MB | N/A |
| **Complexidade** | Média | Baixa |

**Recomendação:** Use Docker para padronizar! ✅

---

## 🔒 Segurança

### Usuário Não-Root

O Dockerfile cria um usuário `nextjs` (UID 1001):

```dockerfile
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs
USER nextjs
```

### Variáveis de Ambiente

Nunca commite `.env.local` no Git! Use variáveis no Render.

---

## 📋 Checklist de Deploy

### Preparação
- [ ] Dockerfile criado em `frontend-nextjs/`
- [ ] `.dockerignore` criado
- [ ] Código commitado no Git

### Render
- [ ] Web Service criado
- [ ] Runtime: Docker
- [ ] Root Directory: `frontend-nextjs`
- [ ] Variáveis de ambiente configuradas
- [ ] Build completou com sucesso

### Testes
- [ ] Aplicação acessível
- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] CRUD de tenants funciona

---

## 🚀 Comandos Úteis

### Build Local
```bash
# Build da imagem
cd frontend-nextjs
docker build -t juridico-frontend .

# Rodar container
docker run -p 3001:3001 \
  -e BACKEND_API_URL=http://localhost:3000 \
  -e JWT_SECRET=sua-chave \
  juridico-frontend

# Acessar
http://localhost:3001
```

### Docker Compose
```bash
# Iniciar tudo
docker-compose up -d

# Ver logs
docker-compose logs -f frontend

# Parar tudo
docker-compose down

# Rebuild
docker-compose up --build
```

### Debug
```bash
# Entrar no container
docker exec -it juridico-frontend sh

# Ver variáveis de ambiente
docker exec juridico-frontend env

# Ver processos
docker exec juridico-frontend ps aux
```

---

## 📈 Otimizações Futuras

### 1. Cache de Dependências
```dockerfile
# Copiar apenas package.json primeiro
COPY package*.json ./
RUN npm ci

# Depois copiar código
COPY . .
```

### 2. Imagem Ainda Menor
```dockerfile
# Usar distroless
FROM gcr.io/distroless/nodejs18-debian11
```

### 3. Health Check
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s \
  CMD node -e "require('http').get('http://localhost:3001/api/health')"
```

---

## ✅ Conclusão

Agora você tem:

✅ **Dockerfile otimizado** para Next.js  
✅ **Padronização** com backend Clojure  
✅ **Docker Compose** para dev local  
✅ **Deploy no Render** com Docker  

**Próximo Passo:** Deploy no Render seguindo este guia! 🚀

---

## 📞 Suporte

- **Dockerfile:** `frontend-nextjs/Dockerfile`
- **Docker Compose:** `docker-compose.yml`
- **Render Docs:** https://render.com/docs/docker
- **Next.js Docker:** https://nextjs.org/docs/deployment#docker-image
