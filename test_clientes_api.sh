#!/bin/bash

# Script de teste para API de clientes
# Testa se o backend está respondendo corretamente

BACKEND_URL="https://erp-advocacia-api.onrender.com"
# BACKEND_URL="http://localhost:8080"

echo "========================================="
echo "TESTE: API de Clientes"
echo "========================================="
echo ""

# Passo 1: Login para obter token
echo "1. Fazendo login..."
LOGIN_RESPONSE=$(curl -s -X POST "${BACKEND_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jmmaster.dev@gmail.com",
    "password": "Senha@123"
  }')

echo "Response do login:"
echo "$LOGIN_RESPONSE" | jq '.'
echo ""

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ ERRO: Não foi possível obter token"
  exit 1
fi

echo "✅ Token obtido: ${TOKEN:0:20}..."
echo ""

# Passo 2: Listar clientes (GET)
echo "2. Listando clientes..."
LIST_RESPONSE=$(curl -s -X GET "${BACKEND_URL}/api/tenant/clientes?page=1&per-page=20" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "Response da listagem:"
echo "$LIST_RESPONSE" | jq '.'
echo ""

# Passo 3: Criar novo cliente (POST)
echo "3. Criando novo cliente..."
CREATE_RESPONSE=$(curl -s -X POST "${BACKEND_URL}/api/tenant/clientes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Cliente Teste API",
    "cpf_cnpj": "12345678901",
    "email": "teste@email.com",
    "telefone": "(11) 98765-4321",
    "endereco": "Rua Teste, 123"
  }')

echo "Response da criação:"
echo "$CREATE_RESPONSE" | jq '.'
echo ""

# Verificar se criou com sucesso
CLIENTE_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id // .clientes_id')

if [ "$CLIENTE_ID" == "null" ] || [ -z "$CLIENTE_ID" ]; then
  echo "❌ ERRO: Cliente não foi criado"
  echo "Response completo:"
  echo "$CREATE_RESPONSE"
  exit 1
fi

echo "✅ Cliente criado com ID: $CLIENTE_ID"
echo ""

# Passo 4: Buscar cliente criado (GET by ID)
echo "4. Buscando cliente criado..."
GET_RESPONSE=$(curl -s -X GET "${BACKEND_URL}/api/tenant/clientes/${CLIENTE_ID}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "Response da busca:"
echo "$GET_RESPONSE" | jq '.'
echo ""

# Passo 5: Atualizar cliente (PUT)
echo "5. Atualizando cliente..."
UPDATE_RESPONSE=$(curl -s -X PUT "${BACKEND_URL}/api/tenant/clientes/${CLIENTE_ID}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Cliente Teste API ATUALIZADO",
    "telefone": "(11) 99999-9999"
  }')

echo "Response da atualização:"
echo "$UPDATE_RESPONSE" | jq '.'
echo ""

# Passo 6: Deletar cliente (DELETE)
echo "6. Deletando cliente..."
DELETE_RESPONSE=$(curl -s -X DELETE "${BACKEND_URL}/api/tenant/clientes/${CLIENTE_ID}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "Response da deleção:"
if [ -z "$DELETE_RESPONSE" ]; then
  echo "✅ Status 204 - Cliente deletado com sucesso"
else
  echo "$DELETE_RESPONSE" | jq '.'
fi
echo ""

echo "========================================="
echo "✅ TODOS OS TESTES CONCLUÍDOS"
echo "========================================="
