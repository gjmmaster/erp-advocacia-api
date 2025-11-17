# Script para iniciar backend (modo mock) e frontend simultaneamente (Windows)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔧 MODO DE DESENVOLVIMENTO COMPLETO" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📦 Backend: Repositório MOCK (in-memory)" -ForegroundColor Yellow
Write-Host "🌐 Frontend: Next.js Dev Server" -ForegroundColor Yellow
Write-Host ""
Write-Host "👤 Credenciais de acesso:" -ForegroundColor Cyan
Write-Host "   Super Admin: admin@demo.com / admin123"
Write-Host "   Master User: master@demo.com / master123"
Write-Host "   Operador 1:  operador1@demo.com / operador123"
Write-Host "   Operador 2:  operador2@demo.com / operador123"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verifica se o backend já está rodando
$backendRunning = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
if ($backendRunning) {
    Write-Host "⚠️  Backend já está rodando na porta 3000" -ForegroundColor Yellow
    Write-Host "   Se quiser reiniciar, pare o processo primeiro"
} else {
    # Define variável de ambiente para modo dev
    $env:DEV_MODE="true"
    
    # Inicia o backend em background
    Write-Host "🚀 Iniciando backend (porta 3000)..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "lein run"
}

# Aguarda um pouco para o backend iniciar
Start-Sleep -Seconds 3

# Verifica se o frontend já está rodando
$frontendRunning = Get-NetTCPConnection -LocalPort 3001 -State Listen -ErrorAction SilentlyContinue
if ($frontendRunning) {
    Write-Host "⚠️  Frontend já está rodando na porta 3001" -ForegroundColor Yellow
} else {
    # Inicia o frontend
    Write-Host ""
    Write-Host "🌐 Iniciando frontend (porta 3001)..." -ForegroundColor Green
    Set-Location frontend-nextjs
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "npm run dev"
    Set-Location ..
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Ambiente de desenvolvimento pronto!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔗 URLs:" -ForegroundColor Cyan
Write-Host "   Backend:  http://localhost:3000"
Write-Host "   Frontend: http://localhost:3001"
Write-Host ""
Write-Host "Os servidores estão rodando em janelas separadas." -ForegroundColor Yellow
Write-Host "Para parar, feche as janelas do PowerShell." -ForegroundColor Yellow
Write-Host ""
