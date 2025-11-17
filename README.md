# Sistema de Gerenciamento Jurídico Multi-Tenant

Sistema completo de gerenciamento jurídico com arquitetura multi-tenant, autenticação segura e painel administrativo.

---

## 🚀 Quick Start

### Desenvolvimento Local (1 comando)

```bash
# Windows
.\dev-full.ps1

# Linux/Mac
./dev-full.sh
```

Acesse: http://localhost:3001

### 📚 Documentação

Toda a documentação está organizada na pasta [docs/](./docs/):

- 📖 [Guias de Desenvolvimento](./docs/guias/)
- 🚀 [Deploy e Produção](./docs/deploy/)
- 🔧 [Troubleshooting](./docs/troubleshooting/)
- 📝 [Histórico de Sessões](./docs/historico/)

---

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

### ✅ Troca de Senha Temporária (NOVO!)
- [x] Detecção automática de senha temporária
- [x] Redirecionamento forçado para troca
- [x] Validação de força da senha em tempo real
- [x] Indicador visual de força
- [x] Bloqueio de acesso até troca ser concluída
- [x] Atualização segura com bcrypt

---

## 📋 Próximas Funcionalidades (Backlog)

### 🔄 Prioridade Alta
1. ✅ **Trocar Senha Temporária** - IMPLEMENTADO! Ver `IMPLEMENTACAO_COMPLETA_FORCE_PASSWORD.md`
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

## 📚 Documentação Completa

Toda a documentação está organizada em [docs/](./docs/):

### 📖 Guias
- [Desenvolvimento Local](./docs/guias/GUIA_DESENVOLVIMENTO_LOCAL.md)
- [Setup Inicial](./docs/guias/SETUP_INICIAL.md)
- [Configuração de Ambiente](./docs/guias/CONFIGURACAO_AMBIENTE.md)
- [Upload de Arquivos](./docs/guias/GUIA_UPLOAD_ARQUIVOS.md)
- [Cloudflare R2](./docs/guias/GUIA_UPLOAD_CLOUDFLARE_R2.md)
- [Aplicar Migrations](./docs/guias/GUIA_APLICAR_MIGRATIONS.md)
- [Rollback](./docs/guias/COMO_FAZER_ROLLBACK.md)
- [Logs e Debug](./docs/guias/COMO_USAR_LOGS_DEBUG.md)

### 🚀 Deploy
- [Checklist de Deploy](./docs/deploy/CHECKLIST_DEPLOY.md)
- [Comandos de Deploy](./docs/deploy/COMANDOS_DEPLOY.md)
- [Build Frontend](./docs/deploy/BUILD_FRONTEND.md)

### 🔧 Troubleshooting
- [Correção de Erros](./docs/troubleshooting/)

### 🏗️ Arquitetura
- [Database Schema](./docs/DATABASE_SCHEMA.md)
- [Índice de Segurança](./docs/SECURITY_INDEX.md)

### 📋 Specs (Planejamento)
- [Force Password Change](./.kiro/specs/force-password-change/) - ✅ Implementado
- [Admin Impersonation](./.kiro/specs/admin-impersonation/) - ✅ Implementado
- [Gestão de Processos](./.kiro/specs/gestao-processos/) - ✅ Implementado
- [Dev Mode Mock](./.kiro/specs/dev-mode-mock-repository/) - ✅ Implementado

---

## 🛠️ Desenvolvimento Local

### Primeira Vez?

Leia o [Guia de Setup Inicial](./docs/guias/SETUP_INICIAL.md)

### Já Configurou?

Use os scripts de desenvolvimento:

```bash
# Modo completo (backend + frontend + banco)
.\dev-full.ps1      # Windows
./dev-full.sh       # Linux/Mac

# Modo mock (sem banco de dados)
.\dev-full-mock.ps1 # Windows
./dev-full-mock.sh  # Linux/Mac
```

Acesse: http://localhost:3001

### Configuração

O projeto usa configurações automáticas:
- **Dev:** `.env.development` (backend) + `frontend-nextjs/.env.development`
- **Prod:** Variáveis do Render + `frontend-nextjs/.env.production`

Ver [Configuração de Ambiente](./docs/guias/CONFIGURACAO_AMBIENTE.md) para detalhes.

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

Deploy automático no Render.com via push para `main`.

Ver [Checklist de Deploy](./docs/deploy/CHECKLIST_DEPLOY.md) para detalhes completos.

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
