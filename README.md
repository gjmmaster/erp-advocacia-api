# Sistema de Gerenciamento Jurídico Multi-Tenant

Sistema completo de gerenciamento jurídico com arquitetura multi-tenant, autenticação segura e painel administrativo.

## 🎯 Status do Projeto

**Versão:** 1.0.0  
**Status:** ✅ Em Produção  
**Última Atualização:** 29 de Outubro de 2025

---

## 🚀 Funcionalidades Implementadas

### ✅ Autenticação e Autorização
- [x] Login de Super Admin
- [x] Login de Tenant (auto-descoberta por email)
- [x] Geração automática de senha temporária
- [x] JWT com expiração configurável
- [x] Middleware de proteção de rotas
- [x] Cookies HTTP-only seguros

### ✅ Gestão de Tenants (Super Admin)
- [x] Criar novos escritórios (tenants)
- [x] Listar todos os tenants
- [x] Editar informações do tenant
- [x] Deletar tenant (com confirmação)
- [x] Modal com senha temporária visível
- [x] Envio de email de boas-vindas

### ✅ Dashboard de Tenant
- [x] Interface personalizada por escritório
- [x] Menu lateral com navegação
- [x] Área de conteúdo principal
- [x] Logout seguro

---

## 📋 Próximas Funcionalidades (Backlog)

### 🔄 Prioridade Alta
1. **Trocar Senha Temporária** - Forçar usuário a mudar senha no primeiro login
2. **Impersonation** - Super admin acessar como tenant para suporte
3. **Reset de Senha** - Self-service via email

### 🔄 Prioridade Média
4. **Dashboard com Dados Reais** - Estatísticas e gráficos
5. **Gestão de Operadores** - Tenant criar usuários operadores
6. **Gestão de Processos** - CRUD completo de processos jurídicos
7. **Gestão de Clientes** - Cadastro e acompanhamento de clientes

### 🔄 Prioridade Baixa
8. **Notificações** - Sistema de alertas e lembretes
9. **Relatórios** - Geração de relatórios em PDF
10. **Auditoria** - Logs detalhados de todas as ações

---

## 🏗️ Arquitetura

### Backend (Clojure)
- **Framework:** Ring + Reitit
- **Banco de Dados:** PostgreSQL (CockroachDB)
- **Autenticação:** JWT com Buddy
- **Deploy:** Render.com

### Frontend (Next.js)
- **Framework:** Next.js 14 (App Router)
- **Linguagem:** TypeScript
- **Estilização:** CSS Modules
- **Deploy:** Render.com

### Padrão BFF (Backend for Frontend)
- API Routes do Next.js como camada intermediária
- Cookies HTTP-only para tokens
- Proteção contra CSRF

---

## 🌐 URLs de Produção

### Frontend
- **URL:** https://erp-advocacia-frontend.onrender.com
- **Super Admin:** https://erp-advocacia-frontend.onrender.com/super-admin/login
- **Tenant Login:** https://erp-advocacia-frontend.onrender.com/login

### Backend
- **API:** https://erp-advocacia-api.onrender.com
- **Health Check:** https://erp-advocacia-api.onrender.com/debug/secret-check

---

## 🔐 Credenciais de Teste

### Super Admin
- **Email:** superadmin@example.com
- **Senha:** (configurada via variável de ambiente)

### Tenant de Teste
- **Escritório:** TESTINHO
- **Email:** jmmaster.dev@gmail.com
- **Senha:** evirULWqAuBq (temporária)

---

## 📚 Documentação

### Documentos Principais
- [SETUP.md](./docs/SETUP.md) - Guia de instalação e configuração
- [API.md](./docs/API.md) - Documentação da API
- [DEPLOY.md](./docs/DEPLOY.md) - Guia de deploy
- [SECURITY.md](./docs/SECURITY.md) - Práticas de segurança

### Specs (Planejamento de Features)
- [Impersonation e Reset de Senha](./.kiro/specs/impersonation-password-reset/) - Próxima feature planejada
- [Tenant Authentication](./.kiro/specs/tenant-authentication/) - Feature implementada

---

## 🛠️ Desenvolvimento Local

### Pré-requisitos
- Java 11+
- Leiningen
- Node.js 18+
- PostgreSQL

### Backend
```bash
# Instalar dependências
lein deps

# Configurar variáveis de ambiente
export DATABASE_URL="postgresql://..."
export JWT_SECRET="sua-chave-secreta"

# Rodar servidor
lein run
```

### Frontend
```bash
cd frontend-nextjs

# Instalar dependências
npm install

# Configurar variáveis de ambiente
# Criar arquivo .env.local com:
# BACKEND_URL=http://localhost:3000
# JWT_SECRET=sua-chave-secreta

# Rodar servidor de desenvolvimento
npm run dev
```

---

## 🧪 Testes

### Backend
```bash
lein test
```

### Frontend
```bash
cd frontend-nextjs
npm test
```

---

## 📦 Deploy

### Backend (Render)
1. Conectar repositório GitHub
2. Configurar variáveis de ambiente
3. Deploy automático no push para `main`

### Frontend (Render)
1. Conectar repositório GitHub
2. Configurar variáveis de ambiente
3. Deploy automático no push para `main`

Ver [DEPLOY.md](./docs/DEPLOY.md) para detalhes completos.

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Add: Minha feature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## 📝 Licença

Este projeto é proprietário e confidencial.

---

## 👥 Equipe

- **Desenvolvedor Principal:** [Seu Nome]
- **Data de Início:** Setembro de 2025
- **Última Atualização:** 29 de Outubro de 2025

---

## 📞 Suporte

Para questões ou suporte, entre em contato através de:
- Email: suporte@seudominio.com
- Issues: GitHub Issues

---

## 🎉 Agradecimentos

Obrigado a todos que contribuíram para este projeto!
