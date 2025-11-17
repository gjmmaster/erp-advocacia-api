# 📚 Documentação da API - Sistema Jurídico Multi-Tenant

## 🌐 Base URL

**Desenvolvimento:** `http://localhost:3000`
**Produção:** `https://erp-advocacia-api.onrender.com`

## 🔐 Autenticação

A API utiliza **JWT (JSON Web Tokens)** para autenticação. O token deve ser incluído no header `Authorization` de todas as requisições protegidas:

```
Authorization: Bearer {token}
```

### Obter Token

**POST** `/api/auth/login`

```json
{
  "email": "usuario@example.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "usuario@example.com",
    "full_name": "Nome do Usuário",
    "role": "master",
    "tenant_id": 1
  }
}
```

---

## 🏢 Super Admin

### Login Super Admin

**POST** `/admin/login`

```json
{
  "email": "superadmin@example.com",
  "password": "senha-super-admin"
}
```

### Listar Tenants

**GET** `/admin/tenants`

**Headers:** `Authorization: Bearer {token}`

**Resposta:**
```json
{
  "tenants": [
    {
      "id": 1,
      "company_name": "Escritório Silva & Associados",
      "subdomain": "silva",
      "active": true,
      "created_at": "2025-01-01T00:00:00Z"
    }
  ]
}
```

### Criar Tenant

**POST** `/admin/tenants`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "company_name": "Escritório Silva & Associados",
  "subdomain": "silva",
  "master_email": "master@silva.com",
  "master_name": "João Silva"
}
```

**Resposta:**
```json
{
  "tenant": {
    "id": 1,
    "company_name": "Escritório Silva & Associados",
    "subdomain": "silva",
    "active": true
  },
  "master_user": {
    "id": 1,
    "email": "master@silva.com",
    "temporary_password": "abc123xyz"
  }
}
```

### Obter Tenant

**GET** `/admin/tenants/:id`

**Headers:** `Authorization: Bearer {token}`

### Atualizar Tenant

**PUT** `/admin/tenants/:id`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "company_name": "Novo Nome",
  "active": true
}
```

### Deletar Tenant

**DELETE** `/admin/tenants/:id`

**Headers:** `Authorization: Bearer {token}`

### Impersonation

**POST** `/admin/impersonate/:user-id`

**Headers:** `Authorization: Bearer {token}`

**Resposta:**
```json
{
  "token": "novo-token-impersonado",
  "message": "Impersonation iniciada com sucesso"
}
```

**POST** `/admin/stop-impersonate`

**Headers:** `Authorization: Bearer {token}`

---

## 👤 Gestão de Usuários (Tenant)

### Listar Usuários

**GET** `/api/tenant/users`

**Headers:** `Authorization: Bearer {token}`

**Resposta:**
```json
{
  "users": [
    {
      "id": 1,
      "email": "operador@example.com",
      "full_name": "Nome do Operador",
      "role": "operador",
      "active": true,
      "created_at": "2025-01-01T00:00:00Z"
    }
  ]
}
```

### Criar Usuário

**POST** `/api/tenant/users`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "email": "novo@example.com",
  "full_name": "Novo Usuário",
  "role": "operador"
}
```

**Resposta:**
```json
{
  "user": {
    "id": 2,
    "email": "novo@example.com",
    "full_name": "Novo Usuário",
    "role": "operador",
    "active": true
  },
  "temporary_password": "xyz789abc",
  "message": "Usuário criado com sucesso"
}
```

### Obter Usuário

**GET** `/api/tenant/users/:id`

**Headers:** `Authorization: Bearer {token}`

### Atualizar Usuário

**PUT** `/api/tenant/users/:id`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "full_name": "Nome Atualizado",
  "role": "master",
  "active": true
}
```

### Desativar Usuário

**DELETE** `/api/tenant/users/:id`

**Headers:** `Authorization: Bearer {token}`

### Resetar Senha

**POST** `/api/tenant/users/:id/reset-password`

**Headers:** `Authorization: Bearer {token}`

**Resposta:**
```json
{
  "temporary_password": "nova-senha-temp",
  "message": "Senha resetada com sucesso"
}
```

---

## 📁 Processos

### Listar Processos

**GET** `/api/tenant/processos`

**Headers:** `Authorization: Bearer {token}`

**Query Parameters:**
- `page` (opcional): Número da página (padrão: 1)
- `per_page` (opcional): Itens por página (padrão: 20)
- `status` (opcional): Filtrar por status
- `cliente_id` (opcional): Filtrar por cliente

**Resposta:**
```json
{
  "items": [
    {
      "id": 1,
      "numero_processo": "1234567-89.2025.1.00.0000",
      "tipo": "Trabalhista",
      "status": "em_andamento",
      "cliente_id": 1,
      "cliente_nome": "João da Silva",
      "vara": "1ª Vara do Trabalho",
      "data_distribuicao": "2025-01-15",
      "valor_causa": 50000.00,
      "created_at": "2025-01-15T10:00:00Z"
    }
  ],
  "total": 100,
  "page": 1,
  "per_page": 20
}
```

### Criar Processo

**POST** `/api/tenant/processos`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "numero_processo": "1234567-89.2025.1.00.0000",
  "tipo": "Trabalhista",
  "status": "em_andamento",
  "cliente_id": 1,
  "vara": "1ª Vara do Trabalho",
  "comarca": "São Paulo",
  "data_distribuicao": "2025-01-15",
  "valor_causa": 50000.00,
  "descricao": "Ação trabalhista por rescisão indireta"
}
```

### Obter Processo

**GET** `/api/tenant/processos/:id`

**Headers:** `Authorization: Bearer {token}`

### Atualizar Processo

**PUT** `/api/tenant/processos/:id`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "status": "encerrado",
  "descricao": "Descrição atualizada"
}
```

