# Documentação: `src/juridico/api/middleware.clj`

## Visão Geral

Este arquivo define os "middlewares" customizados da aplicação. Em uma arquitetura baseada em Ring (o padrão de facto para aplicações web em Clojure), um middleware é uma função de alta ordem que adiciona funcionalidades em torno de um handler de requisição. Eles são usados para tratar de questões transversais como autenticação, logging, gerenciamento de sessão e, neste projeto, **injeção de dependência**.

O papel principal deste namespace é instanciar a implementação **concreta** do repositório de dados e injetá-la no mapa da requisição, para que os `handlers` possam usá-la de forma desacoplada.

**Ponto Importante:** Este é o local do código que decide **qual** implementação de banco de dados será usada. Note que ele depende diretamente de `juridico.api.db.mock`. Se a aplicação fosse migrada para usar PostgreSQL, este seria um dos principais arquivos a serem modificados para instanciar o novo repositório PostgreSQL em vez do `mock`.

---

## Detalhamento do Código

### Dependências (Namespaces Requeridos)

- `juridico.api.db.mock :as db.mock`: Importa a implementação do repositório de banco de dados em memória (`mock`). É a partir daqui que os objetos de repositório são criados.

---

### Funções de Middleware

#### `(wrap-tenant-db-repo [handler])`

Este é o middleware de segurança e contexto para todas as rotas de negócio protegidas (aquelas sob `/api`).

- **Função:** Proteger um endpoint e fornecer-lhe um contexto de banco de dados **isolado por tenant**.
- **Mecanismo:**
    1.  Recebe o próximo `handler` na cadeia de processamento.
    2.  Retorna uma nova função que inspeciona a requisição (`request`).
    3.  Procura pelo header `x-tenant-id` nos cabeçalhos da requisição.
    4.  **Se o header `x-tenant-id` existir:**
        - Cria uma instância do repositório de dados passando o `tenant-id`: `(db.mock/create-repository tenant-id)`. Isto garante que o repositório resultante só possa "ver" os dados pertencentes àquele *tenant*.
        - Adiciona essa instância ao mapa da requisição sob a chave `:db-repo`.
        - Chama o `handler` original, passando a requisição modificada.
    5.  **Se o header `x-tenant-id` NÃO existir:**
        - Interrompe o processamento da requisição (não chama o `handler`).
        - Retorna imediatamente uma resposta `401 Unauthorized`, bloqueando o acesso ao recurso.

#### `(wrap-public-db-repo [handler])`

Este é o middleware de contexto para as rotas públicas que ainda precisam de acesso ao banco de dados, como o provisionamento de novos *tenants* ou o login.

- **Função:** Fornecer um contexto de banco de dados **não-isolado** (ou "público").
- **Mecanismo:**
    1.  Recebe o próximo `handler` na cadeia.
    2.  Retorna uma nova função que sempre executa o seguinte:
    3.  Cria uma instância do repositório de dados **sem** especificar um `tenant-id`: `(db.mock/create-repository)`. Esta instância tem a capacidade de operar em dados de todos os *tenants* (por exemplo, para encontrar um *tenant* pelo subdomínio durante o login).
    4.  Adiciona essa instância à requisição na chave `:db-repo`.
    5.  Chama o `handler` original com a requisição modificada.

Esses dois middlewares são, portanto, a peça central que implementa a estratégia de multi-tenancy da aplicação, garantindo que a camada de lógica (`handlers`) receba o "mundo" de dados correto para operar.
