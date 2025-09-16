# Documentação: `src/juridico/api/middleware.clj`

## Visão Geral

Este arquivo define os "middlewares" customizados da aplicação. Em uma arquitetura baseada em Ring, um middleware é uma função de alta ordem que adiciona funcionalidades em torno de um handler de requisição. Eles são usados para tratar de questões transversais como autenticação, validação, injeção de dependência e roteamento.

O papel principal dos middlewares neste projeto é:
1.  **Identificar o Tenant:** Determinar qual tenant está a fazer a requisição com base no subdomínio do `Host`.
2.  **Autenticação via JWT:** Validar o token JWT fornecido no header `Authorization` para proteger os endpoints.
3.  **Injeção de Dependência:** Fornecer aos handlers um repositório de banco de dados (`db-repo`) que pode ser de escopo público ou isolado por tenant.
4.  **Autorização baseada em Role:** Controlar o acesso a endpoints específicos com base na "role" do usuário (ex: `master`).

---

## Detalhamento do Código

### Dependências (Namespaces Requeridos)

- `juridico.api.db.postgres :as db`: Importa a implementação do repositório PostgreSQL.
- `juridico.api.db.protocols :as p`: Importa os contratos de persistência.
- `buddy.sign.jwt :as jwt`: Biblioteca usada para assinar e verificar os tokens JWT.
- `juridico.api.config :as config`: Importa a configuração centralizada da aplicação, incluindo a `jwt-secret`.

---

### Funções de Middleware

#### `(wrap-tenant-context [handler])`

Este middleware é o primeiro ponto de entrada para identificar o tenant.

- **Função:** Identificar o tenant com base no subdomínio do header `Host` da requisição.
- **Mecanismo:**
    1.  Extrai o `Host` (ex: `meu-escritorio.api.com`).
    2.  Pega a primeira parte do `Host` como sendo o subdomínio (`meu-escritorio`).
    3.  Usa `p/encontrar-tenant-por-subdominio` para procurar o tenant correspondente na base de dados.
    4.  Se encontrado, injeta o mapa do tenant na requisição sob a chave `:tenant` e passa para o próximo handler.
    5.  Se não encontrado, retorna `404 Not Found`.

#### `(wrap-jwt-authentication [handler])`

Este é o principal middleware de segurança, protegendo os endpoints que requerem autenticação.

- **Função:** Validar um token JWT e injetar um repositório de banco de dados com escopo e a identidade do usuário.
- **Mecanismo:**
    1.  Extrai o token do header `Authorization: Bearer <token>`.
    2.  Usa `jwt/unsign` para verificar a assinatura do token. **Importante:** A verificação usa a chave secreta centralizada de `config/jwt-secret`, garantindo consistência com a chave usada para assinar o token no login.
    3.  Se o token for válido, extrai as "claims" (informações) de dentro dele, como `:user-id`, `:tenant-id` e `:role`.
    4.  Cria uma instância do repositório de dados **isolada para o tenant** do token: `(db/create-repository tenant-id)`.
    5.  Injeta o repositório (`:db-repo`) e a identidade do usuário (`:identity`) na requisição.
    6.  Chama o próximo handler.
    7.  Se o token for inválido, expirado, ou a assinatura não corresponder, o bloco `(catch Exception _ ...)` retorna `401 Unauthorized`.

#### `(wrap-public-db-repo [handler])`

Usado por endpoints públicos (como `/login` e `/provision-tenant`) que precisam de acesso ao banco de dados, mas sem o escopo de um tenant específico.

- **Função:** Fornecer um contexto de banco de dados **não-isolado** ("público").
- **Mecanismo:** Cria uma instância do repositório sem `tenant-id`, `(db/create-repository)`, que pode aceder a dados de todos os tenants.

#### `(wrap-master-role-authorization [handler])`

Um middleware de autorização para proteger endpoints que só devem ser acedidos por administradores de tenant.

- **Função:** Verificar se o usuário autenticado tem a role "master".
- **Mecanismo:**
    1.  Assume que `wrap-jwt-authentication` já foi executado e injetou a `:identity` do usuário.
    2.  Verifica se o campo `:role` dentro de `:identity` é igual a `"master"`.
    3.  Se for, permite o acesso. Caso contrário, retorna `403 Forbidden`.