### Deletar Processo

**DELETE** `/api/tenant/processos/:id`

**Headers:** `Authorization: Bearer {token}`

---

## 📄 Documentos

### Listar Documentos do Processo

**GET** `/api/tenant/processos/:processo_id/documentos`

**Headers:** `Authorization: Bearer {token}`

**Resposta:**
```json
{
  "documentos": [
    {
      "id": 1,
      "nome": "Petição Inicial.pdf",
      "tipo": "peticao",
      "url": "https://storage.url/documento.pdf",
      "tamanho": 1024000,
      "created_at": "2025-01-15T10:00:00Z"
    }
  ]
}
```

### Upload de Documento

**POST** `/api/tenant/processos/:processo_id/documentos`

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: multipart/form-data`

**Form Data:**
- `file`: Arquivo (PDF, DOC, DOCX, JPG, PNG)
- `tipo`: Tipo do documento (peticao, contrato, procuracao, etc.)
- `descricao` (opcional): Descrição do documento

**Resposta:**
```json
{
  "documento": {
    "id": 1,
    "nome": "Petição Inicial.pdf",
    "tipo": "peticao",
    "url": "https://storage.url/documento.pdf",
    "tamanho": 1024000
  },
  "message": "Documento enviado com sucesso"
}
```

### Deletar Documento

**DELETE** `/api/tenant/processos/:processo_id/documentos/:documento_id`

**Headers:** `Authorization: Bearer {token}`

---

## 📝 Histórico de Processos

### Obter Histórico

**GET** `/api/tenant/processos/:processo_id/historico`

**Headers:** `Authorization: Bearer {token}`

**Resposta:**
```json
{
  "historico": [
    {
      "id": 1,
      "tipo": "movimentacao",
      "titulo": "Audiência de Conciliação",
      "descricao": "Realizada audiência de conciliação",
      "data_evento": "2025-02-01",
      "created_by": "João Silva",
      "created_at": "2025-02-01T14:00:00Z"
    }
  ]
}
```

### Adicionar Evento ao Histórico

**POST** `/api/tenant/processos/:processo_id/historico`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "tipo": "movimentacao",
  "titulo": "Nova Movimentação",
  "descricao": "Descrição detalhada do evento",
  "data_evento": "2025-02-15"
}
```

---

## 👥 Clientes

### Listar Clientes

**GET** `/api/tenant/clientes`

**Headers:** `Authorization: Bearer {token}`

**Query Parameters:**
- `page` (opcional): Número da página
- `per_page` (opcional): Itens por página
- `search` (opcional): Buscar por nome ou CPF/CNPJ

**Resposta:**
```json
{
  "items": [
    {
      "id": 1,
      "nome": "João da Silva",
      "cpf_cnpj": "123.456.789-00",
      "email": "joao@example.com",
      "telefone": "(11) 98765-4321",
      "tipo": "fisica",
      "created_at": "2025-01-01T00:00:00Z"
    }
  ],
  "total": 50,
  "page": 1,
  "per_page": 20
}
```

### Criar Cliente

**POST** `/api/tenant/clientes`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "nome": "João da Silva",
  "cpf_cnpj": "123.456.789-00",
  "email": "joao@example.com",
  "telefone": "(11) 98765-4321",
  "tipo": "fisica",
  "endereco": "Rua Exemplo, 123"
}
```

### Obter Cliente

**GET** `/api/tenant/clientes/:id`

**Headers:** `Authorization: Bearer {token}`

### Atualizar Cliente

**PUT** `/api/tenant/clientes/:id`

**Headers:** `Authorization: Bearer {token}`

### Deletar Cliente

**DELETE** `/api/tenant/clientes/:id`

**Headers:** `Authorization: Bearer {token}`

---

## 🔒 Senha e Autenticação

### Trocar Senha

**POST** `/api/auth/change-password`

**Headers:** `Authorization: Bearer {token}`

```json
{
  "old_password": "senha-antiga",
  "new_password": "senha-nova-segura"
}
```

### Logout

**POST** `/api/auth/logout`

**Headers:** `Authorization: Bearer {token}`

---

## ❌ Códigos de Erro

| Código | Significado |
|--------|-------------|
| 200 | Sucesso |
| 201 | Criado com sucesso |
| 400 | Requisição inválida |
| 401 | Não autenticado |
| 403 | Sem permissão |
| 404 | Não encontrado |
| 409 | Conflito (ex: email duplicado) |
| 500 | Erro interno do servidor |

---

## 📋 Formatos de Resposta

### Sucesso
```json
{
  "data": { ... },
  "message": "Operação realizada com sucesso"
}
```

### Erro
```json
{
  "error": "Mensagem de erro descritiva",
  "code": "ERROR_CODE"
}
```

---

## 🔐 Segurança

- Todos os endpoints protegidos requerem JWT válido
- Senhas são hash com bcrypt
- Rate limiting: 100 requisições por minuto
- CORS configurado para origens permitidas
- Validação de entrada em todos os endpoints

---

## 📦 Paginação

Endpoints que retornam listas suportam paginação:

**Query Parameters:**
- `page`: Número da página (padrão: 1)
- `per_page`: Itens por página (padrão: 20, máximo: 100)

**Resposta:**
```json
{
  "items": [...],
  "total": 150,
  "page": 1,
  "per_page": 20,
  "total_pages": 8
}
```

---

## 🧪 Testando a API

### cURL

```bash
# Login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"admin123"}'

# Listar processos
curl -X GET http://localhost:3000/api/tenant/processos \
  -H "Authorization: Bearer {seu-token}"
```

### Postman

Importe a collection disponível em `/docs/postman_collection.json`

---

**Versão da API:** 2.0
**Última atualização:** 17 de Novembro de 2025
