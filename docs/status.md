### **Diário de Bordo Técnico: Prova de Conceito da Arquitetura Multi-Tenant**

**Data de Referência:** 05 de Setembro de 2025
**Status:** Prova de Conceito (PoC) da arquitetura multi-tenant concluída e validada funcionalmente em ambiente de produção (Render).

#### **1. Introdução e Objetivos da PoC**

Este documento detalha a implementação e validação da Prova de Conceito (PoC) para a API de um ERP Jurídico, construída sobre uma arquitetura multi-tenant em Clojure. O objetivo primordial desta fase foi validar, em um ambiente simulado porém funcional, os pilares arquitetônicos definidos nos documentos de planejamento, focando em:

1.  **Isolamento de Dados:** Garantir que a lógica da aplicação possa prevenir rigorosamente o acesso a dados entre tenants distintos.
2.  **Desacoplamento da Persistência:** Implementar uma camada de acesso a dados que seja agnóstica à sua implementação subjacente, utilizando os `protocols` do Clojure.
3.  **Robustez dos Endpoints:** Assegurar a integridade dos dados na borda da aplicação através de um sistema de validação por contrato (`clojure.spec`).
4.  **Ciclo de Vida do Tenant:** Validar o fluxo de ponta a ponta, desde o provisionamento de um novo tenant (simulando a ação de um **Super Admin**) até a autenticação do seu respectivo usuário **Admin**.

#### **2. Arquitetura Implementada**

A PoC materializou as seguintes decisões arquitetônicas:

*   **Estratégia de Isolamento de Dados:** Foi adotado o modelo de **Schema Compartilhado com Chave Estrangeira** (`tenant_id`). Para a PoC, este modelo foi simulado através de um `atom` global, cuja estrutura de dados espelha a segregação por `tenant-id`.

* **Padrão Repository via `defprotocol`:** A interação com a camada de dados foi completamente abstraída através de `protocols`. Foram definidos dois contratos principais:
    * **`ProcessosRepository`**: Define as operações de CRUD para entidades de negócio (e.g., `listar-processos`, `criar-processo`), com a premissa de que a implementação deve ser implicitamente ciente do contexto do tenant.
    * **`AuthRepository`**: Define as operações globais de autenticação e provisionamento (`encontrar-tenant-por-subdominio`, `criar-tenant-e-usuario-master`), que operam fora do escopo de um único tenant.

* **Injeção de Contexto via Middleware (Ring):** O contexto da requisição (principalmente a identidade do tenant e o repositório de dados) é gerenciado e injetado através de um pipeline de middlewares do Ring:
    * **`wrap-public-db-repo`**: Um middleware aplicado a rotas não autenticadas (`/admin`, `/auth`). Ele instancia o `MockRepository` sem um `tenant-id` pré-definido, permitindo que os handlers executem operações globais.
    * **`wrap-tenant-db-repo`**: Um middleware de segurança aplicado a rotas protegidas (`/api`). Para a PoC, ele impõe a presença do header `x-tenant-id`, utiliza este valor para instanciar uma versão do `MockRepository` com escopo definido, e injeta esta instância na requisição. Falhas em encontrar o header resultam em uma resposta `401 Unauthorized`, protegendo os endpoints de negócio.

* **Validação de Contrato com `clojure.spec`:** Para garantir a robustez e a integridade dos dados na borda da API, foi implementada uma camada de validação declarativa. Um namespace centralizado (`juridico.api.specs`) define as "formas" (`specs`) dos payloads de entrada para cada endpoint. Os handlers de requisição atuam como uma barreira de validação, utilizando `(s/valid? ...)` para verificar a conformidade do payload com a `spec` definida. Em caso de não conformidade, a requisição é imediatamente rejeitada com uma resposta `400 Bad Request`, contendo uma descrição detalhada da falha extraída via `(s/explain-data ...)`.

#### **3. Validação Funcional da PoC: Resultados dos Testes**

