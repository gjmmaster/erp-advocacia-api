# Guia Rápido de Testes - Melhorias de Segurança

## Teste 1: Validação Fail-Fast de JWT_SECRET ⚡

### Cenário 1: Produção sem JWT_SECRET (deve falhar)
```bash
# Windows CMD
set APP_ENV=production
set JWT_SECRET=
lein run
```

**Resultado Esperado:**
```
Exception in thread "main" java.lang.Exception: 
ERRO CRÍTICO: A variável de ambiente JWT_SECRET não foi definida.
```

### Cenário 2: Desenvolvimento sem JWT_SECRET (deve funcionar com aviso)
```bash
# Windows CMD
set APP_ENV=development
set JWT_SECRET=
lein run
```

**Resultado Esperado:**
```
WARN - ⚠️  Usando chave JWT padrão. NÃO USE EM PRODUÇÃO!
INFO - Server started on port 3000
```

### Cenário 3: Com JWT_SECRET configurado (deve funcionar)
```bash
# Windows CMD
set JWT_SECRET=minha-chave-secreta-de-teste-123
set APP_ENV=production
lein run
```

**Resultado Esperado:**
```
INFO - ✓ JWT_SECRET carregado com sucesso
INFO - Server started on port 3000
```

---

## Teste 2: Geração Segura de Senhas Temporárias 🔐

### Provisionar um novo tenant
```bash
# 1. Fazer login como super-admin
curl -X POST http://localhost:3000/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"superadmin@example.com\",\"password\":\"sua-senha\"}"

# Copiar o token retornado

# 2. Provisionar novo tenant
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SEU_TOKEN_AQUI" ^
  -d "{\"company_name\":\"Escritorio Teste\",\"email\":\"admin@teste.com\"}"
```

**Resultado Esperado:**
```json
{
  "tenant": {
    "id": "123",
    "subdomain": "escritorio-teste",
    "company_name": "Escritorio Teste"
  },
  "user": {
    "email": "admin@teste.com",
    "temp_password": "aB3dE5gH9jK2"  ← 12 caracteres alfanuméricos
  }
}
```

### Validar a senha temporária
- ✅ Deve ter exatamente 12 caracteres
- ✅ Deve conter apenas letras (a-z, A-Z) e números (0-9)
- ✅ Deve ser diferente a cada provisionamento

---

## Teste 3: Validação de Subdomínio RFC 1035 ✅

### Cenário 1: Subdomínios válidos (devem ser aceitos)
```bash
# Teste 1: Letras minúsculas
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Authorization: Bearer SEU_TOKEN" ^
  -d "{\"company_name\":\"Legal\",\"subdomain\":\"escritorio\",\"email\":\"admin@legal.com\"}"

# Teste 2: Com hífen no meio
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Authorization: Bearer SEU_TOKEN" ^
  -d "{\"company_name\":\"Legal\",\"subdomain\":\"escritorio-legal\",\"email\":\"admin@legal.com\"}"

# Teste 3: Alfanumérico
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Authorization: Bearer SEU_TOKEN" ^
  -d "{\"company_name\":\"Legal\",\"subdomain\":\"adv2025\",\"email\":\"admin@legal.com\"}"
```

**Resultado Esperado:** HTTP 201 Created

### Cenário 2: Subdomínios inválidos (devem ser rejeitados)
```bash
# Teste 1: Começa com hífen
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Authorization: Bearer SEU_TOKEN" ^
  -d "{\"company_name\":\"Legal\",\"subdomain\":\"-escritorio\",\"email\":\"admin@legal.com\"}"

# Teste 2: Termina com hífen
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Authorization: Bearer SEU_TOKEN" ^
  -d "{\"company_name\":\"Legal\",\"subdomain\":\"escritorio-\",\"email\":\"admin@legal.com\"}"

# Teste 3: Maiúsculas
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Authorization: Bearer SEU_TOKEN" ^
  -d "{\"company_name\":\"Legal\",\"subdomain\":\"ESCRITORIO\",\"email\":\"admin@legal.com\"}"

# Teste 4: Underscore
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Authorization: Bearer SEU_TOKEN" ^
  -d "{\"company_name\":\"Legal\",\"subdomain\":\"escritorio_legal\",\"email\":\"admin@legal.com\"}"
```

**Resultado Esperado:** HTTP 400 Bad Request
```json
{
  "error": "Payload inválido",
  "details": {
    "problems": [{
      "path": ["subdomain"],
      "pred": "(re-matches #\"[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\" %)",
      "val": "-escritorio"
    }]
  }
}
```

