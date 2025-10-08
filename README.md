# API de ERP Jurídico - Backend em Clojure

Este projeto é a API REST para um ERP jurídico, construída em Clojure. A arquitetura foi projetada para ser uma fundação de produção robusta, com suporte a multi-tenancy, persistência de dados em PostgreSQL e Controle de Acesso Baseado em Papel (RBAC).

## Arquitetura Aplicada

A arquitetura foi desenhada para ser modular, testável e escalável.

1.  **Persistência com PostgreSQL (CockroachDB)**: A camada de dados utiliza um cluster CockroachDB (compatível com PostgreSQL) para garantir a permanência e a segurança dos dados. A interação com o banco de dados é gerenciada pela biblioteca `next.jdbc`.

2.  **Padrão Repository com `protocol`**: Toda a interação com dados é mediada por um "contrato" definido em `juridico.api.db.protocols`. Isso desacopla a lógica de negócio dos detalhes de implementação do banco de dados. A implementação atual, `PostgresRepository`, traduz as funções do protocolo para consultas SQL.

3.  **Autenticação e Autorização (RBAC)**:
    *   **Autenticação Stateless com JWT**: O login de um usuário gera um token JWT que contém claims essenciais como `tenant-id` e `role`.
    *   **Injeção de Dependência e Contexto**: Um middleware de autenticação valida o token em cada requisição e injeta a identidade do usuário (incluindo o `tenant-id`) na requisição. Isso garante o isolamento de dados entre os tenants.
    *   **Controle de Acesso (RBAC)**: Middlewares de autorização, como `wrap-master-role-authorization`, inspecionam a `role` do usuário no token para proteger rotas específicas, permitindo ou negando o acesso com base nas permissões.

## Estrutura de Arquivos

O projeto está organizado da seguinte forma:

```
/src
└── /juridico
    └── /api
        ├── core.clj          # Ponto de entrada: define rotas, aplica middlewares e inicia o servidor.
        ├── db
        │   ├── protocols.clj # Define o "contrato" (protocol) da camada de dados.
        │   ├── postgres.clj  # Implementação do repositório com PostgreSQL.
        │   └── mock.clj      # Implementação mock (usada para testes mais antigos).
        ├── middleware.clj    # Middlewares para autenticação, autorização (RBAC) e injeção de contexto.
        ├── handlers.clj      # Lógica dos handlers da API.
        └── specs.clj         # Definições de `clojure.spec` para validação de dados.
```

## Como Executar o Projeto

