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

**Configuração:**
1.  Crie uma variável de ambiente `DATABASE_URL` com a string de conexão para o seu banco de dados.
    *Exemplo:*
    ```bash
    export DATABASE_URL="postgresql://user:password@host:port/database?sslmode=require"
    ```

Com os pré-requisitos instalados e a variável de ambiente configurada, inicie o servidor:

```bash
lein run
```

O servidor será iniciado na porta `3000` por padrão, ou na porta definida pela variável de ambiente `PORT`.

## Como Testar a API

Use `curl` ou uma ferramenta de sua preferência para fazer requisições à API. O fluxo abaixo demonstra a criação e gestão de usuários "operadores" por um usuário "master".

**Variáveis de Ambiente (Exemplo):**
```bash
# URL da API
API_URL="http://localhost:3000"

# Token do usuário "master" (obtido após o login)
MASTER_TOKEN="SEU_TOKEN_DE_MASTER_AQUI"

# Token do usuário "operador" (obtido após o login com as credenciais do operador criado)
OPERATOR_TOKEN="SEU_TOKEN_DE_OPERADOR_AQUI"
```

### Testes de Gestão de Operadores (RBAC)

**1. Criar um novo Operador (como Master)**
*Um usuário "master" cria um novo usuário com a role "operador" dentro do seu próprio tenant.*
*Deve retornar `201 Created`.*
```bash
curl -i -X POST "$API_URL/api/operadores" \
  -H "Authorization: Bearer $MASTER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"full_name": "Fulano de Tal", "email": "fulano@operador.com", "password": "uma_senha_forte"}'
```

**2. Listar Operadores (como Master)**
*O usuário "master" lista todos os usuários do seu tenant. A resposta deve incluir o próprio master e o operador recém-criado.*
*Deve retornar `200 OK`.*
```bash
curl -i -X GET "$API_URL/api/operadores" \
  -H "Authorization: Bearer $MASTER_TOKEN"
```

**3. Tentar Listar Operadores (como Operador)**
*Um usuário "operador" tenta acessar a mesma rota de listagem.*
*Deve retornar `403 Forbidden`, confirmando que o middleware de autorização está funcionando.*
```bash
curl -i -X GET "$API_URL/api/operadores" \
  -H "Authorization: Bearer $OPERATOR_TOKEN"
```
