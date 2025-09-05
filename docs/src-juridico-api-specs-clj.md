# Documentação: `src/juridico/api/specs.clj`

## Visão Geral

Este arquivo é o "dicionário de dados" da aplicação, definido usando a biblioteca `clojure.spec`. O `clojure.spec` é uma ferramenta poderosa para especificar a "forma" das estruturas de dados. Neste projeto, ele é usado como uma camada de validação na borda da API para garantir que qualquer dado que entre no sistema (via payloads de requisição) esteja correto e completo.

Manter as definições das `specs` em um arquivo centralizado torna a aplicação mais robusta, fácil de entender e de manter. Os `handlers` usam essas `specs` para validar os dados de entrada antes de executar qualquer lógica de negócio.

---

## Detalhamento do Código

### Dependências (Namespaces Requeridos)

- `clojure.spec.alpha :as s`: A biblioteca `clojure.spec`, que fornece todas as funções para definir e usar as especificações (ex: `s/def`, `s/keys`, `s/and`).

---

### Definições de Specs

As `specs` são definidas usando `s/def` com um keyword qualificado (ex: `::email`). Isso registra uma especificação globalmente.

#### Specs para Entidades de Processo

- `::case_number`: Define o número de um processo. Deve ser uma `string` e não pode ser vazia.
- `::jurisdiction`: Define a jurisdição de um processo. Deve ser uma `string` e não pode ser vazia.
- `::create-process-payload`: Define a estrutura do corpo da requisição para criar um novo processo.
    - É um mapa (`s/keys`).
    - Exige as chaves `:case_number` e `:jurisdiction` (`:req-un`). O valor de cada chave deve estar em conformidade com as `specs` `::case_number` e `::jurisdiction`, respectivamente.

#### Specs para Provisionamento e Login

Estas `specs` definem os contratos para os fluxos de administração de *tenants* e autenticação de usuários.

- `::email`: Define um e-mail válido. Deve ser uma `string` que corresponda à expressão regular `.+@.+\..+`.
- `::company_name`: Define o nome de uma empresa (*tenant*). Deve ser uma `string` não vazia.
- `::password`: Define uma senha. Para a PoC, a regra é simples: deve ser uma `string` com mais de 3 caracteres.
- `::subdomain`: Define um subdomínio. Deve ser uma `string` não vazia.

- `::provision-payload`: Define a estrutura do corpo da requisição para provisionar um novo *tenant*.
    - Exige as chaves `:company_name` e `:email`.

- `::login-payload`: Define a estrutura do corpo da requisição para o endpoint de login.
    - Exige as chaves `:subdomain`, `:email`, e `:password`.

### Como as Specs são Utilizadas

No namespace `juridico.api.handlers`, você encontrará um padrão como este:

```clojure
(if (s/valid? ::login-payload body-params)
  ;; ... executa a lógica de login ...
  ;; ... retorna 400 Bad Request com detalhes do erro ...
  )
```

- `(s/valid? ::login-payload body-params)`: Verifica se o mapa `body-params` está em conformidade com a `spec` `::login-payload`. Retorna `true` ou `false`.
- Se a validação falhar, o `handler` retorna uma resposta `400 Bad Request`. Geralmente, o corpo dessa resposta inclui informações de depuração geradas por `(s/explain-data ::login-payload body-params)`, que informa ao cliente exatamente qual parte dos dados estava incorreta.

Essa abordagem impede que dados malformados ou incompletos cheguem à lógica de negócio principal ou à camada de banco de dados.
