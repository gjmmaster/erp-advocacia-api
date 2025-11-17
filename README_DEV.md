# 🚀 Desenvolvimento Local - Quick Start

Guia rápido para desenvolvedores que já têm tudo instalado.

---

## ⚡ Start Rápido

```bash
# Windows
.\dev-full.ps1

# Linux/Mac
./dev-full.sh
```

Acesse: http://localhost:3001

---

## 📁 Estrutura de Configuração

```
projeto/
├── .env.development              # ✅ Commitar - Config padrão backend dev
├── .env.local                    # ❌ NÃO commitar - Suas configs locais
├── dev.sh / dev.ps1              # Scripts para rodar backend
├── dev-full.sh / dev-full.ps1    # Scripts para rodar tudo
│
├── frontend-nextjs/
│   ├── .env.development          # ✅ Commitar - Config padrão frontend dev
│   ├── .env.production           # ✅ Commitar - Config padrão frontend prod
│   ├── .env.local                # ❌ NÃO commitar - Suas configs locais
│   └── .env.example              # ✅ Commitar - Exemplo de configuração
```

---

## 🔧 Configurações Automáticas

### Desenvolvimento (npm run dev / lein run)
- Backend: `.env.development` → `http://localhost:3000`
- Frontend: `.env.development` → `http://localhost:3001`
- Database: `postgresql://localhost:5432/juridico_dev`

### Produção (npm run build / deploy)
- Backend: Variáveis do Render
- Frontend: `.env.production` → URLs de produção
- Database: CockroachDB (Render)

**Você NÃO precisa mudar nada ao fazer push!**

---

## 🛠️ Comandos Principais

### Rodar Tudo
```bash
# Windows
.\dev-full.ps1

# Linux/Mac
./dev-full.sh
```

### Rodar Separado

**Backend:**
```bash
# Windows
.\dev.ps1

# Linux/Mac
./dev.sh
```

**Frontend:**
```bash
cd frontend-nextjs
npm run dev
```

---

## 🧪 Testar

```bash
# Health check
curl http://localhost:3000/health

# Login super admin
curl -X POST http://localhost:3000/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"superadmin@example.com","password":"sua-senha"}'
```

---

## 📝 Workflow de Desenvolvimento

1. **Fazer mudanças** no código
2. **Testar localmente** com `dev-full.ps1` / `dev-full.sh`
3. **Commitar** as mudanças
4. **Push** para o repositório
5. **Deploy automático** no Render (sem configuração!)

---

## 🔐 Credenciais Locais

**Super Admin:**
- Email: `superadmin@example.com`
- Senha: (configurada no `create_super_admin.sql`)

**JWT Secret (dev):**
- `chave-padrao-para-desenvolvimento-segura`

---

## 📚 Documentação Completa

- [SETUP_INICIAL.md](./SETUP_INICIAL.md) - Setup completo (primeira vez)
- [GUIA_DESENVOLVIMENTO_LOCAL.md](./GUIA_DESENVOLVIMENTO_LOCAL.md) - Guia detalhado
- [README.md](./README.md) - Visão geral do projeto

---

## 💡 Dicas

- Use `.env.local` para configs pessoais (não é commitado)
- O `.gitignore` já protege seus arquivos locais
- Scripts `dev-*.ps1` / `dev-*.sh` facilitam o desenvolvimento
- Migrations são aplicadas uma vez no setup inicial
- Deploy é automático, sem precisar configurar nada

---

**Primeira vez?** Leia o [SETUP_INICIAL.md](./SETUP_INICIAL.md)

**Problemas?** Consulte [GUIA_DESENVOLVIMENTO_LOCAL.md](./GUIA_DESENVOLVIMENTO_LOCAL.md)
