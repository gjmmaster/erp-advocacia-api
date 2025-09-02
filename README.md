# API de ERP Jurídico - Backend em Clojure

Este projeto é a estrutura inicial para uma API REST de um ERP jurídico, construída em Clojure. A principal característica arquitetural é o suporte a multi-tenancy desde o início, utilizando uma camada de abstração (Padrão Repository) que permite a fácil substituição do banco de dados mock atual por um banco de dados real no futuro.

## Arquitetura Aplicada

A arquitetura foi desenhada para ser modular, testável e escalável.

1.  **Padrão Repository com `protocol`**: Toda a interação com dados é mediada por um "contrato" definido em `meuerp.db.protocols`. Isso desacopla a lógica de negócio dos detalhes de implementação do banco de dados. Para trocar o banco de dados, basta criar um novo `defrecord` que implemente o mesmo protocolo.

2.  **Banco de Dados Mock com `atom`**: Para desenvolvimento e testes, um `atom` global em `meuerp.db.mock` simula o banco de dados. Ele é pré-populado com dados para dois tenants (`tenant-1` e `tenant-2`) para permitir a verificação do isolamento de dados.

3.  **Injeção de Dependência via Middleware**: Um middleware customizado (`meuerp.middleware/wrap-db-repo`) intercepta cada requisição, lê o cabeçalho `X-Tenant-ID` para identificar o tenant, e instancia o repositório correto. Este repositório, já configurado para o tenant específico, é então "injetado" na requisição, ficando disponível para os handlers. Isso mantém os handlers limpos e sem conhecimento direto sobre o multi-tenancy.

4.  **Roteamento com Reitit**: As rotas da API, a negociação de conteúdo (JSON) e a aplicação de middlewares são gerenciadas pela biblioteca Reitit, que oferece uma forma declarativa e eficiente de definir os endpoints.

## Estrutura de Arquivos

O projeto está organizado da seguinte forma para promover a separação de responsabilidades:

```
/src
└── /meuerp
    ├── core.clj          # Ponto de entrada: define rotas, aplica middlewares e inicia o servidor.
    ├── db
    │   ├── protocols.clj # Define o "contrato" (protocol) da camada de dados.
    │   └── mock.clj      # Implementação do banco de dados mock com `atom` e o `defrecord` do repositório.
    ├── middleware.clj    # Middleware customizado para autenticação simulada e injeção de dependência.
    └── handlers.clj      # Lógica dos handlers da API, que orquestram as requisições.
```

## Como Executar o Projeto

**Pré-requisitos:**
*   [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/)
*   [Ferramentas de Linha de Comando do Clojure](https://clojure.org/guides/getting_started)

Com os pré-requisitos instalados, inicie o servidor com o seguinte comando a partir da raiz do projeto:

```bash
clj -M:run
```

O servidor será iniciado na porta `3000`.

## Como Testar a API

Use `curl` ou uma ferramenta de sua preferência para fazer requisições à API. O cabeçalho `X-Tenant-ID` é **obrigatório** para todas as requisições.

### 1. Listar processos (Tenant 1)
*Deve retornar 2 processos.*
```bash
curl -i -H "X-Tenant-ID: tenant-1" http://localhost:3000/api/processos
```

### 2. Listar processos (Tenant 2)
*Deve retornar 1 processo.*
```bash
curl -i -H "X-Tenant-ID: tenant-2" http://localhost:3000/api/processos
```

### 3. Obter processo por ID (Sucesso)
*Busca um processo que pertence ao Tenant 1.*
```bash
curl -i -H "X-Tenant-ID: tenant-1" http://localhost:3000/api/processos/proc-111
```

### 4. Obter processo por ID (Falha - Isolamento de Tenant)
*Tenta buscar um processo do Tenant 2 usando as credenciais do Tenant 1. Deve retornar 404.*
```bash
curl -i -H "X-Tenant-ID: tenant-1" http://localhost:3000/api/processos/proc-333
```

### 5. Criar um novo processo (Tenant 2)
*Cria um novo processo para o Tenant 2.*
```bash
curl -i -X POST \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-2" \
  -d '{"case_number": "XYZ-987", "jurisdiction": "Supremo Tribunal Federal"}' \
  http://localhost:3000/api/processos
```

### 6. Verificar criação
*Lista os processos do Tenant 2 novamente. Deve agora retornar 2 processos.*
```bash
curl -i -H "X-Tenant-ID: tenant-2" http://localhost:3000/api/processos
```

### 7. Requisição sem Tenant ID
*Deve retornar um erro 401 Unauthorized.*
```bash
curl -i http://localhost:3000/api/processos
```