**Pré-requisitos:**
*   [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/)
*   [Leiningen](https://leiningen.org/)
*   Acesso a um cluster PostgreSQL / CockroachDB.

**Configuração de Ambiente:**
O projeto utiliza variáveis de ambiente para configurar suas conexões e segredos. É crucial que todas as variáveis abaixo estejam definidas no ambiente de execução para o pleno funcionamento da aplicação.

- `DATABASE_URL`: String de conexão com o banco de dados PostgreSQL / CockroachDB.
- `JWT_SECRET`: Chave secreta para assinar os tokens JWT. Essencial para a segurança em produção.
- `EMAIL_API_URL`: URL da API do serviço de e-mail transacional.
- `EMAIL_API_TOKEN`: Token de autenticação para a API de e-mail.
- `EMAIL_API_USER`: Usuário para a API de e-mail, se necessário pelo provedor.
- `PORT`: Porta na qual o servidor web irá escutar. O padrão é `3000`.

Com os pré-requisitos instalados e as variáveis de ambiente configuradas, inicie o servidor:

```bash
lein run
```

O servidor será iniciado na porta definida pela variável `PORT` (padrão: 3000).

## Como Testar a API

A API possui diferentes níveis de acesso baseados em papéis (`super-admin`, `master`, `operador`). Recomenda-se o uso de ferramentas como Postman ou `curl`.

### Autenticação

#### Login do Super Admin

**IMPORTANTE:** Super admin usa uma rota separada que não requer tenant context.

```bash
curl -X POST https://[URL_DA_API]/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"super@admin.com","password":"sua_senha"}'
```

**Resposta:**
```json
{
  "message": "Super Admin super@admin.com autenticado com sucesso.",
  "token": "eyJ..."
}
```

#### Login de Admin/Operador de Tenant

```bash
curl -X POST https://[URL_DA_API]/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Subdomain: nome-do-escritorio" \
  -d '{"email":"admin@escritorio.com","password":"senha"}'
```

**Nota:** O header `X-Tenant-Subdomain` é obrigatório para identificar o escritório.

### Testes de Super Administrador (requer token de `super-admin`)

**Listar todos os Tenants**
```bash
curl -X GET https://[URL_DA_API]/admin/tenants \
  -H "Authorization: Bearer $SUPER_ADMIN_TOKEN"
```

**Criar um novo Tenant**
```bash
curl -X POST https://[URL_DA_API]/admin/tenants \
  -H "Authorization: Bearer $SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"company_name": "Novo Escritório", "subdomain": "novo-escritorio", "operator_limit": 5}'
```

**Provisionar um novo Tenant e seu usuário Master**
```bash
curl -X POST https://[URL_DA_API]/admin/provision-tenant \
  -H "Authorization: Bearer $SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"company_name": "Novo Escritório", "email": "admin@novo.com", "operator_limit": 5}'
```

### Testes de Administrador de Tenant (requer token de `master`)

**Listar Operadores do seu Tenant**
```bash
curl -X GET https://[URL_DA_API]/api/operadores \
  -H "Authorization: Bearer $MASTER_TOKEN"
```

**Criar um novo Operador**
*Esta ação será bloqueada com `409 Conflict` se o `operator_limit` do tenant for atingido.*
```bash
curl -X POST https://[URL_DA_API]/api/operadores \
  -H "Authorization: Bearer $MASTER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"full_name": "Novo Operador", "email": "operador@exemplo.com", "password": "senha"}'
```

## 🎯 Estado Atual do Projeto

### Funcionalidades Implementadas

#### ✅ Backend (API REST)
- **Multi-tenancy completo** com isolamento de dados por tenant
- **Autenticação JWT** stateless com roles (`super-admin`, `master`, `operador`)
- **RBAC (Role-Based Access Control)** com middlewares de autorização
- **CRUD completo de Tenants** (apenas super admin)
- **CRUD completo de Operadores** (apenas master do tenant)
- **CRUD de Processos Jurídicos** (todos os usuários autenticados)
- **Limite de operadores por tenant** com validação
- **Integração com serviço de e-mail** para boas-vindas
- **Persistência em PostgreSQL/CockroachDB**

#### ✅ Frontend (React)
- **Interface de login do Super Admin** (`/super-admin/login`)
- **Dashboard do Super Admin** com:
  - Listagem de todos os escritórios (tenants)
  - Criação de novos escritórios (provisionamento)
  - Edição de escritórios (nome e limite de operadores)
  - Exclusão de escritórios (com confirmação)
- **Design responsivo** e intuitivo
- **Feedback visual** para todas as operações

#### ✅ Correções Técnicas Importantes

**Problema de Precisão BIGINT Resolvido:**
- JavaScript não consegue representar números BIGINT com precisão
- **Solução:** IDs são retornados como strings no JSON
- Backend converte strings para Long ao receber requisições
- Sem necessidade de migração de banco de dados
- Documentação completa em [SOLUCAO_BIGINT.md](SOLUCAO_BIGINT.md)

### Documentação Disponível

- **[docs/status.md](docs/status.md)** - Histórico completo de evolução do projeto
- **[DOCUMENTACAO_INDEX.md](DOCUMENTACAO_INDEX.md)** - Índice completo da documentação
- **[GUIA_RAPIDO.md](GUIA_RAPIDO.md)** - Guia rápido de uso
- **[SUPER_ADMIN_SETUP.md](SUPER_ADMIN_SETUP.md)** - Como configurar o super admin
- **[CHECKLIST_DEPLOY.md](CHECKLIST_DEPLOY.md)** - Checklist de deploy
- **[SOLUCAO_BIGINT.md](SOLUCAO_BIGINT.md)** - Solução para precisão de IDs grandes
- **[FAQ.md](FAQ.md)** - Perguntas frequentes

## 🚀 Próximos Passos Sugeridos

1. **Interface do Admin de Tenant (Master)**
   - Dashboard para gerenciar operadores
   - Dashboard para gerenciar processos jurídicos
   - Relatórios e estatísticas

2. **Interface do Operador**
   - Visualização e edição de processos
   - Filtros e busca avançada
   - Notificações de prazos

3. **Funcionalidades Avançadas**
   - Upload de documentos
   - Calendário de prazos
   - Notificações por e-mail
   - Relatórios em PDF
   - Auditoria de ações

4. **Melhorias de Segurança**
   - Rate limiting
   - Refresh tokens
   - 2FA (autenticação de dois fatores)
   - Logs de auditoria

## 🆕 Sistema de Autenticação

### Super Admin

**Rota de login:** `/admin/login` (não requer tenant context)

```bash
curl -X POST https://[URL_DA_API]/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"super@admin.com","password":"sua_senha"}'
```

**Token JWT:** Contém `user-id` e `role`, mas **não** contém `tenant-id`

### Admin/Operador de Tenant

**Rota de login:** `/auth/login` (requer header `X-Tenant-Subdomain`)

```bash
curl -X POST https://[URL_DA_API]/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Subdomain: nome-do-escritorio" \
  -d '{"email":"admin@escritorio.com","password":"senha"}'
```

**Token JWT:** Contém `user-id`, `tenant-id` e `role`

### Criando o Super Admin

Para criar o primeiro super admin no banco de dados:

```bash
# Gera o SQL com hash da senha
lein run -m generate-password-hash

# Execute o SQL gerado no seu banco PostgreSQL
```

Ou consulte o arquivo [SUPER_ADMIN_SETUP.md](SUPER_ADMIN_SETUP.md) para mais opções.
