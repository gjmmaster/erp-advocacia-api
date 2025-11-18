# Modo Desenvolvimento vs Produção

## Visão Geral

O sistema suporta dois modos de operação:

1. **Modo Desenvolvimento (DEV_MODE=true)**: Usa repositório mock in-memory
2. **Modo Produção (DEV_MODE=false)**: Usa PostgreSQL/CockroachDB

## Configuração

### Desenvolvimento Local

**Arquivo:** `.env.development`

```bash
DEV_MODE=true
DATABASE_URL=postgresql://localhost:5432/juridico_dev  # Ignorado em modo mock
JWT_SECRET=chave-padrao-para-desenvolvimento-segura
PORT=3000
ENVIRONMENT=development
```

**Como iniciar:**
```bash
# Windows
.\dev-mock.ps1

# Linux/Mac
./dev-mock.sh
```

### Produção

**Arquivo:** `.env.production`

```bash
DEV_MODE=false
DATABASE_URL=postgresql://user:pass@host:5432/dbname
JWT_SECRET=sua-chave-secreta-forte
PORT=3000
ENVIRONMENT=production
```

**Variáveis de Ambiente no Render.com:**
- `DEV_MODE=false` (ou não definir - padrão é false)
- `DATABASE_URL=<sua-connection-string>`
- `JWT_SECRET=<sua-chave-secreta>`
- `PORT=3000`

## Diferenças entre Modos

### Modo Desenvolvimento (Mock)

**Vantagens:**
- ✅ Não precisa de banco de dados
- ✅ Inicialização rápida
- ✅ Dados de seed pré-carregados
- ✅ Ideal para desenvolvimento de UI
- ✅ Não precisa de migrations

**Limitações:**
- ❌ Dados não persistem entre reinícios
- ❌ Performance pode ser diferente do banco real
- ❌ Não testa queries SQL reais
- ❌ Não testa transações de banco

**Dados de Seed:**
```
Super Admin: admin@demo.com / admin123
Master User: master@demo.com / master123
Operador 1:  operador1@demo.com / operador123
Operador 2:  operador2@demo.com / operador123

1 Tenant: Demo Company (subdomain: demo)
3 Clientes
5 Processos
8 Documentos
10 Entradas de Histórico
```

### Modo Produção (PostgreSQL)

**Vantagens:**
- ✅ Dados persistem permanentemente
- ✅ Performance otimizada
- ✅ Suporta queries complexas
- ✅ Transações ACID
- ✅ Backups e recuperação

**Requisitos:**
- ⚠️ Precisa de PostgreSQL/CockroachDB rodando
- ⚠️ Precisa executar migrations
- ⚠️ Precisa configurar DATABASE_URL

## Como o Sistema Decide Qual Modo Usar

O arquivo `src/juridico/api/db/core.clj` verifica a variável `DEV_MODE`:

```clojure
(defn create-repository [tenant-id]
  (if (= "true" (env :dev-mode))
    ;; Modo DEV - usa Mock
    (mock/create-mock-repository)
    ;; Modo PROD - usa PostgreSQL
    (pg/->PostgresRepository @pg/datasource tenant-id)))
```

## Compatibilidade de Dados

Ambos os repositórios (Mock e PostgreSQL) retornam dados no **mesmo formato** com namespaces:

```clojure
{:users/id 1
 :users/email "admin@demo.com"
 :users/password_hash "$2a$..."
 :users/full_name "Super Administrador"
 :users/role "super-admin"
 :users/temporary_password false
 :users/tenant_id nil}
```

Isso garante que os handlers funcionem identicamente em ambos os modos.

## Testando Produção Localmente

Se você quiser testar o modo produção localmente com PostgreSQL:

1. **Inicie o PostgreSQL:**
   ```bash
   docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
   ```

2. **Configure `.env.development`:**
   ```bash
   DEV_MODE=false
   DATABASE_URL=postgresql://postgres:postgres@localhost:5432/juridico_dev
   ```

3. **Execute migrations:**
   ```bash
   # Conecte ao banco e execute os arquivos em migrations/
   psql -h localhost -U postgres -d juridico_dev -f migrations/001_create_tenants.sql
   # ... outros arquivos
   ```

4. **Inicie o servidor:**
   ```bash
   lein run
   ```

## Troubleshooting

### "Credenciais inválidas" em modo dev

**Causa:** O repositório mock não está sendo usado.

**Solução:** Verifique que `DEV_MODE=true` está em `.env.development`

### "Database connection failed" em modo prod

**Causa:** `DATABASE_URL` não está configurada ou está incorreta.

**Solução:** Verifique a connection string e que o banco está acessível.

### Dados não aparecem em modo dev

**Causa:** Isso é esperado! Dados mock não persistem.

**Solução:** Use modo produção se precisar de persistência.

### Modo errado está sendo usado

**Causa:** Variável de ambiente não está sendo carregada.

**Solução:** 
1. Verifique que o arquivo `.env.development` ou `.env.production` existe
2. Reinicie o servidor
3. Verifique os logs - deve aparecer "MODO DE DESENVOLVIMENTO" ou "MODO DE PRODUÇÃO"

## Checklist de Deploy para Produção

Antes de fazer deploy, garanta que:

- [ ] `DEV_MODE=false` (ou não definida) no Render.com
- [ ] `DATABASE_URL` configurada corretamente
- [ ] `JWT_SECRET` é uma chave forte e única
- [ ] Migrations foram executadas no banco
- [ ] Usuário super-admin foi criado no banco
- [ ] Variáveis de R2 (Cloudflare) estão configuradas
- [ ] Testou login em produção
- [ ] Testou criação de tenant em produção

## Logs para Identificar o Modo

Quando o servidor inicia, você verá:

**Modo Desenvolvimento:**
```
========================================
🔧 MODO DE DESENVOLVIMENTO ATIVADO
========================================
📦 Usando Repositório MOCK (in-memory)
⚡ Dados não persistem entre reinícios
🚀 Ideal para desenvolvimento rápido
========================================
[MOCK] 🚀 Repositório Mock Inicializado
```

**Modo Produção:**
```
========================================
🚀 MODO DE PRODUÇÃO
========================================
🗄️  Usando Repositório PostgreSQL
💾 Dados persistem no banco de dados
🔒 Ambiente de produção
========================================
```

## Referências

- [Guia de Desenvolvimento Local](./guias/GUIA_DESENVOLVIMENTO_LOCAL.md)
- [Checklist de Deploy](./deploy/CHECKLIST_DEPLOY.md)
- [Database Schema](./DATABASE_SCHEMA.md)
