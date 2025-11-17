#!/bin/bash

# Script para iniciar o backend em modo de desenvolvimento com repositório mock

echo ""
echo "========================================"
echo "🔧 MODO DE DESENVOLVIMENTO (MOCK)"
echo "========================================"
echo ""
echo "📦 Usando repositório MOCK (in-memory)"
echo "⚡ Dados não persistem entre reinícios"
echo "🚀 Ideal para desenvolvimento rápido"
echo ""
echo "👤 Credenciais de acesso:"
echo "   Super Admin: admin@demo.com / admin123"
echo "   Master User: master@demo.com / master123"
echo "   Operador 1:  operador1@demo.com / operador123"
echo "   Operador 2:  operador2@demo.com / operador123"
echo ""
echo "========================================"
echo ""

# Define variável de ambiente para modo dev
export DEV_MODE=true

# Inicia o backend
echo "🚀 Iniciando backend..."
lein run
