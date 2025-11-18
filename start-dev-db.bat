@echo off
echo.
echo ========================================
echo   INICIANDO AMBIENTE COM BANCO DE DADOS
echo ========================================
echo.
echo Backend: Porta 3000 (PostgreSQL)
echo Frontend: Porta 3001
echo.
echo IMPORTANTE: PostgreSQL deve estar rodando!
echo.
echo Credenciais:
echo   Super Admin: superadmin@example.com
echo   (Configure a senha no banco)
echo.
echo ========================================
echo.

REM Remove modo dev (usa PostgreSQL)
set DEV_MODE=

REM Inicia o backend em uma nova janela
echo [1/2] Iniciando backend...
start "Backend - Porta 3000" cmd /k "lein run"

REM Aguarda 5 segundos
timeout /t 5 /nobreak >nul

REM Inicia o frontend em uma nova janela
echo [2/2] Iniciando frontend...
start "Frontend - Porta 3001" cmd /k "cd frontend-nextjs && npm run dev"

echo.
echo ========================================
echo   SERVICOS INICIADOS!
echo ========================================
echo.
echo Acesse: http://localhost:3001
echo.
echo Para parar, feche as janelas do CMD.
echo.
pause
