# Guia de Testes - Autenticação de Tenants

**Data:** 24 de Outubro de 2025

---

## Task 1: Endpoint de busca de tenant por subdomínio

### ✅ Implementação Completa

**Arquivos modificados:**
- `src/juridico/api/handlers.clj` - Adicionado `get-tenant-by-subdomain-handler`
- `src/juridico/api/core.clj` - Adicionada rota `GET /api/tenants/by-subdomain/:subdomain`

### Como Testar

#### 1. Iniciar o backend

```bash
cd /path/to/backend
lein run
```

#### 2. Criar um tenant de teste (se ainda não existe)

```bash
# Fazer login como super admin primeiro
$response = Invoke-WebRequest -Uri "http://localhost:3000/admin/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"super@admin.com","password":"sua-senha"}'

$token = ($response.Content | ConvertFrom-Json).token

# Criar tenant
Invoke-WebRequest -Uri "http://localhost:3000/admin/provision-tenant" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{Authorization="Bearer $token"} `
  -Body '{"company_name":"Escritório Silva","email":"admin@silva.com","operator_limit":5}'
```

#### 3. Testar o novo endpoint

**Teste 1: Buscar tenant existente**

```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/tenants/by-subdomain/escritorio-silva" `
  -Method GET | Select-Object -ExpandProperty Content
```

**Resposta esperada:**
```json
{
  "id": "1234567890",
  "name": "Escritório Silva",
  "subdomain": "escritorio-silva",
  "active": true
}
```

**Teste 2: Buscar tenant inexistente**

```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/tenants/by-subdomain/nao-existe" `
  -Method GET
```

**Resposta esperada:**
```json
{
  "error": "Tenant não encontrado"
}
```
Status: 404

**Teste 3: Validar formato da resposta**

```powershell
$response = Invoke-WebRequest -Uri "http://localhost:3000/api/tenants/by-subdomain/escritorio-silva" -Method GET
$tenant = $response.Content | ConvertFrom-Json

# Verificar campos
Write-Host "ID: $($tenant.id)"
Write-Host "Name: $($tenant.name)"
Write-Host "Subdomain: $($tenant.subdomain)"
Write-Host "Active: $($tenant.active)"
```

### Checklist de Validação

- [ ] Endpoint retorna 200 para tenant existente
- [ ] Endpoint retorna 404 para tenant inexistente
- [ ] Resposta contém campos: id, name, subdomain, active
- [ ] Campo `id` é string (para evitar perda de precisão)
- [ ] Campo `active` é sempre true (tenants inativos não são retornados)
- [ ] Endpoint é público (não requer autenticação)

---

## Task 2: Atualizar endpoint de login para validar tenant

### ✅ Implementação Completa

**Arquivos modificados:**
- `src/juridico/api/handlers.clj` - Atualizado `login-handler` para aceitar `subdomain`
- `src/juridico/api/specs.clj` - Atualizado `::login-payload` para incluir `subdomain`

### Mudanças Principais

1. **Login agora aceita `subdomain` no body**
2. **Valida que tenant existe antes de autenticar**
3. **Valida que usuário pertence ao tenant correto**
4. **Retorna erros específicos:**
   - 404: Escritório não encontrado
   - 403: Usuário não pertence ao escritório
   - 401: Credenciais inválidas

### Como Testar

#### 1. Criar usuário de teste (se ainda não existe)

```powershell
# Login como super admin
$response = Invoke-WebRequest -Uri "http://localhost:3000/admin/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"super@admin.com","password":"sua-senha"}'

$token = ($response.Content | ConvertFrom-Json).token

# Provisionar tenant com usuário
Invoke-WebRequest -Uri "http://localhost:3000/admin/provision-tenant" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{Authorization="Bearer $token"} `
  -Body '{"company_name":"Escritório Silva","email":"admin@silva.com","operator_limit":5}'

# A senha temporária será enviada por email ou exibida na resposta
```

#### 2. Testar login com subdomain

**Teste 1: Login com credenciais válidas**

```powershell
Invoke-WebRequest -Uri "http://localhost:3000/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@silva.com","password":"senha-temporaria","subdomain":"escritorio-silva"}'
```

**Resposta esperada (200):**
```json
{
  "message": "Usuário admin@silva.com autenticado com sucesso.",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Teste 2: Login com subdomain inexistente**

```powershell
Invoke-WebRequest -Uri "http://localhost:3000/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@silva.com","password":"senha","subdomain":"nao-existe"}'
```

**Resposta esperada (404):**
```json
{
  "error": "Escritório não encontrado."
}
```

**Teste 3: Login com usuário de outro tenant**

```powershell
# Criar outro tenant
Invoke-WebRequest -Uri "http://localhost:3000/admin/provision-tenant" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{Authorization="Bearer $token"} `
  -Body '{"company_name":"Escritório Santos","email":"admin@santos.com","operator_limit":5}'

# Tentar login com usuário do Silva no tenant Santos
Invoke-WebRequest -Uri "http://localhost:3000/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@silva.com","password":"senha","subdomain":"escritorio-santos"}'
```

**Resposta esperada (401):**
```json
{
  "error": "Credenciais inválidas."
}
```

**Teste 4: Login com senha incorreta**

```powershell
Invoke-WebRequest -Uri "http://localhost:3000/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@silva.com","password":"senha-errada","subdomain":"escritorio-silva"}'
```

**Resposta esperada (401):**
```json
{
  "error": "Credenciais inválidas."
}
```

**Teste 5: Validar token JWT**

