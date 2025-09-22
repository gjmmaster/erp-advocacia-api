# Documentação: `src/juridico/api/core.clj`

## Visão Geral

Este é o arquivo principal e ponto de entrada (`main`) da aplicação `juridico-api`. Sua responsabilidade central é configurar e iniciar o servidor web, definir todas as rotas da API e conectar os middlewares necessários para o processamento das requisições.

Ele utiliza a biblioteca `reitit` para uma definição de rotas clara e baseada em dados, e o `ring-jetty-adapter` para servir a aplicação.

---

## Detalhamento do Código

### Dependências (Namespaces Requeridos)

O namespace `juridico.api.core` importa várias bibliotecas e módulos internos essenciais:

- `ring.adapter.jetty`: Utilizado para iniciar o servidor web Jetty que hospedará a aplicação.
- `reitit.ring`: A biblioteca principal para a criação e gerenciamento das rotas da API.
- `reitit.ring.middleware.muuntaja`: Um middleware que se integra ao `reitit` para automaticamente negociar e transformar formatos de dados (ex: converter request bodies de JSON para mapas Clojure e vice-versa).
- `muuntaja.core`: A biblioteca de negociação de conteúdo subjacente.
- `juridico.api.handlers`: Importa as funções que efetivamente lidam com a lógica de cada rota (ex: `provision-tenant-handler`).
- `juridico.api.middleware`: Importa os middlewares customizados da aplicação, cruciais para a lógica de multi-tenancy.

### Definição das Rotas (`def routes`)

A variável `routes` define a estrutura completa de todas as rotas da API de forma hierárquica. As rotas são divididas em seções lógicas, cada uma protegida por uma cadeia de middlewares específica:

1.  **Rotas de Super Administração (`/admin`)**:
    - **Middleware:** `mw/wrap-jwt-authentication`, `mw/wrap-super-admin-authorization`, `mw/wrap-public-db-repo`.
    - **Propósito:** Estas rotas são usadas para o gerenciamento global do sistema e só podem ser acessadas por um usuário com a role `super-admin`. Elas incluem o provisionamento de novos tenants e o CRUD completo para gerenciar tenants existentes.

2.  **Rotas de Autenticação (`/auth`)**:
    - **Middleware:** `mw/wrap-public-db-repo`, `mw/wrap-tenant-context`.
    - **Propósito:** Rota pública para que usuários de um tenant específico (identificado pelo subdomínio) possam se autenticar e obter um token JWT.

3.  **Rotas Protegidas da API (`/api`)**:
    - **Middleware:** `mw/wrap-jwt-authentication`.
    - **Propósito:** Estas são as rotas de negócio da aplicação, que manipulam dados pertencentes a um tenant específico. O acesso é validado via token JWT.
    - **Sub-rotas de Gestão de Operadores (`/api/operadores`)**:
        - **Middleware Adicional:** `mw/wrap-master-role-authorization`.
        - **Propósito:** Protege especificamente as rotas de gestão de operadores, garantindo que apenas o usuário `master` do tenant possa criar, listar, atualizar ou deletar outros usuários.

### Handler Principal da Aplicação (`def app`)

A variável `app` constrói o handler principal do Ring, que é a função que processa todas as requisições HTTP recebidas. Ela é composta por:

1.  `ring/router`: Pega a estrutura de `routes` e a transforma em um roteador funcional.
2.  `{:data {:muuntaja m/instance, :middleware [...]}}`: Configura o roteador. Aqui, ele é instruído a usar o `muuntaja` para processar corpos de requisição e resposta (JSON, etc.) automaticamente.
3.  `ring/create-default-handler`: Define o que acontece se nenhuma rota corresponder à requisição. Neste caso, retorna uma resposta `404 Not Found`.

### Ponto de Entrada (`defn -main`)

A função `-main` é o ponto de entrada padrão para uma aplicação Clojure. Suas ações são:

1.  Obter a porta do servidor a partir da variável de ambiente `PORT`. Se não estiver definida, usa `3000` como padrão.
2.  Imprimir uma mensagem no console indicando que o servidor está iniciando.
3.  Chamar `jetty/run-jetty` para iniciar o servidor web na porta especificada, passando o handler `app` para processar as requisições. A opção `:join? false` permite que o servidor rode em uma thread separada, não bloqueando o processo principal (útil em desenvolvimento).
