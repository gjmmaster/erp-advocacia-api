# 🎯 Configuração de Ambiente - Visão Geral

## 📊 Como Funciona

```
┌─────────────────────────────────────────────────────────────┐
│                    DESENVOLVIMENTO LOCAL                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Backend (Clojure)                Frontend (Next.js)         │
│  ├─ .env.development          ├─ .env.development           │
│  ├─ Port: 3000                ├─ Port: 3001                 │
│  └─ DB: localhost:5432        └─ API: localhost:3000        │
│                                                              │
│  Rodar: .\dev-full.ps1  (Windows)                           │
│         ./dev-full.sh   (Linux/Mac)                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘

                            ⬇️  git push

┌─────────────────────────────────────────────────────────────┐
│                    PRODUÇÃO (RENDER)                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Backend (Clojure)                Frontend (Next.js)         │
│  ├─ Variáveis do Render       ├─ .env.production            │
│  ├─ Port: automático          ├─ Port: automático           │
│  └─ DB: CockroachDB           └─ API: render backend URL    │
│                                                              │
│  Deploy: AUTOMÁTICO (sem configuração!)                     │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 O Que Foi Criado

### ✅ Arquivos de Configuração

| Arquivo | Commitar? | Descrição |
|---------|-----------|-----------|
| `.env.development` | ✅ Sim | Config padrão backend dev |
| `.env.local` | ❌ Não | Suas configs locais backend |
| `frontend-nextjs/.env.development` | ✅ Sim | Config padrão frontend dev |
| `frontend-nextjs/.env.production` | ✅ Sim | Config padrão frontend prod |
| `frontend-nextjs/.env.local` | ❌ Não | Suas configs locais frontend |

### ✅ Scripts de Desenvolvimento

| Script | Plataforma | O Que Faz |
|--------|------------|-----------|
| `dev.ps1` | Windows | Inicia só o backend |
| `dev.sh` | Linux/Mac | Inicia só o backend |
| `dev-full.ps1` | Windows | Inicia backend + frontend |
| `dev-full.sh` | Linux/Mac | Inicia backend + frontend |

### ✅ Documentação

| Arquivo | Para Quem |
|---------|-----------|
| `SETUP_INICIAL.md` | Primeira vez rodando o projeto |
| `GUIA_DESENVOLVIMENTO_LOCAL.md` | Guia completo de desenvolvimento |
| `README_DEV.md` | Quick start para devs |
| `CONFIGURACAO_AMBIENTE.md` | Este arquivo (visão geral) |

---

## 🚀 Como Usar

### Primeira Vez (Setup Inicial)

1. Leia e siga: [SETUP_INICIAL.md](./SETUP_INICIAL.md)
2. Instale dependências
3. Configure banco de dados
4. Rode os scripts

### Desenvolvimento Diário

```bash
# Windows
.\dev-full.ps1

# Linux/Mac
./dev-full.sh
```

Pronto! Backend e frontend rodando.

### Fazer Deploy

```bash
git add .
git commit -m "feat: minha feature"
git push
```

Deploy automático no Render, sem configuração!

---

## 🔐 Variáveis de Ambiente

### Desenvolvimento (Automático)

```bash
# Backend (.env.development)
DATABASE_URL=postgresql://localhost:5432/juridico_dev
JWT_SECRET=chave-padrao-para-desenvolvimento-segura
PORT=3000
ENVIRONMENT=development

# Frontend (.env.development)
BACKEND_API_URL=http://localhost:3000
JWT_SECRET=chave-padrao-para-desenvolvimento-segura
NODE_ENV=development
```

### Produção (Render - Já Configurado)

```bash
# Backend (Variáveis do Render)
DATABASE_URL=<cockroachdb-url>
JWT_SECRET=<sua-chave-secreta>
PORT=<automático>

# Frontend (.env.production)
BACKEND_API_URL=https://erp-advocacia-api.onrender.com
JWT_SECRET=${JWT_SECRET}
NODE_ENV=production
```

---

## 📝 Personalizar Configurações Locais

Se você precisar de configs diferentes:

### Backend

Crie `.env.local` (não será commitado):
```bash
DATABASE_URL=postgresql://localhost:5433/meu_banco
JWT_SECRET=minha-chave-customizada
PORT=8080
```

### Frontend

Crie `frontend-nextjs/.env.local` (não será commitado):
```bash
BACKEND_API_URL=http://localhost:8080
JWT_SECRET=minha-chave-customizada
```

---

## 🎯 Fluxo de Trabalho

```
1. Desenvolvimento Local
   ├─ Usar dev-full.ps1 / dev-full.sh
   ├─ Testar mudanças
   └─ Commitar código

2. Git Push
   ├─ git add .
   ├─ git commit -m "..."
   └─ git push

3. Deploy Automático
   ├─ Render detecta push
   ├─ Build automático
   ├─ Deploy automático
   └─ Usa configs de produção
```

---

## ✅ Vantagens Desta Configuração

1. **Zero Configuração no Deploy**
   - Faz push e pronto!
   - Não precisa mudar nada

2. **Ambientes Separados**
   - Dev usa configs locais
   - Prod usa configs do Render
   - Sem conflitos

3. **Segurança**
   - `.gitignore` protege configs locais
   - Senhas não vão para o Git
   - Cada ambiente tem suas credenciais

4. **Facilidade**
   - Scripts prontos para rodar
   - Documentação clara
   - Setup rápido

5. **Flexibilidade**
   - Pode personalizar com `.env.local`
   - Não afeta outros devs
   - Não afeta produção

---

## 🆘 Problemas Comuns

### "Não encontra .env.development"
→ Está na raiz do projeto, não na pasta frontend

### "Backend não conecta no banco"
→ Verifique se PostgreSQL está rodando e se o banco existe

### "Frontend não conecta no backend"
→ Verifique se o backend está rodando na porta 3000

### "Erro de JWT"
→ Confirme que JWT_SECRET é o mesmo no backend e frontend

### "Deploy não funciona"
→ Verifique as variáveis de ambiente no painel do Render

---

## 📚 Próximos Passos

1. ✅ Leia [SETUP_INICIAL.md](./SETUP_INICIAL.md) se é primeira vez
2. ✅ Use `dev-full.ps1` / `dev-full.sh` para desenvolver
3. ✅ Consulte [GUIA_DESENVOLVIMENTO_LOCAL.md](./GUIA_DESENVOLVIMENTO_LOCAL.md) para detalhes
4. ✅ Leia [README_DEV.md](./README_DEV.md) para quick reference

---

**Última Atualização:** 13 de Novembro de 2025
