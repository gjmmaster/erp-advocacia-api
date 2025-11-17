#!/bin/bash

# ========================================
# Script de Desenvolvimento COMPLETO
# ========================================
# Este script inicia BACKEND + FRONTEND simultaneamente
# em modo desenvolvimento
# ========================================

echo "🚀 Iniciando BACKEND + FRONTEND em modo DESENVOLVIMENTO..."
echo ""

# Função para limpar processos ao sair
cleanup() {
    echo ""
    echo "🛑 Encerrando servidores..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    exit
}

trap cleanup EXIT INT TERM

# Carregar variáveis do .env.development para o backend
export $(cat .env.development | grep -v '^#' | xargs)

# Iniciar Backend
echo "📦 Iniciando Backend (porta 3000)..."
lein run &
BACKEND_PID=$!

# Aguardar backend iniciar
sleep 5

# Iniciar Frontend
echo "🎨 Iniciando Frontend (porta 3001)..."
cd frontend-nextjs
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "✅ Servidores iniciados!"
echo ""
echo "📍 Backend:  http://localhost:3000"
echo "📍 Frontend: http://localhost:3001"
echo ""
echo "Pressione Ctrl+C para encerrar ambos os servidores"
echo ""

# Manter o script rodando
wait
