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
3.  **Conversão de Tipos de Dados:**
    - **Importante:** Parâmetros de path como o ID são recebidos como *strings*. O código nos handlers que recebem um ID (`obter-processo-handler`, `atualizar-processo-handler`, etc.) realiza a conversão explícita deste ID para um número (`Long/parseLong`) antes de passá-lo para a camada de banco de dados.
    - Esta conversão é crucial para evitar erros de tipo de dados no PostgreSQL (ex: `unsupported comparison operator: <int> = <varchar>`).
4.  **Valida os Dados (se aplicável):** Usa `(s/valid? ...)` para verificar se os `body-params` correspondem à `spec` definida. Se a validação falhar, retorna imediatamente uma resposta `400 Bad Request` com detalhes do erro extraídos de `(s/explain-data ...)`.
5.  **Executa a Lógica:** Chama as funções do protocolo de persistência (ex: `p/criar-processo`) passando o `db-repo` e os dados necessários.
6.  **Retorna a Resposta:** Constrói e retorna um mapa de resposta do Ring (ex: `{:status 200 :body ...}`).

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
    - Valida o payload de login (`email`, `password`). O subdomínio do tenant é identificado antes pelo middleware `wrap-tenant-context`.
    - Usa `p/encontrar-usuario-por-email` (passando o `tenant-id` do contexto) para localizar o usuário dentro do escopo do seu tenant.
    - **Segurança:** A verificação de senha é feita de forma segura usando a biblioteca `buddy-hashers`, que compara a senha fornecida com o hash criptográfico armazenado no banco de dados.
    - **Geração de Token:** Em caso de sucesso, gera um token JWT real usando `buddy.sign.jwt`. O token é assinado com a chave secreta centralizada em `src/juridico/api/config.clj`, garantindo consistência com a verificação feita no middleware.
    - Retorna o token JWT para o cliente.
