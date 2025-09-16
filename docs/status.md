### **Diário de Bordo Técnico: Evolução da Arquitetura Multi-Tenant**

**Data de Referência:** 05 de Setembro de 2025
**Status:** Segunda fase da PoC concluída: Autenticação stateless com JWT implementada e validada.

---

### **Etapa 1: Prova de Conceito (PoC) da Arquitetura Multi-Tenant**

Esta fase inicial validou os pilares da arquitetura, incluindo o isolamento de dados via `tenant_id`, o desacoplamento da persistência com `protocols`, a validação de contratos com `clojure.spec`, e o ciclo de vida básico do tenant.

*   **Validação Crítica:** Demonstrou-se que um tenant não poderia acessar dados de outro, utilizando um header `X-Tenant-ID` para injetar o contexto de segurança.
*   **Resultado:** A arquitetura base foi considerada **bem-sucedida**, abrindo caminho para a implementação de um mecanismo de autenticação robusto.

---

### **Etapa 2: Implementação de Autenticação Stateless com JWT**

Nesta segunda fase, o mecanismo de autenticação simulado foi substituído por um sistema completo e seguro baseado em JSON Web Tokens (JWT), utilizando a biblioteca `buddy-auth`.

#### **2.1. Arquitetura de Autenticação com JWT**

*   **Geração de Token no Login:** O `login-handler` foi aprimorado. Após validar as credenciais do usuário com `buddy.hashers`, ele agora gera um token JWT assinado.
    *   **Claims do JWT:** O payload do token (claims) é enriquecido com dados essenciais para o controle de acesso:
        *   `:user-id`: Identificador do usuário autenticado.
        *   `:tenant-id`: Identificador do tenant ao qual o usuário pertence. **Esta é a fonte da verdade para o isolamento de dados.**
        *   `:role`: Papel do usuário (e.g., `:admin`, `:operador`).
        *   `:exp`: Timestamp de expiração do token (atualmente configurado para 1 hora), garantindo que as sessões sejam automaticamente invalidadas.

*   **Novo Middleware de Autenticação (`wrap-jwt-authentication`):**
    *   Este middleware substitui completamente o antigo `wrap-tenant-db-repo`.
    *   Ele é responsável por inspecionar o header `Authorization: Bearer <token>` em todas as requisições para endpoints protegidos.
    *   Utilizando `buddy.sign.jwt/unsign`, ele valida a assinatura e a expiração do token.
    *   Se o token for válido, as *claims* são extraídas. A `claim` `:tenant-id` é usada para instanciar o repositório de dados (`db-repo`) com o escopo correto, garantindo que todas as operações de banco de dados subsequentes fiquem restritas àquele tenant.
    *   A identidade completa do usuário (`identity`), contendo todas as claims, é injetada na requisição para uso futuro na lógica de negócios (e.g., autorização baseada em roles).
    *   O header `X-Tenant-ID` foi **completamente removido** e não é mais necessário para acessar a API.

#### **2.2. Validação Funcional da Autenticação JWT**

Os testes de integração foram atualizados para refletir o novo fluxo de autenticação.

1.  **Provisionamento e Login (Fluxo Inalterado):**
    * `POST /admin/provision-tenant` e `POST /auth/login` continuam funcionando como antes.
    * **Resultado do Login:** A resposta de um login bem-sucedido agora inclui um `token` JWT.
      ```json
      {
        "message": "Usuário admin@empresa-a.com autenticado com sucesso.",
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyLWlkIjoi..."
      }
      ```

2.  **Acesso a Recursos Protegidos com Token JWT:**
    * **Ação:** Para acessar endpoints como `/api/processos`, o cliente agora deve incluir o token JWT no header `Authorization`.
    * **Exemplo de Requisição (`curl`):**
      ```bash
      # Assumindo que a variável $JWT_TOKEN contém o token obtido no login
      curl -X GET http://localhost:3000/api/processos \
        -H "Authorization: Bearer $JWT_TOKEN"
      ```
    * **Validação do Isolamento de Tenant:** O teste crítico de segurança foi revalidado com sucesso. Um token gerado para o `tenant-1` **não permite** o acesso a recursos do `tenant-2`, resultando em um `404 Not Found`, pois o repositório instanciado pelo middleware só "enxerga" os dados do `tenant-1`.
    * **Validação do Middleware:** Uma requisição a `/api/processos` sem o header `Authorization` (ou com um token inválido/expirado) resulta em **SUCESSO (`401 Unauthorized`)**, confirmando a robustez da camada de segurança.

---

### **3. Próximos Passos Arquitetônicos**

Com a PoC e a autenticação JWT validadas, a evolução para o produto final seguirá o plano, focando em:

