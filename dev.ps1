# ========================================
# Script de Desenvolvimento - Backend (Windows)
# ========================================
# Este script inicia o backend em modo desenvolvimento
# com as configurações do .env.development
# ========================================

Write-Host "🚀 Iniciando Backend em modo DESENVOLVIMENTO..." -ForegroundColor Green
Write-Host ""
Write-Host "📍 Carregando configurações de .env.development" -ForegroundColor Cyan
Write-Host ""

# Carregar variáveis do .env.development
Get-Content .env.development | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        Set-Item -Path "env:$name" -Value $value
        Write-Host "  ✓ $name configurado" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "🔧 Iniciando servidor Clojure..." -ForegroundColor Yellow
Write-Host ""

# Iniciar o servidor
lein run

Write-Host ""
Write-Host "✅ Backend rodando em http://localhost:3000" -ForegroundColor Green
