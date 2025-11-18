#!/bin/bash

# Script para iniciar backend e frontend em modo de desenvolvimento com repositório mock

echo ""
echo "========================================"
echo "🔧 MODO DE DESENVOLVIMENTO COMPLETO"
echo "========================================"
echo ""
echo "📦 Backend: Repositório MOCK (in-memory)"
echo "🌐 Frontend: Next.js Dev Server"
echo "⚡ Dados não persistem entre reinícios"
echo ""
echo "👤 Credenciais de acesso:"
echo "   Super Admin: admin@demo.com / admin123"
echo "   Master User: master@demo.com / master123"
echo "   Operador 1:  operador1@demo.com / operador123"
echo "   Operador 2:  operador2@demo.com / operador123"
echo ""
echo "========================================"
echo ""

# Verificar dependências
if ! command -v lein &> /dev/null; then
    echo "❌ ERRO: Leiningen não está instalado"
    echo "   Instale com: brew install leiningen (Mac) ou veja https://leiningen.org"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo "❌ ERRO: Node.js/npm não está instalado"
    echo "   Instale com: brew install node (Mac) ou veja https://nodejs.org"
    exit 1
fi

if [ ! -d "frontend-nextjs" ]; then
    echo "❌ ERRO: Diretório frontend-nextjs não encontrado"
    echo "   Execute este script da raiz do projeto"
    exit 1
fi

# Verificar se o backend já está rodando
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Backend já está rodando na porta 3000"
    echo "   Se quiser reiniciar, pare o processo primeiro"
else
    # Define variável de ambiente para modo dev
    export DEV_MODE=true
    
    # Inicia o backend em background
    echo "🚀 Iniciando backend (porta 3000)..."
    lein run &
    BACKEND_PID=$!
    echo "   Backend PID: $BACKEND_PID"
fi

# Aguarda um pouco para o backend iniciar
echo "⏳ Aguardando backend inicializar..."
sleep 3

# Verificar se o frontend já está rodando
if lsof -Pi :3001 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Frontend já está rodando na porta 3001"
else
    # Inicia o frontend
    echo ""
    echo "🌐 Iniciando frontend (porta 3001)..."
    cd frontend-nextjs
    npm run dev &
    FRONTEND_PID=$!
    echo "   Frontend PID: $FRONTEND_PID"
    cd ..
fi

echo ""
echo "========================================"
echo "✅ Ambiente de desenvolvimento pronto!"
echo "========================================"
echo ""
echo "🔗 URLs:"
echo "   Backend:  http://localhost:3000"
echo "   Frontend: http://localhost:3001"
echo ""
echo "Para parar os servidores, pressione Ctrl+C"
echo ""

# Capturar Ctrl+C e encerrar processos filhos
trap 'echo ""; echo "🛑 Encerrando serviços..."; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit' INT TERM

# Aguarda interrupção
wait
