# 📝 Comandos Úteis - Next.js BFF

## 🚀 Desenvolvimento

```bash
# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npm run dev

# Build para produção
npm run build

# Iniciar servidor de produção
npm start

# Lint (verificar código)
npm run lint
```

## 🧪 Testes

```bash
# Executar todos os testes
npm test

# Testes em modo watch (re-executa ao salvar)
npm run test:watch

# Cobertura de código
npm run test:coverage

# Executar teste específico
npm test -- login
```

## 🔧 Configuração

```bash
# Copiar template de variáveis
cp .env.example .env.local

# Editar variáveis de ambiente
# Windows: notepad .env.local
# Linux/Mac: nano .env.local
```

## 🐛 Debug

```bash
# Logs detalhados
DEBUG=* npm run dev

# Verificar build
npm run build

# Limpar cache
rm -rf .next
npm run dev
```

## 📦 Dependências

```bash
# Verificar atualizações disponíveis
npm outdated

# Atualizar todas as dependências
npm update

# Atualizar Next.js
npm install next@latest react@latest react-dom@latest

# Reinstalar tudo (se houver problemas)
rm -rf node_modules package-lock.json
npm install
```

## 🔍 Análise

```bash
# Analisar tamanho do bundle
npm run build
# Verificar .next/analyze/

# Verificar tipos TypeScript
npx tsc --noEmit
```

## 🌐 URLs Importantes

```
Desenvolvimento:  http://localhost:3001
Login:            http://localhost:3001/super-admin/login
Dashboard:        http://localhost:3001/super-admin/dashboard
Backend:          http://localhost:3000
```

## 📊 Estrutura de Comandos

```bash
# Fluxo completo de desenvolvimento
cd frontend-nextjs
npm install
cp .env.example .env.local
npm run dev

# Fluxo completo de testes
npm test
npm run test:coverage

# Fluxo completo de deploy
npm run build
npm start
```
