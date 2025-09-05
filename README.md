# API de ERP Jurídico - Backend em Clojure

Este projeto é a estrutura inicial para uma API REST de um ERP jurídico, construída em Clojure. A principal característica arquitetural é o suporte a multi-tenancy desde o início, utilizando uma camada de abstração (Padrão Repository) que permite a fácil substituição do banco de dados mock atual por um banco de dados real no futuro.

## Arquitetura Aplicada

A arquitetura foi desenhada para ser modular, testável e escalável.

1.  **Padrão Repository com `protocol`**: Toda a interação com dados é mediada por um "contrato" definido em `juridico.api.db.protocols`. Isso desacopla a lógica de negócio dos detalhes de implementação do banco de dados. Para trocar o banco de dados, basta criar um novo `defrecord` que implemente o mesmo protocolo.

2.  **Banco de Dados Mock com `atom`**: Para desenvolvimento e testes, um `atom` global em `juridico.api.db.mock` simula o banco de dados. Ele é pré-populado com dados para dois tenants (`tenant-1` e `tenant-2`) para permitir a verificação do isolamento de dados.

3.  **Injeção de Dependência via Middleware**: Middlewares customizados (`juridico.api.middleware`) interceptam cada requisição, identificam o contexto (público ou de *tenant*) e instanciam o repositório correto. Este repositório, já configurado para o escopo necessário, é então "injetado" na requisição, ficando disponível para os handlers.

4.  **Roteamento com Reitit**: As rotas da API, a negociação de conteúdo (JSON) e a aplicação de middlewares são gerenciadas pela biblioteca Reitit, que oferece uma forma declarativa e eficiente de definir os endpoints.

## Estrutura de Arquivos

O projeto está organizado da seguinte forma para promover a separação de responsabilidades:

```
/src
└── /juridico
    └── /api
        ├── core.clj          # Ponto de entrada: define rotas, aplica middlewares e inicia o servidor.
        ├── db
        │   ├── protocols.clj # Define o "contrato" (protocol) da camada de dados.
        │   └── mock.clj      # Implementação do banco de dados mock com `atom` e o `defrecord` do repositório.
        ├── middleware.clj    # Middlewares customizados para injeção de dependência e contexto.
        ├── handlers.clj      # Lógica dos handlers da API, que orquestram as requisições.
        └── specs.clj         # Definições de `clojure.spec` para validação de dados.
```

## Como Executar o Projeto

**Pré-requisitos:**
*   [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/)
*   [Leiningen](https://leiningen.org/)

Com os pré-requisitos instalados, inicie o servidor com o seguinte comando a partir da raiz do projeto:

```bash
lein run
```

O servidor será iniciado na porta `3000` por padrão, ou na porta definida pela variável de ambiente `PORT`.

## Como Testar a API

Use `curl` ou uma ferramenta de sua preferência para fazer requisições à API.

### Testes de Provisionamento e Autenticação

**1. Provisionar um novo Tenant**
*Cria um novo tenant e seu usuário admin. Retorna o subdomínio e a senha temporária.*
```bash
curl -i -X POST \
  -H "Content-Type: application/json" \
  -d '{"company_name": "Nova Advocacia", "email": "admin@nova.com"}' \
  http://localhost:3000/admin/provision-tenant
```

**2. Autenticar (Login)**
*Usa os dados do passo anterior para simular um login. Retorna um token JWT simulado.*
```bash
curl -i -X POST \
  -H "Content-Type: application/json" \
  -d '{"subdomain": "nova-advocacia", "email": "admin@nova.com", "password": "SENHA_TEMPORARIA_RECEBIDA"}' \
  http://localhost:3000/auth/login
```

### Testes de API Protegida (Exige `X-Tenant-ID`)

**3. Listar processos (Tenant 1)**
*Deve retornar 2 processos.*
```bash
curl -i -H "X-Tenant-ID: tenant-1" http://localhost:3000/api/processos
```

**4. Listar processos (Tenant 2)**
*Deve retornar 1 processo.*
```bash
curl -i -H "X-Tenant-ID: tenant-2" http://localhost:3000/api/processos
```

**5. Obter processo por ID (Sucesso)**
*Busca um processo que pertence ao Tenant 1.*
```bash
curl -i -H "X-Tenant-ID: tenant-1" http://localhost:3000/api/processos/proc-111
```

**6. Obter processo por ID (Falha - Isolamento de Tenant)**
*Tenta buscar um processo do Tenant 2 usando as credenciais do Tenant 1. Deve retornar 404 Not Found.*
```bash
curl -i -H "X-Tenant-ID: tenant-1" http://localhost:3000/api/processos/proc-333
```

**7. Criar um novo processo (Tenant 2)**
*Cria um novo processo para o Tenant 2.*
```bash
curl -i -X POST \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-2" \
  -d '{"case_number": "XYZ-987", "jurisdiction": "Supremo Tribunal Federal"}' \
  http://localhost:3000/api/processos
```

**8. Requisição sem Tenant ID**
*Deve retornar um erro 401 Unauthorized.*
```bash
curl -i http://localhost:3000/api/processos
```
