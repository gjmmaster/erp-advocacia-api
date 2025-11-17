# 🎯 Setup Inicial - Primeira Vez

Execute estes passos **apenas na primeira vez** que for rodar o projeto localmente.

---

## 1️⃣ Instalar Dependências do Sistema

### Windows

```powershell
# Java (via Chocolatey)
choco install openjdk11

# Leiningen
choco install lein

# Node.js
choco install nodejs

# PostgreSQL
choco install postgresql
```

### Linux (Ubuntu/Debian)

```bash
# Java
sudo apt update
sudo apt install openjdk-11-jdk

# Leiningen
curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > ~/bin/lein
chmod +x ~/bin/lein
lein

# Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# PostgreSQL
sudo apt install postgresql postgresql-contrib
```

### macOS

```bash
# Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Java
brew install openjdk@11

# Leiningen
brew install leiningen

# Node.js
brew install node@18

# PostgreSQL
brew install postgresql@14
brew services start postgresql@14
```

---

## 2️⃣ Configurar PostgreSQL

```bash
# Iniciar PostgreSQL (se não estiver rodando)
# Windows: Já inicia automaticamente
# Linux: sudo systemctl start postgresql
# macOS: brew services start postgresql

# Criar usuário (se necessário)
# Linux/macOS:
sudo -u postgres createuser -s $USER

# Criar banco de dados
createdb juridico_dev

# Verificar conexão
psql -d juridico_dev -c "SELECT version();"
```

---

## 3️⃣ Clonar e Configurar o Projeto

```bash
# Clonar o repositório (se ainda não clonou)
git clone <seu-repositorio>
cd <nome-do-projeto>

# Instalar dependências do backend
lein deps

# Instalar dependências do frontend
cd frontend-nextjs
npm install
cd ..
```

---

## 4️⃣ Configurar Banco de Dados

```bash
# Aplicar todas as migrations
psql -d juridico_dev -f APLICAR_TODAS_MIGRATIONS.sql

# Criar super admin
psql -d juridico_dev -f create_super_admin.sql

# Verificar se as tabelas foram criadas
psql -d juridico_dev -c "\dt"
```

Você deve ver estas tabelas:
- `tenants`
- `users`
- `clientes`
- `processos`
- `processo_historico`
- `documentos`

---

## 5️⃣ Configurar Variáveis de Ambiente (Opcional)

O projeto já vem com configurações padrão em `.env.development`, mas você pode personalizar:

```bash
# Editar configurações do backend (opcional)
# Abra .env.development e ajuste se necessário

# Editar configurações do frontend (opcional)
# Crie frontend-nextjs/.env.local se quiser sobrescrever
```

**Configurações padrão que já funcionam:**
- Database: `postgresql://localhost:5432/juridico_dev`
- JWT Secret: `chave-padrao-para-desenvolvimento-segura`
- Backend Port: `3000`
- Frontend Port: `3001`

---

## 6️⃣ Testar a Instalação

```bash
# Testar backend
lein run
# Deve iniciar sem erros na porta 3000

# Em outro terminal, testar frontend
cd frontend-nextjs
npm run dev
# Deve iniciar sem erros na porta 3001

# Testar API
curl http://localhost:3000/health
# Deve retornar: {"status":"ok"}
```

---

## 7️⃣ Acessar o Sistema

Abra o navegador em:
- http://localhost:3001/super-admin/login

**Credenciais padrão:**
- Email: `superadmin@example.com`
- Senha: (a que você configurou no `create_super_admin.sql`)

---

## ✅ Checklist de Verificação

Marque conforme for completando:

- [ ] Java 11+ instalado (`java -version`)
- [ ] Leiningen instalado (`lein version`)
- [ ] Node.js 18+ instalado (`node --version`)
- [ ] PostgreSQL instalado e rodando
- [ ] Banco `juridico_dev` criado
- [ ] Migrations aplicadas
- [ ] Super admin criado
- [ ] Dependências do backend instaladas (`lein deps`)
- [ ] Dependências do frontend instaladas (`npm install`)
- [ ] Backend inicia sem erros
- [ ] Frontend inicia sem erros
- [ ] Consegue fazer login como super admin

---

## 🎉 Pronto!

Agora você pode usar os scripts de desenvolvimento:

**Windows:**
```powershell
.\dev-full.ps1
```

**Linux/Mac:**
```bash
./dev-full.sh
```

---

## 🆘 Problemas?

Consulte o [GUIA_DESENVOLVIMENTO_LOCAL.md](./GUIA_DESENVOLVIMENTO_LOCAL.md) na seção "Problemas Comuns".

---

**Próximo passo:** Leia o [GUIA_DESENVOLVIMENTO_LOCAL.md](./GUIA_DESENVOLVIMENTO_LOCAL.md) para entender o fluxo de desenvolvimento.
