@echo off
echo.
echo ========================================
echo   INICIANDO AMBIENTE DE DESENVOLVIMENTO
echo ========================================
echo.

REM Verifica se Node.js esta instalado
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Node.js nao encontrado!
    echo.
    echo Por favor, instale o Node.js:
    echo   1. Acesse: https://nodejs.org/
    echo   2. Baixe a versao LTS
    echo   3. Instale marcando "Add to PATH"
    echo   4. Reinicie o terminal
    echo.
    echo Leia: INSTALAR_NODE.md para mais detalhes
    echo.
    pause
    exit /b 1
)

REM Verifica se as dependencias do frontend estao instaladas
if not exist "frontend-nextjs\node_modules" (
    echo.
    echo [AVISO] Dependencias do frontend nao instaladas!
    echo Instalando agora...
    echo.
    cd frontend-nextjs
    call npm install
    cd ..
    echo.
)

echo Backend: Porta 3000 (Modo Mock)
echo Frontend: Porta 3001
echo.
echo Credenciais:
echo   Master: master@demo.com / master123
echo   Operador: operador1@demo.com / operador123
echo.
echo ========================================
echo.

REM Define modo dev
set DEV_MODE=true

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
