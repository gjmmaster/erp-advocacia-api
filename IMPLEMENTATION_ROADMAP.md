# 🗺️ Roadmap de Implementação - Sistema Jurídico Multi-Tenant

## ✅ Funcionalidades Já Implementadas

### 1. Autenticação e Autorização
- [x] Login de Super Admin
- [x] Login de Tenant (auto-descoberta por email)
- [x] JWT com expiração configurável
- [x] Middleware de proteção de rotas
- [x] Troca de senha temporária obrigatória
- [x] Impersonation (super admin como tenant)

### 2. Gestão de Tenants (Super Admin)
- [x] Criar novos tenants
- [x] Listar todos os tenants
- [x] Editar tenant
- [x] Deletar tenant
- [x] Modal com senha temporária
- [x] Email de boas-vindas

### 3. Gestão de Clientes
- [x] CRUD completo de clientes
- [x] Validação de CPF/CNPJ único por tenant
- [x] Soft delete
- [x] Filtros e paginação

### 4. Gestão de Processos
- [x] CRUD completo de processos
- [x] Validação de número único por tenant
- [x] Associação com clientes
- [x] Status do processo
- [x] Soft delete

### 5. Upload de Documentos
- [x] Upload de arquivos
- [x] Associação com processos
- [x] Metadata de documentos
- [x] Integração com Cloudflare R2

### 6. Histórico de Processos
- [x] Registro de eventos
- [x] Timeline de movimentações
- [x] Tipos de eventos padronizados

## 🚧 Funcionalidades a Implementar

### 1. Gestão de Usuários por Tenant
**Backend:**
- [ ] Handler criado em `handlers/users.clj`
- [ ] Rotas em `/api/tenant/users`
- [ ] CRUD completo
- [ ] Reset de senha
- [ ] Controle de permissões (master vs operador)

**Frontend:**
- [ ] Página de listagem de usuários
- [ ] Modal de criação de usuário
- [ ] Modal de edição de usuário
- [ ] Modal de reset de senha
- [ ] Tabela com filtros

**Implementação:**
```clojure
;; Rotas a adicionar no core.clj
["/users"
 ["" {:get {:handler users/list-users-handler}
      :post {:handler users/create-user-handler}}]
 ["/:id" {:get {:handler users/get-user-handler}
          :put {:handler users/update-user-handler}
          :delete {:handler users/delete-user-handler}}]
 ["/:id/reset-password" {:post {:handler users/reset-user-password-handler}}]]
```

### 2. Download de Documentos
**Backend:**
- [ ] Gerar URL assinada para download
- [ ] Streaming de arquivo
- [ ] Controle de acesso

**Frontend:**
- [ ] Botão de download
- [ ] Preview de documentos (PDF, imagens)
- [ ] Indicador de progresso

### 3. Dashboard com Dados Reais
**Backend:**
- [ ] Endpoint `/api/tenant/dashboard/stats`
- [ ] Agregações no banco:
  - Total de processos por status
  - Total de clientes
  - Processos com prazo próximo
  - Últimas movimentações

**Frontend:**
- [ ] Cards com estatísticas reais
- [ ] Gráficos (Chart.js ou Recharts)
- [ ] Tabela de processos recentes
- [ ] Alertas de prazos

### 4. Sistema de Notificações
**Backend:**
- [ ] Tabela `notifications`
- [ ] CRUD de notificações
- [ ] Tipos: prazo, movimentação, atribuição
- [ ] Marcar como lida

**Frontend:**
- [ ] Sino de notificações no header
- [ ] Dropdown com lista
- [ ] Badge com contador
- [ ] Página de todas as notificações

### 5. Geração de Relatórios
**Backend:**
- [ ] Geração de PDF (usar biblioteca JVM)
- [ ] Templates de relatórios:
  - Relatório de processos
  - Relatório de clientes
  - Relatório de movimentações
- [ ] Filtros por período

**Frontend:**
- [ ] Página de relatórios
- [ ] Seleção de tipo
- [ ] Filtros de data
- [ ] Botão de download

### 6. Busca Avançada
**Backend:**
- [ ] Endpoint `/api/tenant/search`
- [ ] Busca em múltiplas entidades
- [ ] Full-text search no PostgreSQL
- [ ] Filtros combinados

**Frontend:**
- [ ] Barra de busca global
- [ ] Página de resultados
- [ ] Filtros laterais
- [ ] Paginação

### 7. Auditoria e Logs
**Backend:**
- [ ] Tabela `audit_logs`
- [ ] Middleware de auditoria
- [ ] Registrar todas as ações CRUD
- [ ] IP, usuário, timestamp

**Frontend:**
- [ ] Página de logs (apenas super admin)
- [ ] Filtros por usuário, ação, data
- [ ] Exportação de logs

### 8. Configurações do Tenant
**Backend:**
- [ ] Tabela `tenant_settings`
- [ ] Upload de logo
- [ ] Cores personalizadas
- [ ] Configurações de email

**Frontend:**
- [ ] Página de configurações
- [ ] Upload de logo
- [ ] Seletor de cores
- [ ] Prévia em tempo real

### 9. Calendário de Prazos
**Backend:**
- [ ] Tabela `deadlines`
- [ ] CRUD de prazos
- [ ] Cálculo de dias úteis
- [ ] Associação com processos

