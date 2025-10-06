# Estágio de Build do Backend
FROM clojure:lein-2.9.1 as backend-builder
WORKDIR /app

# Copia os arquivos de definição do projeto e baixa as dependências
COPY project.clj .
RUN lein deps

# Copia o código-fonte do backend
COPY src ./src

# Compila a aplicação em um uberjar
RUN lein uberjar

# --- Estágio Final de Execução ---
FROM openjdk:11-jre-slim
WORKDIR /app

# Copia APENAS o uberjar final do estágio de build
COPY --from=backend-builder /app/target/jurico-api-0.1.0-SNAPSHOT-standalone.jar ./app.jar

ENV PORT="3000"
EXPOSE 3000
CMD ["java", "-jar", "app.jar"]
