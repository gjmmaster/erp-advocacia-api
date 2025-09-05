# Documentação: `Dockerfile`

## Visão Geral

O `Dockerfile` é um script que contém uma série de instruções para construir uma **imagem de contêiner** para a aplicação. Uma imagem de contêiner é um pacote leve, autônomo e executável que inclui tudo o que é necessário para rodar uma aplicação: o código, o ambiente de execução (neste caso, o Java Runtime Environment), bibliotecas, variáveis de ambiente e arquivos de configuração.

O uso de contêineres, gerenciados por uma ferramenta como o Docker, garante que a aplicação rode da mesma forma em qualquer ambiente, seja na máquina de um desenvolvedor, em um servidor de testes ou em produção.

Este `Dockerfile` utiliza uma técnica avançada e recomendada chamada **build multi-stage (múltiplos estágios)**.

---

## O Build Multi-Stage

A principal vantagem de um build multi-stage é a criação de uma imagem final **pequena e segura**. A ideia é usar uma imagem grande e cheia de ferramentas para *compilar* a aplicação, e depois copiar *apenas o artefato compilado* para uma imagem final "limpa" e minimalista, que contém apenas o necessário para *executar* a aplicação.

### Etapa 1: O Ambiente de Build (`as builder`)

Esta primeira etapa é responsável por compilar o código-fonte Clojure em um arquivo `.jar` executável.

-   `FROM clojure:lein-2.9.1 as builder`: Começamos com uma imagem base oficial que já vem com o Clojure e a ferramenta de build `Leiningen` instalados. Esta imagem é relativamente grande, pois contém todo o ambiente de desenvolvimento.
-   `WORKDIR /app`: Define o diretório de trabalho padrão dentro do contêiner.
-   `COPY project.clj .` e `RUN lein deps`: Esta é uma otimização importante. Primeiro, copiamos apenas o `project.clj` e rodamos `lein deps` para baixar as dependências. Como o `project.clj` muda com menos frequência que o código-fonte, o Docker pode usar o cache desta camada, acelerando builds futuros.
-   `COPY src ./src`: Copia o código-fonte da aplicação para o contêiner.
-   `RUN lein uberjar`: Executa o comando do Leiningen para compilar todo o código e empacotá-lo, junto com todas as suas dependências, em um único arquivo `.jar` auto-suficiente (o "uberjar").

Ao final desta etapa, temos o arquivo `juridico-api-0.1.0-SNAPSHOT-standalone.jar` dentro do diretório `/app/target/` do contêiner de build.

### Etapa 2: O Ambiente de Execução (Imagem Final)

Esta segunda etapa constrói a imagem final que será distribuída e executada.

-   `FROM openjdk:11-jre-slim`: A imagem final é baseada em uma imagem oficial do OpenJDK que contém **apenas o Java Runtime Environment (JRE)**. Ela não tem o compilador, o Leiningen, nem o código-fonte. Isso a torna muito menor e mais segura (menos superfície de ataque).
-   `WORKDIR /app`: Define o diretório de trabalho na imagem final.
-   `COPY --from=builder /app/target/... ./app.jar`: Esta é a instrução chave do multi-stage. Ela copia o `uberjar` gerado na Etapa 1 (`from=builder`) para a imagem final e o renomeia para `app.jar`.
-   `ENV PORT="3000"`: Define a variável de ambiente `PORT` dentro do contêiner. A aplicação (`core.clj`) lê esta variável para saber em qual porta deve iniciar o servidor.
-   `EXPOSE 3000`: Informa ao Docker que o contêiner escuta na porta `3000` em tempo de execução. Isso não publica a porta, mas serve como documentação e pode ser usado por outras ferramentas.
-   `CMD ["java", "-jar", "app.jar"]`: Define o comando padrão que será executado quando um contêiner for iniciado a partir desta imagem. Ele simplesmente executa o `uberjar` da aplicação.