1.  **Migração da Persistência para PostgreSQL:** Substituir a implementação do `MockRepository` por uma nova que interaja com um banco de dados PostgreSQL. Graças ao desacoplamento provido pelos `protocols`, esta migração não exigirá alterações na camada de `handlers` ou na lógica de negócio.
2.  **Identificação de Tenant por Subdomínio na Requisição:** Evoluir o mecanismo de identificação de tenant para analisar o subdomínio do host da requisição (e.g., `tenant-a.meuerp.com`), conforme definido na arquitetura final. Isso simplificará o processo de login, eliminando a necessidade de enviar o `subdomain` no corpo da requisição.

---

### **Etapa 3: Migração para PostgreSQL e Implementação de RBAC**

**Data de Referência:** 16 de Setembro de 2025
**Status:** Fase de produção iniciada. A persistência em memória foi substituída por um banco de dados PostgreSQL (CockroachDB) e o primeiro fluxo de autorização baseado em papéis (RBAC) foi implementado.

#### **3.1. Migração da Camada de Persistência para PostgreSQL (CockroachDB)**

A camada de dados, que antes utilizava um `atom` do Clojure para simulação, foi migrada para um cluster CockroachDB, compatível com o protocolo PostgreSQL, garantindo a permanência e a segurança dos dados.

*   **Estrutura do Banco de Dados:**
    *   **Tabelas Criadas:** `tenants`, `users`, `legal_cases`.
    *   **Constraints de Integridade:** `UNIQUE` na coluna `subdomain` da tabela `tenants`, e `FOREIGN KEY` com `ON DELETE CASCADE` na coluna `tenant_id` das tabelas `users` e `legal_cases` para garantir o isolamento e a integridade referencial dos dados.
    *   **Segurança:** `UNIQUE` na combinação de `(tenant_id, email)` na tabela `users` para impedir emails duplicados dentro do mesmo tenant.

*   **Alterações na Aplicação Clojure:**
    *   **Dependências:** Adicionadas `[com.github.seancorfield/next.jdbc "1.3.894"]` e `[org.postgresql/postgresql "42.7.3"]` ao `project.clj`.
    *   **Nova Camada de Persistência (`postgres.clj`):** Criado o namespace `juridico.api.db.postgres` com um `defrecord PostgresRepository` que implementa os protocolos `ProcessosRepository` e `AuthRepository`, traduzindo cada função para consultas SQL.
    *   **Lógica de Conexão Robusta:**
        *   **Inicialização Atrasada (`delay`):** A criação do datasource foi encapsulada para evitar a tentativa de conexão durante a compilação AOT.
        *   **Parsing da URL de Conexão:** Uma nova função `parse-db-url` foi criada para desmontar a `DATABASE_URL` via regex, resolvendo problemas de parsing do driver JDBC (`UnknownHostException`).
        *   **Configuração de SSL:** O parâmetro `:sslmode "require"` foi adicionado para garantir a conexão segura com o cluster CockroachDB.
        *   **Recuperação de Chaves Geradas:** O código foi ajustado para ler a chave qualificada `:tenants/id` retornada pelo `next.jdbc` após inserções, resolvendo `not-null constraint violation` na criação de usuários.

#### **3.2. Implementação do Controle de Acesso Baseado em Papel (RBAC)**

Com a base de dados funcional, a camada de segurança foi aprimorada para autorizar ações com base no papel do usuário.

*   **Middleware de Autorização (`wrap-master-role-authorization`):**
    *   Um novo middleware foi criado em `juridico.api.middleware`.
    *   Ele inspeciona a `role` na identidade do token JWT. Se a `role` for `"master"`, a requisição prossegue; caso contrário, é retornada uma resposta `403 Forbidden`.

*   **Proteção de Rotas:**
    *   No arquivo `juridico.api.core.clj`, um novo aninhamento de rotas `/api/operadores` foi criado.
    *   O middleware `wrap-master-role-authorization` foi aplicado a este aninhamento, protegendo todos os endpoints (GET, POST) e garantindo que sejam acessíveis apenas por usuários "master".

#### **3.3. Funcionalidade de Gestão de Operadores (Primeira Feature RBAC)**

*   **Extensão da Camada de Dados:**
    *   O protocolo `AuthRepository` foi estendido com as funções `listar-usuarios-do-tenant` e `criar-usuario-operador`.
    *   **Implementação em `postgres.clj`:**
        *   `listar-usuarios-do-tenant`: Executa um `SELECT` na tabela `users` omitindo o `password_hash` por segurança.
        *   `criar-usuario-operador`: Executa um `INSERT` na tabela `users`, criptografando a senha com `buddy.hashers` e fixando a `role` como `"operador"`.

*   **Novos Handlers e Specs:**
    *   Criados os handlers `listar-operadores-handler` e `criar-operador-handler`, que extraem o `tenant-id` diretamente da identidade do token JWT.
    *   Definida a spec `::create-operator-payload` para validar as requisições de criação.

*   **Validação em Produção:**
    *   A funcionalidade foi validada de ponta a ponta com testes `curl`, confirmando que usuários "master" podem criar e listar operadores, e que operadores recebem um token com a `role` correta e são bloqueados pelo middleware de autorização ao tentar acessar recursos protegidos.
