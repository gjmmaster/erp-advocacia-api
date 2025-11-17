# Comandos para Deploy - Copie e Cole

**Data:** 04/11/2025

---

## 1️⃣ Commit e Push

```bash
# Adicionar todos os arquivos
git add .

# Fazer commit
git commit -m "feat: complete backend for processo management (tasks 4-11)

- Implement 4 repositories with 25 functions (ProcessoRepository, DocumentoRepository, HistoricoRepository, ClienteRepository)
- Create 16 handlers for processos, clientes, documentos, and historico
- Add 15 REST endpoints with JWT authentication
- Add full CRUD for processos and clientes
- Implement soft delete, audit trail, and multi-tenancy
- Add pagination, filters, and full-text search
- Tasks 4-11 complete (39% of total project)"

# Push para GitHub
git push origin main
```

---

## 2️⃣ Aguardar Deploy

O Render vai fazer deploy automático. Aguarde ~5 minutos.

Você pode acompanhar em: https://dashboard.render.com

---

## 3️⃣ Aplicar Migrations

### Opção A: Via Console Web (RECOMENDADO)

1. Acesse: https://cockroachlabs.cloud/
2. Faça login
3. Selecione seu cluster
4. Clique em "SQL Shell" ou "SQL"
5. Execute os comandos abaixo:

**Migration 004 - Clientes:**
```sql
-- Copie TODO o conteúdo do arquivo migrations/004_create_clientes_table.sql
-- Cole aqui e execute
```

**Migration 005 - Processos:**
```sql
-- Copie TODO o conteúdo do arquivo migrations/005_create_processos_tables.sql
-- Cole aqui e execute
```

### Opção B: Via psql (Se tiver instalado)

```bash
# Definir DATABASE_URL (substitua pelos seus dados)
export DATABASE_URL='postgresql://usuario:senha@host:26257/defaultdb?sslmode=require'

# Aplicar migration 004
psql "$DATABASE_URL" -f migrations/004_create_clientes_table.sql

# Aplicar migration 005
psql "$DATABASE_URL" -f migrations/005_create_processos_tables.sql
```

### Opção C: Via Scripts (Windows/Mac/Linux)

**Windows (PowerShell):**
```powershell
# Definir DATABASE_URL
$env:DATABASE_URL="postgresql://usuario:senha@host:26257/defaultdb?sslmode=require"

# Executar scripts
bash run_migration_clientes.sh
bash run_migration_processos.sh
```

**Mac/Linux:**
```bash
# Definir DATABASE_URL
export DATABASE_URL='postgresql://usuario:senha@host:26257/defaultdb?sslmode=require'

# Dar permissão
chmod +x run_migration_clientes.sh
chmod +x run_migration_processos.sh

# Executar
./run_migration_clientes.sh
./run_migration_processos.sh
```

---

## 4️⃣ Verificar Migrations

Execute no console SQL do CockroachDB:

```sql
-- Listar tabelas
SHOW TABLES;

-- Deve mostrar:
-- clientes
-- processos
-- processo_documentos
-- processo_historico
-- (+ tabelas existentes)

-- Verificar estrutura
SHOW CREATE TABLE clientes;
SHOW CREATE TABLE processos;
SHOW CREATE TABLE processo_documentos;
SHOW CREATE TABLE processo_historico;

-- Verificar índices
SHOW INDEXES FROM processos;
SHOW INDEXES FROM clientes;
```

---

## 5️⃣ Testar Endpoints

### Obter Token JWT

Primeiro, faça login para obter um token:

```bash
# Login como tenant
curl -X POST https://seu-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seu-email@example.com",
    "password": "sua-senha"
  }'

# Copie o token da resposta
# Exemplo: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Testar Clientes

```bash
# Substitua SEU_TOKEN pelo token obtido acima
export TOKEN="SEU_TOKEN_AQUI"
export API_URL="https://seu-backend.onrender.com"

# Listar clientes (deve retornar array vazio inicialmente)
curl "$API_URL/api/tenant/clientes" \
  -H "Authorization: Bearer $TOKEN"

# Criar cliente
curl -X POST "$API_URL/api/tenant/clientes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "cpf_cnpj": "123.456.789-00",
    "email": "joao@example.com",
    "telefone": "(11) 98765-4321",
    "endereco": "Rua Exemplo, 123"
  }'

# Listar clientes novamente (deve retornar o cliente criado)
curl "$API_URL/api/tenant/clientes" \
  -H "Authorization: Bearer $TOKEN"
```

### Testar Processos

```bash
# Listar processos (deve retornar array vazio inicialmente)
curl "$API_URL/api/tenant/processos" \
  -H "Authorization: Bearer $TOKEN"

# Criar processo (use o ID do cliente criado acima)
curl -X POST "$API_URL/api/tenant/processos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "numero_processo": "0001234-56.2025.8.26.0100",
    "cliente_id": 1,
    "tipo": "Cível",
    "vara_tribunal": "1ª Vara Cível",
    "comarca": "São Paulo",
    "uf": "SP",
    "status": "Em Andamento",
    "valor_causa": 50000.00,
    "descricao": "Ação de cobrança de valores"
  }'

# Listar processos novamente
curl "$API_URL/api/tenant/processos" \
  -H "Authorization: Bearer $TOKEN"

# Buscar processos
curl "$API_URL/api/tenant/processos/search?q=cobrança" \
  -H "Authorization: Bearer $TOKEN"
```

### Testar Histórico

```bash
# Ver histórico de um processo (use o ID do processo criado)
curl "$API_URL/api/tenant/processos/1/historico" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 6️⃣ Verificar Logs

### No Render

1. Acesse: https://dashboard.render.com
2. Clique no seu serviço backend
3. Vá para a aba "Logs"
4. Procure por erros ou mensagens de sucesso

### Logs Esperados

Você deve ver algo como:
```
=== INICIANDO SERVIDOR API ===
Porta: 10000
Rotas registradas:
  GET  /api/tenant/processos
  POST /api/tenant/processos
  ...
==============================
```

---

## 🐛 Se Algo Der Errado

### Erro: "Tabela não existe"

**Causa:** Migrations não foram aplicadas

**Solução:** Volte ao passo 3 e aplique as migrations

### Erro: "Unauthorized"

**Causa:** Token JWT inválido ou expirado

**Solução:** Faça login novamente para obter novo token

### Erro: "Tenant não encontrado"

**Causa:** Usuário não tem tenant_id no token

**Solução:** Verifique se está usando login correto (não super-admin)

### Erro: "Connection refused"

**Causa:** Backend não está rodando ou URL errada

**Solução:** 
- Verifique se deploy foi concluído no Render
- Verifique se URL está correta
- Aguarde alguns minutos e tente novamente

---

## ✅ Checklist Final

Marque conforme for completando:

- [ ] Código commitado e pushed
- [ ] Deploy concluído no Render
- [ ] Migration 004 aplicada
- [ ] Migration 005 aplicada
- [ ] Tabelas criadas no banco
- [ ] Endpoint de clientes testado
- [ ] Endpoint de processos testado
- [ ] Endpoint de busca testado
- [ ] Endpoint de histórico testado
- [ ] Logs verificados (sem erros)

---

## 🎉 Pronto!

Se todos os testes passaram, seu backend está **100% funcional**! 🚀

**Próximo passo:** Criar o frontend para consumir estes endpoints.

---

**Criado em:** 04/11/2025  
**Tempo estimado:** 15-30 minutos para deploy completo

