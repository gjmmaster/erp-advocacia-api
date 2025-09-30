# --- Estágio 1: Build do Frontend ---
# Usamos uma imagem oficial do Node.js para construir a aplicação React.
FROM node:18-alpine as frontend-builder

# Definimos o diretório de trabalho para o código do frontend.
WORKDIR /app/frontend

# Copiamos os arquivos de gerenciamento de pacotes primeiro para aproveitar o cache.
COPY frontend/package.json frontend/package-lock.json ./

# Instalamos as dependências do frontend.
RUN npm install

# Copiamos o restante do código-fonte do frontend.
COPY frontend/ ./

# Executamos o build de produção do React.
RUN npm run build


# --- Estágio 2: Build do Backend (O seu estágio original) ---
# Use a imagem oficial do Leiningen para compilar o projeto
FROM clojure:lein-2.9.1 as backend-builder

# Defina o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copie os arquivos de definição do projeto primeiro para aproveitar o cache do Docker
COPY project.clj .
# Baixe as dependências. Isso será cacheado se o project.clj não mudar.
RUN lein deps

# Copie o código-fonte da aplicação
COPY src ./src

# Compile a aplicação em um uberjar (JAR auto-suficiente)
RUN lein uberjar


# --- Estágio 3: Imagem Final de Execução (Unificada) ---
# Use uma imagem leve com apenas o Java Runtime Environment (JRE)
FROM openjdk:11-jre-slim

# Defina o diretório de trabalho
WORKDIR /app

# 1. Copie o uberjar da etapa de build do backend para a imagem final
COPY --from=backend-builder /app/target/juridico-api-0.1.0-SNAPSHOT-standalone.jar ./app.jar

# 2. Copie os arquivos estáticos da etapa de build do frontend DIRETAMENTE PARA A RAIZ
#    O ponto final (.) significa copiar para o WORKDIR atual (/app).
COPY --from=frontend-builder /app/frontend/dist .

# Defina as variáveis de ambiente
ENV PORT="3000"

# Exponha a porta que o servidor web irá usar
EXPOSE 3000

# Comando para executar a aplicação quando o contêiner iniciar
CMD ["java", "-jar", "app.jar"]
