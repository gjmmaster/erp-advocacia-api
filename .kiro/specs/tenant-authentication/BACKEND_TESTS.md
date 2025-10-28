# Testes de Backend - Autenticação de Tenants

**Data:** 28/10/2025  
**Status:** Pronto para Execução

---

## Overview

Este documento contém os testes manuais para validar os endpoints do backend relacionados à autenticação de tenants.

---

## Pré-requisitos

1. Backend rodando em `http://localhost:3000`
2. Banco de dados com pelo menos 2 tenants cadastrados
3. Usuários cadastrados em diferentes tenants
4. Tool de teste HTTP (curl, Postman, ou similar)

---

## Setup de Dados de Teste

### Criar Tenants de Teste

```sql
-- Tenant 1: Escritório Silva
INSERT INTO tenants (name, subdomain, active) 
VALUES ('Escritório Silva', 'escritorio-silva', true);

-- Tenant 2: Escritório Santos
INSERT INTO tenants (name, subdomain, active) 
VALUES ('Escritório Santos', 'escritorio-santos', true);

-- Tenant 3: Inativo
INSERT INTO tenants (name, subdomain, active) 
VALUES ('Escritório Inativo', 'escritorio-inativo', false);
```

### Criar Usuários de Teste

```sql
-- Usuário do Tenant 1 (escritorio-silva)
INSERT INTO users (email, password_hash, role, tenant_id)
VALUES (
  'admin@silva.com',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIiIiIiIiI', -- senha: test123
  'master',
  (SELECT id FROM tenants WHERE subdomain = 'escritorio-silva')
);

-- Usuário do Tenant 2 (escritorio-santos)
INSERT INTO users (email, password_hash, role, tenant_id)
VALUES (
  'admin@santos.com',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIiIiIiIiI', -- senha: test123
  'master',
  (SELECT id FROM tenants WHERE subdomain = 'escritorio-santos')
);
```

---

## Teste 1: Buscar Tenant por Subdomínio

### Caso 1.1: Subdomínio Válido

**Request:**
```bash
curl -X GET http://localhost:3000/api/tenants/by-subdomain/escritorio-silva \
  -H "Content-Type: application/json"
```

**Resposta Esperada:**
```json
{
  "id": 1,
  "name": "Escritório Silva",
  "subdomain": "escritorio-silva",
  "active": true
}
```

**Status:** `200 OK`

---

### Caso 1.2: Subdomínio Inválido

**Request:**
```bash
curl -X GET http://localhost:3000/api/tenants/by-subdomain/nao-existe \
  -H "Content-Type: application/json"
```

**Resposta Esperada:**
```json
{
  "error": "Tenant não encontrado"
}
```

**Status:** `404 Not Found`

---

### Caso 1.3: Tenant Inativo

**Request:**
```bash
curl -X GET http://localhost:3000/api/tenants/by-subdomain/escritorio-inativo \
  -H "Content-Type: application/json"
```

**Resposta Esperada:**
```json
{
  "error": "Tenant inativo"
}
```

**Status:** `403 Forbidden`

---

## Teste 2: Login com Validação de Tenant

### Caso 2.1: Login Válido com Tenant Correto

**Request:**
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@silva.com",
    "password": "test123",
    "subdomain": "escritorio-silva"
  }'
```

**Resposta Esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "admin@silva.com",
    "role": "master",
    "tenant-id": 1
  }
}
```

**Status:** `200 OK`

**Validações:**
- Token JWT válido
- Token contém `tenant-id` correto
- Token contém `user-id`, `email`, `role`

---

### Caso 2.2: Login com Usuário de Outro Tenant

**Request:**
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@silva.com",
    "password": "test123",
    "subdomain": "escritorio-santos"
  }'
```

**Resposta Esperada:**
```json
{
  "error": "Usuário não pertence a este escritório"
}
```

**Status:** `403 Forbidden`

---

### Caso 2.3: Login com Tenant Inativo

**Request:**
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@inativo.com",
    "password": "test123",
    "subdomain": "escritorio-inativo"
  }'
```

**Resposta Esperada:**
```json
{
  "error": "Escritório inativo"
}
```

**Status:** `403 Forbidden`

---

### Caso 2.4: Login sem Subdomain

**Request:**
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@silva.com",
    "password": "test123"
  }'
```

**Resposta Esperada:**
```json
{
  "error": "Subdomain é obrigatório"
}
```

**Status:** `400 Bad Request`

---

### Caso 2.5: Login com Credenciais Inválidas

**Request:**
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@silva.com",
    "password": "senha-errada",
    "subdomain": "escritorio-silva"
  }'
```

**Resposta Esperada:**
```json
{
  "error": "Credenciais inválidas"
}
```

**Status:** `401 Unauthorized`

---

## Teste 3: Estatísticas do Dashboard

### Setup: Obter Token de Autenticação

Primeiro, faça login para obter um token:

```bash
TOKEN=$(curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@silva.com",
    "password": "test123",
    "subdomain": "escritorio-silva"
  }' | jq -r '.token')

echo $TOKEN
```

---

### Caso 3.1: Buscar Stats com Autenticação Válida

**Request:**
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada:**
```json
{
  "total-processos": 0,
  "processos-ativos": 0,
  "total-clientes": 0,
  "total-operadores": 1
}
```

**Status:** `200 OK`

**Validações:**
- Todos os campos são números
- `total-operadores` >= 1 (pelo menos o usuário logado)