```powershell
$response = Invoke-WebRequest -Uri "http://localhost:3000/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@silva.com","password":"senha-correta","subdomain":"escritorio-silva"}'

$token = ($response.Content | ConvertFrom-Json).token

# Decodificar token (base64)
$parts = $token.Split('.')
$payload = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($parts[1]))
Write-Host "Token payload: $payload"

# Verificar campos: user-id, email, tenant-id, role, exp
```

### Checklist de Validação

- [ ] Login com credenciais válidas retorna 200 e token
- [ ] Token JWT contém: user-id, email, tenant-id, role, exp
- [ ] Login com subdomain inexistente retorna 404
- [ ] Login com usuário de outro tenant retorna 401
- [ ] Login com senha incorreta retorna 401
- [ ] Token expira em 15 minutos (900 segundos)
- [ ] Campo `subdomain` é obrigatório no body

---

## Task 3: Endpoint de estatísticas do dashboard

### ✅ Implementação Completa

**Arquivos modificados:**
- `src/juridico/api/db/protocols.clj` - Adicionadas 4 funções de contagem
- `src/juridico/api/db/postgres.clj` - Implementadas funções de contagem
- `src/juridico/api/handlers.clj` - Adicionado `get-dashboard-stats-handler`
- `src/juridico/api/core.clj` - Adicionada rota `GET /api/dashboard/stats/:tenant-id`

### Funções Implementadas

1. **`count-processos`** - Total de processos do tenant
2. **`count-clientes`** - Total de clientes (retorna 0 por enquanto)
3. **`count-operadores`** - Total de operadores do tenant
4. **`count-processos-ativos`** - Total de processos com status 'ativo'

### Como Testar

#### 1. Fazer login e obter token

```powershell
$response = Invoke-WebRequest -Uri "http://localhost:3000/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@silva.com","password":"senha","subdomain":"escritorio-silva"}'

$data = $response.Content | ConvertFrom-Json
$token = $data.token

# Extrair tenant-id do token
$parts = $token.Split('.')
$payload = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($parts[1]))
$payloadObj = $payload | ConvertFrom-Json
$tenantId = $payloadObj.'tenant-id'

Write-Host "Tenant ID: $tenantId"
```

#### 2. Buscar estatísticas

**Teste 1: Buscar stats com autenticação válida**

```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/dashboard/stats/$tenantId" `
  -Method GET `
  -Headers @{Authorization="Bearer $token"} | Select-Object -ExpandProperty Content
```

**Resposta esperada (200):**
```json
{
  "total-processos": 5,
  "total-clientes": 0,
  "total-operadores": 2,
  "processos-ativos": 3
}
```

**Teste 2: Buscar stats sem autenticação**

```powershell
Invoke-WebRequest -Uri "http://localhost:3000/api/dashboard/stats/$tenantId" `
  -Method GET
```

**Resposta esperada (401):**
```json
{
  "error": "Não autenticado"
}
```

**Teste 3: Buscar stats de outro tenant**

```powershell
# Usar token do tenant A para acessar stats do tenant B
$outroTenantId = "9999999999"

Invoke-WebRequest -Uri "http://localhost:3000/api/dashboard/stats/$outroTenantId" `
  -Method GET `
  -Headers @{Authorization="Bearer $token"}
```

**Resposta esperada (403):**
```json
{
  "error": "Acesso negado"
}
```

**Teste 4: Criar dados de teste e verificar contagem**

```powershell
# Criar um processo
Invoke-WebRequest -Uri "http://localhost:3000/api/processos" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{Authorization="Bearer $token"} `
  -Body '{"case_number":"12345","jurisdiction":"Vara Cível","status":"ativo"}'

# Criar um operador
Invoke-WebRequest -Uri "http://localhost:3000/api/operadores" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{Authorization="Bearer $token"} `
  -Body '{"email":"operador@silva.com","password":"senha123","full_name":"João Silva"}'

# Buscar stats novamente
Invoke-WebRequest -Uri "http://localhost:3000/api/dashboard/stats/$tenantId" `
  -Method GET `
  -Headers @{Authorization="Bearer $token"} | Select-Object -ExpandProperty Content

# Deve mostrar contadores incrementados
```

### Checklist de Validação

- [ ] Endpoint retorna 200 com stats válidas
- [ ] Endpoint retorna 401 sem autenticação
- [ ] Endpoint retorna 403 ao tentar acessar stats de outro tenant
- [ ] `total-processos` conta corretamente
- [ ] `total-operadores` conta apenas role='operador'
- [ ] `processos-ativos` conta apenas status='ativo'
- [ ] `total-clientes` retorna 0 (tabela não existe ainda)
- [ ] Validação de tenant-id funciona corretamente

---

## Resumo das 3 Primeiras Tasks (Backend)

### ✅ Task 1: Endpoint de busca de tenant
- Rota: `GET /api/tenants/by-subdomain/:subdomain`
- Público (sem autenticação)
- Retorna dados do tenant

### ✅ Task 2: Login com validação de tenant
- Rota: `POST /auth/login`
- Aceita `subdomain` no body
- Valida tenant antes de autenticar
- Retorna JWT com tenant-id

### ✅ Task 3: Estatísticas do dashboard
- Rota: `GET /api/dashboard/stats/:tenant-id`
- Requer autenticação
- Valida que usuário pertence ao tenant
- Retorna 4 métricas

---

## Próximas Tasks

Após validar as 3 tasks do backend, prossiga para:
- **Task 4:** Frontend - Atualizar middleware para extrair subdomínio
- **Task 5:** Frontend - Criar tipos TypeScript

