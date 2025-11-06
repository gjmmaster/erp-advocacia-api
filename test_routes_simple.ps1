# Teste simples de rotas - verifica se o backend está respondendo

$BACKEND_URL = "https://erp-advocacia-api.onrender.com"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "TESTE SIMPLES: Verificação de Rotas" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Teste 1: Health check (rota raiz)
Write-Host "1. Testando rota raiz..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BACKEND_URL/" -Method Get -ErrorAction Stop
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Mensagem: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# Teste 2: Rota de login (deve retornar 400 ou 401, não 404)
Write-Host "2. Testando rota de login..." -ForegroundColor Yellow
try {
    $body = @{ email = "test@test.com"; password = "test" } | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "$BACKEND_URL/api/auth/login" `
        -Method Post `
        -Body $body `
        -ContentType "application/json" `
        -ErrorAction Stop
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "❌ Rota NÃO EXISTE (404)" -ForegroundColor Red
    } elseif ($statusCode -eq 401 -or $statusCode -eq 400) {
        Write-Host "✅ Rota EXISTE (retornou $statusCode - esperado)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Status inesperado: $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

# Teste 3: Rota de clientes sem autenticação (deve retornar 401, não 404)
Write-Host "3. Testando rota de clientes (sem auth)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BACKEND_URL/api/tenant/clientes" `
        -Method Get `
        -ErrorAction Stop
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "❌ Rota NÃO EXISTE (404)" -ForegroundColor Red
    } elseif ($statusCode -eq 401) {
        Write-Host "✅ Rota EXISTE (retornou 401 - esperado sem token)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Status inesperado: $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

# Teste 4: Rota de processos sem autenticação (deve retornar 401, não 404)
Write-Host "4. Testando rota de processos (sem auth)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BACKEND_URL/api/tenant/processos" `
        -Method Get `
        -ErrorAction Stop
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "❌ Rota NÃO EXISTE (404)" -ForegroundColor Red
    } elseif ($statusCode -eq 401) {
        Write-Host "✅ Rota EXISTE (retornou 401 - esperado sem token)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Status inesperado: $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "TESTE CONCLUÍDO" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
