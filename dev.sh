#!/bin/bash

# ========================================
# Script de Desenvolvimento - Backend
# ========================================
# Este script inicia o backend em modo desenvolvimento
# com as configurações do .env.development
# ========================================

echo "🚀 Iniciando Backend em modo DESENVOLVIMENTO..."
echo ""
echo "📍 Carregando configurações de .env.development"
echo ""

# Carregar variáveis do .env.development
export $(cat .env.development | grep -v '^#' | xargs)

# Iniciar o servidor
lein run

echo ""
echo "✅ Backend rodando em http://localhost:3000"
