#!/bin/bash

# Script para iniciar backend (modo mock) e frontend simultaneamente

echo ""
echo "========================================"
echo "🔧 MODO DE DESENVOLVIMENTO COMPLETO"
echo "========================================"
echo ""
echo "📦 Backend: Repositório MOCK (in-memory)"
echo "🌐 Frontend: Next.js Dev Server"
echo ""
echo "👤 Credenciais de acesso:"
echo "   Super Admin: admin@demo.com / admin123"
echo "   Master User: master@demo.com / master123"
echo "   Operador 1:  operador1@demo.com / operador123"
echo "   Operador 2:  operador2@demo.com / operador123"
echo ""
echo "========================================"
echo ""

# Verifica se o backend já está rodando
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
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
sleep 3

# Verifica se o frontend já está rodando
if lsof -Pi :3001 -sTCP:LISTEN -t >/dev/null ; then
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

# Aguarda interrupção
wait
