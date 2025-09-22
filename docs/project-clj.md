# Documentação: `project.clj`

## Visão Geral

O arquivo `project.clj` é o coração de um projeto gerenciado pelo **Leiningen**, a ferramenta de automação de build mais popular para Clojure. Este arquivo declarativo informa ao Leiningen tudo o que ele precisa saber sobre o projeto: seu nome, sua versão, suas dependências, seu ponto de entrada principal e muito mais.

É um arquivo de código Clojure, o que o torna extremamente poderoso e configurável, mas para a maioria dos usos, ele consiste em uma única macro `defproject` com uma série de diretivas (palavras-chave).

---

## Detalhamento das Diretivas

### `(defproject juridico-api "0.1.0-SNAPSHOT" ...)`

Esta é a definição principal do projeto.

-   `juridico-api`: O nome do projeto (group-id e artifact-id no jargão do Maven).
-   `"0.1.0-SNAPSHOT"`: A versão atual do projeto. A palavra-chave `SNAPSHOT` indica que esta é uma versão de desenvolvimento e não uma release estável.

### `:description "FIXME: write description"`

-   Uma breve descrição do projeto. O valor "FIXME" é um placeholder que deve ser substituído por uma descrição real.

### `:url "http://example.com/FIXME"`

-   A URL do site do projeto ou do repositório de código. Também é um placeholder.

### `:license {...}`

-   Define a licença de software sob a qual o projeto é distribuído.

### `:dependencies [[...]]`

Esta é uma das seções mais importantes. Ela define a lista de todas as bibliotecas de terceiros das quais o projeto depende. O Leiningen lerá esta lista e baixará automaticamente as bibliotecas e suas dependências transitivas dos repositórios públicos (como Clojars e Maven Central) na primeira vez que o projeto for construído.

As dependências deste projeto são:

-   `[org.clojure/clojure "1.11.1"]`: A própria linguagem Clojure.
-   `[ring/ring-core "1.9.5"]`: A especificação principal do Ring, que fornece a abstração base para aplicações web em Clojure (mapas de requisição e resposta).
-   `[ring/ring-jetty-adapter "1.9.5"]`: Um "adaptador" que permite que uma aplicação Ring seja executada em um servidor web Jetty.
-   `[metosin/reitit "0.5.18"]`: Uma biblioteca moderna e performática para roteamento de requisições. É usada em `core.clj` para definir todas as rotas da API.
-   `[metosin/muuntaja "0.6.8"]`: Uma biblioteca para negociação de formato de conteúdo. Ela é responsável por, por exemplo, converter automaticamente o corpo de uma requisição JSON em um mapa Clojure e vice-versa.
-   `[ring-cors "0.1.13"]`: Um middleware Ring para adicionar headers de Cross-Origin Resource Sharing (CORS) às respostas. Isso é crucial para permitir que aplicações de frontend (servidas de um domínio diferente) possam fazer requisições à API.
-   `[environ "1.2.0"]`: Uma biblioteca para gerenciar configurações a partir de variáveis de ambiente, facilitando a configuração da aplicação em diferentes ambientes (desenvolvimento, produção) sem modificar o código.

### `:main juridico.api.core`

Esta diretiva informa ao Leiningen qual namespace contém a função `-main`, que é o ponto de entrada da aplicação. Isso é usado quando você executa `lein run` ou quando constrói um `.jar` executável (`uberjar`).

### `:repl-options {:init-ns juridico.api.core}`

-   Configura o comportamento do REPL (Read-Eval-Print Loop). A opção `:init-ns` instrui o Leiningen a carregar e mudar para o namespace `juridico.api.core` assim que o REPL for iniciado com o comando `lein repl`. Isso é uma conveniência para o desenvolvimento interativo.
