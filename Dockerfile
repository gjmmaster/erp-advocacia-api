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
FROM eclipse-temurin:11-jre-jammy
WORKDIR /app

# Criar um usuário e grupo não-root para executar a aplicação
RUN addgroup --system app && adduser --system --ingroup app app

# Copia APENAS o uberjar final do estágio de build
COPY --from=backend-builder /app/target/juridico-api-0.1.0-SNAPSHOT-standalone.jar ./app.jar

# Mudar o proprietário dos arquivos da aplicação para o usuário não-root
RUN chown app:app app.jar

# Mudar para o usuário não-root
USER app

ENV PORT="3000"
EXPOSE 3000
CMD ["java", "-jar", "app.jar"]
