# Documentação: `src/juridico/api/handlers.clj`

## Visão Geral

Este arquivo contém as funções de "handler", que são o coração da lógica de negócios da API. Cada função é responsável por processar uma requisição para um endpoint específico, interagir com a camada de dados e retornar uma resposta apropriada.

Uma característica fundamental da arquitetura deste arquivo é o **desacoplamento da camada de persistência**. Os handlers não sabem qual banco de dados está sendo usado (PostgreSQL, um banco de dados em memória, etc.). Eles interagem com os dados exclusivamente através de um "contrato" (um `protocol` do Clojure), que é injetado em cada requisição pelo middleware.

---

## Detalhamento do Código

### Dependências (Namespaces Requeridos)

- `juridico.api.db.protocols :as p`: Importa os **contratos** de persistência. Toda a comunicação com o banco de dados é feita através das funções deste namespace (ex: `p/listar-processos`). A implementação real é fornecida em tempo de execução.
- `clojure.spec.alpha :as s`: A biblioteca principal do Clojure para validação de dados. É usada para garantir que os dados recebidos nos corpos das requisições (payloads) estejam no formato correto antes de serem processados.
- `juridico.api.specs`: Este namespace contém as definições das `specs` e é carregado para que as `specs` (ex: `:juridico.api.specs/create-process-payload`) fiquem disponíveis para uso.

### O Padrão de um Handler

A maioria dos handlers segue um padrão consistente:

1.  **Recebe a Requisição:** A função recebe o mapa da requisição do Ring.
2.  **Extrai Dados:** Extrai informações relevantes do mapa, como:
    - `:db-repo`: O objeto de repositório de banco de dados, injetado pelo middleware. É a "ponte" para a camada de dados.
    - `:body-params`: O corpo da requisição, já parseado (geralmente de JSON para um mapa Clojure).
    - `:path-params`: Parâmetros da URL (ex: o `{id}` em `/processos/{id}`).
3.  **Valida os Dados (se aplicável):** Usa `(s/valid? ...)` para verificar se os `body-params` correspondem à `spec` definida. Se a validação falhar, retorna imediatamente uma resposta `400 Bad Request` com detalhes do erro extraídos de `(s/explain-data ...)`.
4.  **Executa a Lógica:** Chama as funções do protocolo de persistência (ex: `p/criar-processo`) passando o `db-repo` e os dados necessários.
5.  **Retorna a Resposta:** Constrói e retorna um mapa de resposta do Ring (ex: `{:status 200 :body ...}`).

---

### Funções de Handler

#### Handlers de Processos (Protegidos por Tenant)

Estes handlers operam no contexto de um *tenant* específico, pois o `db-repo` que eles recebem já foi pré-configurado pelo middleware `wrap-tenant-db-repo`.

- `(listar-processos-handler [req])`: Lista todos os processos para o *tenant* atual.
- `(obter-processo-handler [req])`: Busca um único processo pelo `id`. Retorna o processo se encontrado, ou um `404 Not Found` caso contrário.
- `(criar-processo-handler [req])`: Valida o payload de criação e, se for válido, cria um novo processo para o *tenant*.

#### Handlers de Provisionamento e Autenticação (Públicos)

Estes handlers operam em um contexto "global", pois o `db-repo` que eles recebem do middleware `wrap-public-db-repo` não tem escopo de *tenant*.

- `(provision-tenant-handler [req])`:
    - Valida o payload para criação de um novo *tenant* (nome da empresa, e-mail do admin).
    - Chama a função de protocolo `p/criar-tenant-e-usuario-master` para realizar o provisionamento.
    - Retorna os dados do *tenant* criado e uma senha temporária.

- `(login-handler [req])`:
    - Valida o payload de login (`subdomain`, `email`, `password`).
    - Primeiro, usa `p/encontrar-tenant-por-subdominio` para identificar para qual *tenant* o login se destina.
    - Em seguida, usa `p/encontrar-usuario-por-email` (passando o `tenant-id` encontrado) para localizar o usuário dentro do escopo daquele *tenant*.
    - **Importante:** A verificação de senha (`(= password (:password_hash user))`) é uma **simplificação para a Prova de Conceito (PoC)**. Em um ambiente de produção, esta linha seria substituída por uma verificação de hash criptográfico seguro (ex: usando uma biblioteca como `buddy-hashers`).
    - Retorna um token JWT simulado em caso de sucesso ou `401 Unauthorized` em caso de falha.
