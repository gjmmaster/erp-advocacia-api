# Estágio de Build do Frontend Next.js
FROM node:18-alpine AS builder
WORKDIR /app

# Copia os arquivos de definição do projeto
COPY package*.json ./
COPY tsconfig.json ./
COPY next.config.js ./

# Instala TODAS as dependências (incluindo devDependencies para build)
RUN npm ci && \
    npm cache clean --force

# Copia TODO o código-fonte
COPY . .

# Build da aplicação Next.js
RUN npm run build

# --- Estágio Final de Execução ---
FROM node:18-alpine
WORKDIR /app

# Criar um usuário e grupo não-root para executar a aplicação
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

# Copia apenas os arquivos necessários do estágio de build
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.js ./

# Mudar o proprietário dos arquivos para o usuário não-root
RUN chown -R nextjs:nodejs /app

# Mudar para o usuário não-root
USER nextjs

# Expor a porta (Render usa variável PORT)
ENV PORT=3001
EXPOSE 3001

# Comando para iniciar a aplicação
CMD ["npm", "start"]
