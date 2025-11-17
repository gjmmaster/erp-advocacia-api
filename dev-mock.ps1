# Script para iniciar o backend em modo de desenvolvimento com repositório mock (Windows)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔧 MODO DE DESENVOLVIMENTO (MOCK)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📦 Usando repositório MOCK (in-memory)" -ForegroundColor Yellow
Write-Host "⚡ Dados não persistem entre reinícios" -ForegroundColor Yellow
Write-Host "🚀 Ideal para desenvolvimento rápido" -ForegroundColor Yellow
Write-Host ""
Write-Host "👤 Credenciais de acesso:" -ForegroundColor Cyan
Write-Host "   Super Admin: admin@demo.com / admin123"
Write-Host "   Master User: master@demo.com / master123"
Write-Host "   Operador 1:  operador1@demo.com / operador123"
Write-Host "   Operador 2:  operador2@demo.com / operador123"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Define variável de ambiente para modo dev
$env:DEV_MODE="true"

# Inicia o backend
Write-Host "🚀 Iniciando backend..." -ForegroundColor Green
lein run
