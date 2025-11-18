@echo off
echo.
echo ========================================
echo   PARANDO SERVICOS
echo ========================================
echo.

REM Para processos Java (backend Clojure)
echo Parando backend (Java/Leiningen)...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo   Backend parado!
) else (
    echo   Backend nao estava rodando
)

REM Para processos Node (frontend Next.js)
echo Parando frontend (Node.js)...
taskkill /F /IM node.exe 2>nul
if %errorlevel% equ 0 (
    echo   Frontend parado!
) else (
    echo   Frontend nao estava rodando
)

echo.
echo ========================================
echo   SERVICOS PARADOS!
echo ========================================
echo.
pause