---

## Teste 4: Compatibilidade com Versões Anteriores 🔄

### Teste 1: Senhas antigas ainda funcionam
```bash
# 1. Fazer login com usuário criado antes das mudanças
curl -X POST http://localhost:3000/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"usuario-antigo@tenant.com\",\"password\":\"senha-antiga\"}"
```

**Resultado Esperado:** Login bem-sucedido, token JWT retornado

### Teste 2: Tokens JWT antigos ainda são válidos
```bash
# Usar um token JWT gerado antes das mudanças
curl -X GET http://localhost:3000/processos ^
  -H "Authorization: Bearer TOKEN_ANTIGO_AQUI"
```

**Resultado Esperado:** Requisição bem-sucedida

### Teste 3: Subdomínios existentes continuam funcionando
```bash
# Acessar tenant existente
curl -X POST http://tenant-antigo.localhost:3000/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@tenant-antigo.com\",\"password\":\"senha\"}"
```

**Resultado Esperado:** Login bem-sucedido

---

## Teste 5: Teste Completo End-to-End 🎯

### Fluxo completo de provisionamento e login

```bash
# 1. Login como super-admin
curl -X POST http://localhost:3000/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"superadmin@example.com\",\"password\":\"sua-senha\"}"

# Copiar token: SUPER_ADMIN_TOKEN

# 2. Provisionar novo tenant
curl -X POST http://localhost:3000/admin/tenants ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer SUPER_ADMIN_TOKEN" ^
  -d "{\"company_name\":\"Escritorio Novo\",\"email\":\"admin@novo.com\"}"

# Copiar temp_password: TEMP_PASSWORD
# Copiar subdomain: SUBDOMAIN

# 3. Login com senha temporária
curl -X POST http://SUBDOMAIN.localhost:3000/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@novo.com\",\"password\":\"TEMP_PASSWORD\"}"

# Copiar token: TENANT_TOKEN

# 4. Criar um processo
curl -X POST http://SUBDOMAIN.localhost:3000/processos ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer TENANT_TOKEN" ^
  -d "{\"case_number\":\"12345\",\"jurisdiction\":\"Federal\"}"
```

**Resultado Esperado:** Todos os passos bem-sucedidos

---

## Checklist de Validação ✓

Após executar os testes, verifique:

### Segurança
- [ ] Aplicação não inicia em produção sem JWT_SECRET
- [ ] Senhas temporárias têm 12 caracteres alfanuméricos
- [ ] Senhas temporárias são diferentes a cada provisionamento
- [ ] Subdomínios inválidos são rejeitados com erro 400
- [ ] Subdomínios válidos são aceitos

### Compatibilidade
- [ ] Senhas antigas continuam funcionando
- [ ] Tokens JWT antigos são válidos
- [ ] Tenants existentes funcionam normalmente
- [ ] Subdomínios existentes não são afetados

### Logs
- [ ] Log mostra "✓ JWT_SECRET carregado com sucesso" quando configurado
- [ ] Log mostra aviso quando usa chave padrão em desenvolvimento
- [ ] Logs não mostram senhas em texto plano
- [ ] Logs não mostram o valor do JWT_SECRET

---

## Troubleshooting Rápido 🔧

### Problema: "ERRO CRÍTICO: A variável de ambiente JWT_SECRET não foi definida"
**Solução:**
```bash
set JWT_SECRET=minha-chave-secreta
lein run
```

### Problema: Senha temporária não tem 12 caracteres
**Solução:** Verificar que buddy-core está instalado:
```bash
lein deps :tree | findstr buddy-core
```

### Problema: Subdomínio válido sendo rejeitado
**Solução:** Verificar formato (apenas minúsculas, números e hífen no meio):
```
✅ escritorio-legal
❌ Escritorio-Legal (maiúsculas)
❌ -escritorio (começa com hífen)
❌ escritorio_ (underscore)
```

---

## Comandos Úteis 🛠️

### Verificar variáveis de ambiente
```bash
# Windows CMD
set | findstr /I "JWT_SECRET APP_ENV DATABASE_URL"

# Windows PowerShell
Get-ChildItem Env: | Where-Object { $_.Name -match "JWT_SECRET|APP_ENV|DATABASE_URL" }
```

### Gerar JWT_SECRET seguro
```bash
# Windows PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

### Ver logs em tempo real
```bash
# Se estiver usando arquivo de log
Get-Content logs\application.log -Wait -Tail 50
```

---

**✅ Todos os testes passando = Implementação bem-sucedida!**