**Frontend:**
- [ ] Componente de calendário
- [ ] Visualização mensal
- [ ] Alertas visuais
- [ ] Modal de detalhes

### 10. Testes
**Backend:**
- [ ] Testes unitários (handlers)
- [ ] Testes de integração (API)
- [ ] Testes de segurança

**Frontend:**
- [ ] Testes unitários (components)
- [ ] Testes E2E (Playwright/Cypress)
- [ ] Testes de acessibilidade

## 📋 Priorização Sugerida

### Sprint 1 (Alta Prioridade)
1. ✅ Gestão de Usuários por Tenant
2. ✅ Download de Documentos
3. ✅ Dashboard com Dados Reais

### Sprint 2 (Média Prioridade)
4. Sistema de Notificações
5. Busca Avançada
6. Calendário de Prazos

### Sprint 3 (Baixa Prioridade)
7. Geração de Relatórios
8. Auditoria e Logs
9. Configurações do Tenant
10. Testes Completos

## 🛠️ Stack Tecnológico

### Backend
- **Linguagem:** Clojure 1.11.1
- **Framework:** Ring + Reitit
- **Autenticação:** Buddy (JWT + Bcrypt)
- **Banco:** PostgreSQL (CockroachDB em prod)
- **Storage:** Cloudflare R2
- **Deploy:** Render.com

### Frontend
- **Framework:** Next.js 14 (App Router)
- **Linguagem:** TypeScript
- **Estilização:** CSS Modules
- **Autenticação:** Cookies HTTP-only
- **Deploy:** Render.com

## 📦 Estrutura de Arquivos

```
project/
├── src/juridico/api/
│   ├── core.clj               # Rotas principais
│   ├── handlers/
│   │   ├── users.clj          # ✅ Gestão de usuários
│   │   ├── processos.clj      # ✅ Gestão de processos
│   │   ├── password.clj       # ✅ Troca de senha
│   │   ├── notifications.clj  # 🚧 A implementar
│   │   ├── reports.clj        # 🚧 A implementar
│   │   └── search.clj         # 🚧 A implementar
│   ├── db/
│   │   ├── core.clj           # Interface do DB
│   │   ├── postgres.clj       # Implementação PostgreSQL
│   │   └── mock.clj           # ✅ Implementação Mock
│   └── middleware.clj         # ✅ Middlewares

├── frontend-nextjs/
│   └── src/
│       ├── app/
│       │   ├── dashboard/
│       │   │   ├── usuarios/     # 🚧 A implementar
│       │   │   ├── notificacoes/ # 🚧 A implementar
│       │   │   └── relatorios/   # 🚧 A implementar
│       │   └── api/
│       └── components/
│           ├── notifications/    # 🚧 A implementar
│           ├── charts/           # 🚧 A implementar
│           └── calendar/         # 🚧 A implementar
```

## 🔐 Segurança

### Implementado
- [x] JWT com expiração
- [x] Bcrypt para passwords
- [x] Cookies HTTP-only
- [x] CORS configurado
- [x] Rate limiting
- [x] SQL injection prevention
- [x] XSS prevention

### A Implementar
- [ ] CSRF tokens
- [ ] 2FA (Two-Factor Authentication)
- [ ] Política de senha forte
- [ ] Logs de segurança
- [ ] Bloqueio por tentativas falhas

## 📊 Banco de Dados

### Tabelas Existentes
- `tenants` - Escritórios/empresas
- `users` - Usuários do sistema
- `clientes` - Clientes dos escritórios
- `processos` - Processos jurídicos
- `processo_historico` - Histórico de movimentações
- `documentos` - Documentos anexados

### Tabelas a Criar
- `notifications` - Notificações
- `deadlines` - Prazos
- `audit_logs` - Logs de auditoria
- `tenant_settings` - Configurações do tenant
- `reports` - Relatórios gerados

## 🚀 Como Contribuir

1. Escolha uma funcionalidade do roadmap
2. Crie uma branch: `git checkout -b feature/nome-da-feature`
3. Implemente seguindo os padrões do projeto
4. Teste localmente
5. Commit: `git commit -m "Add: descrição"`
6. Push e abra um PR

## 📝 Convenções

### Backend (Clojure)
- Usar kebab-case para funções
- Namespaces organizados por domínio
- Handlers sempre retornam Ring response
- Validações nos handlers
- Logs para debug

### Frontend (TypeScript)
- Usar PascalCase para componentes
- camelCase para funções
- CSS Modules para estilos
- TypeScript strict mode
- Props tipadas

## 🐛 Debugging

### Backend
```bash
# Ver logs em tempo real
tail -f /tmp/cc-agent/60330466/backend.log

# Testar endpoint
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"admin123"}'
```

### Frontend
```bash
# Ver logs em tempo real
tail -f /tmp/cc-agent/60330466/frontend.log

# Acessar em modo dev
open http://localhost:3001
```

## 📖 Documentação Adicional

- [Database Schema](./docs/DATABASE_SCHEMA.md)
- [API Documentation](./docs/API_DOCS.md) - 🚧 A criar
- [Frontend Components](./docs/COMPONENTS.md) - 🚧 A criar
- [Deployment Guide](./docs/deploy/PRONTO_PARA_DEPLOY.md)

---

**Última atualização:** 17 de Novembro de 2025
**Versão:** 1.1.0
**Status:** 🟢 Em desenvolvimento ativo
