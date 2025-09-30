# --- Estágio 1: Build do Frontend ---
FROM node:18-alpine as frontend-builder
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm install
COPY frontend/ ./
# A configuração do base: './' no vite.config.js garante caminhos relativos
RUN npm run build

# --- Estágio 2: Build do Backend (Unificado) ---
FROM clojure:lein-2.9.1 as backend-builder
WORKDIR /app

# Copia os arquivos de definição do projeto e dependências
COPY project.clj .
RUN lein deps

# Copia o código-fonte do backend
COPY src ./src

# A MÁGICA ACONTECE AQUI:
# Copia os arquivos prontos do frontend para a pasta de recursos do backend.
# O Leiningen irá empacotar esta pasta DENTRO do .jar final.
COPY --from=frontend-builder /app/frontend/dist ./resources/public

# Compila a aplicação em um uberjar que agora contém o frontend
RUN lein uberjar

# --- Estágio 3: Imagem Final de Execução ---
FROM openjdk:11-jre-slim
WORKDIR /app

# Copia APENAS o uberjar final, que já contém tudo (backend e frontend)
COPY --from=backend-builder /app/target/juridico-api-0.1.0-SNAPSHOT-standalone.jar ./app.jar

ENV PORT="3000"
EXPOSE 3000
CMD ["java", "-jar", "app.jar"]