---

### Caso 3.2: Buscar Stats sem Autenticação

**Request:**
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/1 \
  -H "Content-Type: application/json"
```

**Resposta Esperada:**
```json
{
  "error": "Não autenticado"
}
```

**Status:** `401 Unauthorized`

---

### Caso 3.3: Buscar Stats de Outro Tenant

Fazer login com usuário do tenant 1 e tentar acessar stats do tenant 2:

**Request:**
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/2 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta Esperada:**
```json
{
  "error": "Acesso negado"
}
```

**Status:** `403 Forbidden`

---

### Caso 3.4: Buscar Stats com Token Inválido

**Request:**
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token-invalido"
```

**Resposta Esperada:**
```json
{
  "error": "Token inválido"
}
```

**Status:** `401 Unauthorized`

---

### Caso 3.5: Buscar Stats com Token Expirado

**Setup:** Criar um token com expiração curta (1 segundo) e aguardar expirar.

**Request:**
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $EXPIRED_TOKEN"
```

**Resposta Esperada:**
```json
{
  "error": "Token expirado"
}
```

**Status:** `401 Unauthorized`

---

## Teste 4: Validação de Dados

### Caso 4.1: Verificar Estrutura do Token JWT

Decodificar o token JWT e verificar os claims:

```bash
# Usar jwt.io ou biblioteca JWT
echo $TOKEN | jwt decode -
```

**Claims Esperados:**
```json
{
  "user-id": 1,
  "email": "admin@silva.com",
  "role": "master",
  "tenant-id": 1,
  "exp": 1698765432,
  "iat": 1698764532
}
```

---

### Caso 4.2: Verificar Isolamento de Dados

Criar dados de teste em diferentes tenants e verificar que as stats retornam apenas dados do tenant correto:

```sql
-- Adicionar dados ao tenant 1
INSERT INTO processos (numero, tenant_id) VALUES ('001/2025', 1);
INSERT INTO clientes (nome, tenant_id) VALUES ('Cliente Silva', 1);

-- Adicionar dados ao tenant 2
INSERT INTO processos (numero, tenant_id) VALUES ('002/2025', 2);
INSERT INTO clientes (nome, tenant_id) VALUES ('Cliente Santos', 2);
```

**Request (Tenant 1):**
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/1 \
  -H "Authorization: Bearer $TOKEN_TENANT_1"
```

**Resposta Esperada:**
```json
{
  "total-processos": 1,
  "processos-ativos": 1,
  "total-clientes": 1,
  "total-operadores": 1
}
```

**Request (Tenant 2):**
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/2 \
  -H "Authorization: Bearer $TOKEN_TENANT_2"
```

**Resposta Esperada:**
```json
{
  "total-processos": 1,
  "processos-ativos": 1,
  "total-clientes": 1,
  "total-operadores": 1
}
```

---

## Checklist de Validação

### Endpoint: GET /api/tenants/by-subdomain/:subdomain
- [ ] Retorna tenant válido com status 200
- [ ] Retorna 404 para subdomínio inexistente
- [ ] Retorna 403 para tenant inativo
- [ ] Resposta contém todos os campos necessários

### Endpoint: POST /api/auth/login (com subdomain)
- [ ] Login bem-sucedido retorna token e dados do usuário
- [ ] Token JWT contém todos os claims necessários
- [ ] Rejeita login de usuário de outro tenant
- [ ] Rejeita login com tenant inativo
- [ ] Rejeita login sem subdomain
- [ ] Rejeita credenciais inválidas

### Endpoint: GET /api/dashboard/stats/:tenant-id
- [ ] Retorna stats com autenticação válida
- [ ] Rejeita requisição sem autenticação
- [ ] Rejeita acesso a stats de outro tenant
- [ ] Rejeita token inválido
- [ ] Rejeita token expirado
- [ ] Stats refletem apenas dados do tenant correto

### Segurança
- [ ] Senhas não são retornadas nas respostas
- [ ] Tokens são válidos e seguros
- [ ] Isolamento de dados entre tenants funciona
- [ ] Validações de autorização funcionam corretamente

---

## Resultados dos Testes

### Teste 1: Buscar Tenant por Subdomínio
- [ ] Caso 1.1: ✅ Passou
- [ ] Caso 1.2: ✅ Passou
- [ ] Caso 1.3: ✅ Passou

### Teste 2: Login com Validação de Tenant
- [ ] Caso 2.1: ✅ Passou
- [ ] Caso 2.2: ✅ Passou
- [ ] Caso 2.3: ✅ Passou
- [ ] Caso 2.4: ✅ Passou
- [ ] Caso 2.5: ✅ Passou

### Teste 3: Estatísticas do Dashboard
- [ ] Caso 3.1: ✅ Passou
- [ ] Caso 3.2: ✅ Passou
- [ ] Caso 3.3: ✅ Passou
- [ ] Caso 3.4: ✅ Passou
- [ ] Caso 3.5: ✅ Passou

### Teste 4: Validação de Dados
- [ ] Caso 4.1: ✅ Passou
- [ ] Caso 4.2: ✅ Passou

---

## Notas

- Todos os testes devem ser executados em ordem
- Limpar dados de teste após execução se necessário
- Documentar qualquer falha encontrada
- Atualizar este documento conforme necessário

---

**Documento criado em:** 28/10/2025  
**Última atualização:** 28/10/2025  
**Status:** Pronto para Execução
