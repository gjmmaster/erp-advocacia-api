# ========================================
# Script de Desenvolvimento COMPLETO (Windows)
# ========================================
# Este script inicia BACKEND + FRONTEND simultaneamente
# em modo desenvolvimento
# ========================================

Write-Host "🚀 Iniciando BACKEND + FRONTEND em modo DESENVOLVIMENTO..." -ForegroundColor Green
Write-Host ""

# Carregar variáveis do .env.development
Get-Content .env.development | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        Set-Item -Path "env:$name" -Value $value
    }
}

# Iniciar Backend em nova janela
Write-Host "📦 Iniciando Backend (porta 3000)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; lein run"

# Aguardar backend iniciar
Write-Host "⏳ Aguardando backend iniciar..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Iniciar Frontend em nova janela
Write-Host "🎨 Iniciando Frontend (porta 3001)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\frontend-nextjs'; npm run dev"

Write-Host ""
Write-Host "✅ Servidores iniciados em janelas separadas!" -ForegroundColor Green
Write-Host ""
Write-Host "📍 Backend:  http://localhost:3000" -ForegroundColor White
Write-Host "📍 Frontend: http://localhost:3001" -ForegroundColor White
Write-Host ""
Write-Host "💡 Feche as janelas do PowerShell para encerrar os servidores" -ForegroundColor Yellow
