# Script de teste para API de clientes
# Testa se o backend está respondendo corretamente

$BACKEND_URL = "https://erp-advocacia-api.onrender.com"
# $BACKEND_URL = "http://localhost:8080"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "TESTE: API de Clientes" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Passo 1: Login para obter token (usando auto-discover)
Write-Host "1. Fazendo login (auto-discover)..." -ForegroundColor Yellow
Write-Host "URL: $BACKEND_URL/api/auth/login" -ForegroundColor Gray

# Tentar com diferentes credenciais
$credentials = @(
    @{ email = "jmmaster.dev@gmail.com"; password = "Senha@123" },
    @{ email = "jmmaster.dev@gmail.com"; password = "senha123" },
    @{ email = "jmmaster.dev@gmail.com"; password = "123456" }
)

$loginSuccess = $false
$token = $null

foreach ($cred in $credentials) {
    Write-Host "Tentando com senha: $($cred.password.Substring(0, 3))..." -ForegroundColor Gray
    
    $loginBody = $cred | ConvertTo-Json
    
    try {
        $loginResponse = Invoke-RestMethod -Uri "$BACKEND_URL/api/auth/login" `
            -Method Post `
            -Body $loginBody `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        $token = $loginResponse.token
        $loginSuccess = $true
        Write-Host "✅ Login realizado com sucesso!" -ForegroundColor Green
        Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
        break
    } catch {
        Write-Host "❌ Falhou" -ForegroundColor Red
    }
}

if (-not $loginSuccess) {
    Write-Host ""
    Write-Host "❌ ERRO: Nenhuma credencial funcionou" -ForegroundColor Red
    Write-Host "Por favor, verifique as credenciais no banco de dados" -ForegroundColor Yellow
    exit 1
}



# Passo 2: Listar clientes (GET)
Write-Host "2. Listando clientes..." -ForegroundColor Yellow

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $listResponse = Invoke-RestMethod -Uri "$BACKEND_URL/api/tenant/clientes?page=1&per-page=20" `
        -Method Get `
        -Headers $headers
    
    Write-Host "✅ Listagem realizada com sucesso" -ForegroundColor Green
    Write-Host "Total de clientes: $($listResponse.total)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ ERRO na listagem: $_" -ForegroundColor Red
    Write-Host "StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host ""
}

# Passo 3: Criar novo cliente (POST)
Write-Host "3. Criando novo cliente..." -ForegroundColor Yellow

$clienteBody = @{
    nome = "Cliente Teste API $(Get-Date -Format 'HHmmss')"
    cpf_cnpj = "12345678901"
    email = "teste@email.com"
    telefone = "(11) 98765-4321"
    endereco = "Rua Teste, 123"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$BACKEND_URL/api/tenant/clientes" `
        -Method Post `
        -Headers $headers `
        -Body $clienteBody
    
    Write-Host "✅ Cliente criado com sucesso" -ForegroundColor Green
    Write-Host "ID: $($createResponse.id)" -ForegroundColor Gray
    Write-Host "Nome: $($createResponse.nome)" -ForegroundColor Gray
    $clienteId = $createResponse.id
    Write-Host ""
} catch {
    Write-Host "❌ ERRO na criação: $_" -ForegroundColor Red
    Write-Host "StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    
    # Tentar ler o corpo da resposta de erro
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $responseBody = $reader.ReadToEnd()
    Write-Host "Response body: $responseBody" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# Passo 4: Buscar cliente criado (GET by ID)
Write-Host "4. Buscando cliente criado..." -ForegroundColor Yellow

try {
    $getResponse = Invoke-RestMethod -Uri "$BACKEND_URL/api/tenant/clientes/$clienteId" `
        -Method Get `
        -Headers $headers
    
    Write-Host "✅ Cliente encontrado" -ForegroundColor Green
    Write-Host "Nome: $($getResponse.nome)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ ERRO na busca: $_" -ForegroundColor Red
    Write-Host ""
}

# Passo 5: Atualizar cliente (PUT)
Write-Host "5. Atualizando cliente..." -ForegroundColor Yellow

$updateBody = @{
    nome = "Cliente Teste API ATUALIZADO"
    telefone = "(11) 99999-9999"
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$BACKEND_URL/api/tenant/clientes/$clienteId" `
        -Method Put `
        -Headers $headers `
        -Body $updateBody
    
    Write-Host "✅ Cliente atualizado com sucesso" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ ERRO na atualização: $_" -ForegroundColor Red
    Write-Host ""
}

# Passo 6: Deletar cliente (DELETE)
Write-Host "6. Deletando cliente..." -ForegroundColor Yellow

try {
    Invoke-RestMethod -Uri "$BACKEND_URL/api/tenant/clientes/$clienteId" `
        -Method Delete `
        -Headers $headers
    
    Write-Host "✅ Cliente deletado com sucesso" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ ERRO na deleção: $_" -ForegroundColor Red
    Write-Host ""
}

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "✅ TODOS OS TESTES CONCLUÍDOS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
