# 🚀 Guia de Desenvolvimento Local

Este guia explica como rodar o projeto localmente de forma simples, sem precisar configurar nada quando fizer deploy.

---

## 🎯 Como Funciona

O projeto agora tem **configurações automáticas** para desenvolvimento e produção:

### Desenvolvimento (Local)
- Backend usa `.env.development`
- Frontend usa `frontend-nextjs/.env.development`
- Banco de dados local: `postgresql://localhost:5432/juridico_dev`
- Backend roda na porta 3000
- Frontend roda na porta 3001

### Produção (Render)
- Backend usa variáveis de ambiente do Render
- Frontend usa `frontend-nextjs/.env.production`
- Banco de dados: CockroachDB (configurado no Render)
- URLs de produção configuradas automaticamente

**Você NÃO precisa mudar NADA quando fizer push!** 🎉

---

## 📋 Pré-requisitos

Instale uma única vez:

1. **Java 11+** - Para rodar o backend Clojure
2. **Leiningen** - Gerenciador de dependências Clojure
   ```bash
   # Windows (via Chocolatey)
   choco install lein
   
   # Linux/Mac
   # Baixe de https://leiningen.org/
   ```
3. **Node.js 18+** - Para rodar o frontend Next.js
4. **PostgreSQL** - Banco de dados local

---

## 🗄️ Setup do Banco de Dados (Primeira Vez)

```bash
# 1. Criar o banco de dados
createdb juridico_dev

# 2. Aplicar as migrations
psql -d juridico_dev -f APLICAR_TODAS_MIGRATIONS.sql

# 3. Criar o super admin
psql -d juridico_dev -f create_super_admin.sql
```

---

## 🚀 Rodar o Projeto

### Opção 1: Tudo de Uma Vez (Recomendado)

**Windows:**
```powershell
.\dev-full.ps1
```

**Linux/Mac:**
```bash
chmod +x dev-full.sh
./dev-full.sh
```

Isso vai iniciar:
- ✅ Backend na porta 3000
- ✅ Frontend na porta 3001

### Opção 2: Backend e Frontend Separados

**Backend:**
```bash
# Windows
.\dev.ps1

# Linux/Mac
chmod +x dev.sh
./dev.sh
```

**Frontend (em outro terminal):**
```bash
cd frontend-nextjs
npm run dev
```

---

## 🌐 Acessar o Sistema

Depois de iniciar os servidores:

- **Frontend:** http://localhost:3001
- **Super Admin:** http://localhost:3001/super-admin/login
- **Tenant Login:** http://localhost:3001/login
- **API Backend:** http://localhost:3000

### Credenciais Padrão

**Super Admin:**
- Email: `superadmin@example.com`
- Senha: (a que você configurou no SQL)

---

## 📝 Arquivos de Configuração

### Não Commitar (já no .gitignore)
- `.env.development` - Suas configs locais do backend
- `frontend-nextjs/.env.local` - Suas configs locais do frontend
- `frontend-nextjs/.env.development.local` - Overrides locais

### Commitar (configs padrão)
- `frontend-nextjs/.env.development` - Configs padrão de dev
- `frontend-nextjs/.env.production` - Configs padrão de prod
- `project.clj` - Profile de desenvolvimento do backend

---

## 🔧 Personalizar Configurações Locais

Se você precisar de configs diferentes (ex: porta diferente, outro banco):

1. **Backend:** Edite `.env.development`
2. **Frontend:** Crie `frontend-nextjs/.env.local` (sobrescreve .env.development)

Exemplo de `.env.local`:
```bash
BACKEND_API_URL=http://localhost:8080
JWT_SECRET=minha-chave-customizada
```

---

## 🚢 Deploy para Produção

Quando você fizer push, **NADA muda**:

```bash
git add .
git commit -m "feat: minha nova feature"
git push
```

O Render vai:
1. Usar as variáveis de ambiente configuradas no painel
2. O frontend vai usar `.env.production` automaticamente
3. Tudo funciona sem configuração adicional

---

## 🧪 Testar a API Diretamente

```bash
# Health check
curl http://localhost:3000/health

# Login super admin
curl -X POST http://localhost:3000/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"superadmin@example.com","password":"sua-senha"}'
```

---

## 🔄 Comandos Úteis

### Backend
```bash
# Instalar dependências
lein deps

# Rodar testes
lein test

# REPL interativo
lein repl

# Limpar e recompilar
lein clean && lein deps
```

### Frontend
```bash
cd frontend-nextjs

# Instalar dependências
npm install

# Build de produção (testar localmente)
npm run build
npm start

# Linter
npm run lint

# Testes
npm test
```

---

## ⚠️ Problemas Comuns

### Backend não inicia
```bash
# Verificar se PostgreSQL está rodando
psql -l

# Verificar se a porta 3000 está livre
netstat -ano | findstr :3000  # Windows
lsof -i :3000                  # Linux/Mac
```

### Frontend não conecta no backend
```bash
# Verificar se backend está rodando
curl http://localhost:3000/health

# Verificar configuração
cat frontend-nextjs/.env.development
```

### Erro de autenticação
- Confirme que `JWT_SECRET` é o mesmo no backend e frontend
- Verifique se o super admin foi criado no banco

### Migrations não aplicadas
```bash
# Verificar tabelas no banco
psql -d juridico_dev -c "\dt"

# Reaplicar migrations
psql -d juridico_dev -f APLICAR_TODAS_MIGRATIONS.sql
```

---

## 📚 Próximos Passos

1. ✅ Rodar o projeto localmente
2. ✅ Fazer login como super admin
3. ✅ Criar um tenant de teste
4. ✅ Fazer login como tenant
5. 🚀 Começar a desenvolver!

---

## 💡 Dicas

- Use `dev-full.ps1` / `dev-full.sh` para iniciar tudo de uma vez
- Mantenha `.env.development` com suas configs locais
- Nunca commite senhas ou dados sensíveis
- O `.gitignore` já está configurado para proteger seus arquivos locais
- Quando fizer push, o deploy é automático sem precisar mudar nada

---

**Última Atualização:** 13 de Novembro de 2025