O fluxo completo foi validado em ambiente de produção (`onrender.com`) através de uma sequência de testes de integração via `curl`.

1.  **Provisionamento do Tenant (Endpoint: `POST /admin/provision-tenant`):**
    * **Ação:** Requisição enviada com `company_name` e `email` para o usuário **Admin**.
    * **Resultado:** **SUCESSO (`201 Created`)**. A API invocou corretamente o `provision-tenant-handler`, que utilizou a implementação de `criar-tenant-e-usuario-master` para atualizar o `atom` global com um novo tenant, seu subdomônio gerado e o usuário **Admin** com uma senha temporária. A resposta continha os dados necessários para o próximo passo (subdomínio e senha).

2.  **Autenticação do Usuário (Endpoint: `POST /auth/login`):**
    * **Ação:** Requisição enviada com `subdomain`, `email` e a `temp_password` obtida no passo anterior.
    * **Resultado:** **SUCESSO (`200 OK`)**. O `login-handler` validou o payload, utilizou `encontrar-tenant-por-subdominio` para identificar o tenant correto, e subsequentemente usou `encontrar-usuario-por-email` (com o `tenant-id` já isolado) para localizar o usuário e validar a credencial. Um token JWT simulado foi retornado.
    * **Teste de Falha:** Uma requisição com a senha incorreta resultou em **SUCESSO (`401 Unauthorized`)**, confirmando a lógica de validação de credenciais.

3.  **Acesso a Recursos Protegidos (Endpoint: `GET /api/processos` e derivados):**
    * **Validação de Dados:** Uma requisição `POST /api/processos` com payload incompleto resultou em **SUCESSO (`400 Bad Request`)**, validando a barreira de proteção do `clojure.spec`.
    * **Isolamento de Tenant (Teste Crítico de Segurança):**
        * `GET /api/processos` com `X-Tenant-ID: tenant-1` retornou a lista de processos pertencente **apenas** ao tenant 1.
        * `GET /api/processos` com `X-Tenant-ID: tenant-2` retornou a lista de processos pertencente **apenas** ao tenant 2.
        * `GET /api/processos/proc-333` (recurso do tenant 2) com `X-Tenant-ID: tenant-1` resultou em **SUCESSO (`404 Not Found`)**. Este teste confirmou que a lógica de busca no repositório está corretamente encapsulando a consulta dentro do escopo do `tenant-id` injetado pelo middleware, prevenindo efetivamente o vazamento de dados entre tenants.
    * **Validação do Middleware de Autenticação:** Uma requisição a `/api/processos` sem o header `X-Tenant-ID` resultou em **SUCESSO (`401 Unauthorized`)**, confirmando que o `wrap-tenant-db-repo` está protegendo corretamente os endpoints de negócio.

#### **4. Próximos Passos Arquitetônicos**

A PoC validou com sucesso as premissas fundamentais da arquitetura. A evolução para o produto final seguirá o plano de implementação, focando em:

1.  **Implementação de Autenticação Stateless com JWT:** Substituir a autenticação simulada por um sistema robusto baseado em JWTs, utilizando a biblioteca `buddy-auth`. O `tenant_id` e a `role` do usuário passarão a ser *claims* dentro do payload do JWT, tornando-se a fonte da verdade para o contexto do usuário em requisições subsequentes. O header `X-Tenant-ID` será preterido em favor do `Authorization: Bearer <token>`.
2.  **Migração da Persistência para PostgreSQL:** Substituir a implementação do `MockRepository` por uma nova que interaja com um banco de dados PostgreSQL. Graças ao desacoplamento provido pelos `protocols`, esta migração não exigirá alterações na camada de `handlers` ou na lógica de negócio.
3.  **Identificação de Tenant por Subdomínio:** Evoluir o mecanismo de identificação de tenant para analisar o subdomínio do host da requisição, conforme definido na arquitetura final, substituindo a dependência do header `X-Tenant-ID` na fase de login.
