-- ========================================
-- MIGRAÇÃO: BIGINT para UUID
-- ========================================
-- Este script converte os IDs de BIGINT para UUID
-- para evitar problemas de precisão no JavaScript
--
-- IMPORTANTE: Faça backup antes de executar!
-- ========================================

-- Passo 1: Adicionar nova coluna UUID em todas as tabelas
ALTER TABLE tenants ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();
ALTER TABLE users ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();
ALTER TABLE legal_cases ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();

-- Passo 2: Preencher os UUIDs (já feito pelo DEFAULT acima)
-- Verificar se foram gerados:
-- SELECT id, uuid_id FROM tenants LIMIT 5;
-- SELECT id, uuid_id FROM users LIMIT 5;
-- SELECT id, uuid_id FROM legal_cases LIMIT 5;

-- Passo 3: Adicionar colunas UUID para foreign keys
ALTER TABLE users ADD COLUMN tenant_uuid_id UUID;
ALTER TABLE legal_cases ADD COLUMN tenant_uuid_id UUID;

-- Passo 4: Preencher as foreign keys UUID
UPDATE users SET tenant_uuid_id = (SELECT uuid_id FROM tenants WHERE tenants.id = users.tenant_id);
UPDATE legal_cases SET tenant_uuid_id = (SELECT uuid_id FROM tenants WHERE tenants.id = legal_cases.tenant_id);

-- Passo 5: Remover constraints antigas
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_tenant_id_fkey;
ALTER TABLE legal_cases DROP CONSTRAINT IF EXISTS legal_cases_tenant_id_fkey;

-- Passo 6: Remover colunas antigas de ID
ALTER TABLE users DROP COLUMN tenant_id;
ALTER TABLE legal_cases DROP COLUMN tenant_id;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE legal_cases DROP COLUMN id;
ALTER TABLE tenants DROP COLUMN id;

-- Passo 7: Renomear colunas UUID para id
ALTER TABLE tenants RENAME COLUMN uuid_id TO id;
ALTER TABLE users RENAME COLUMN uuid_id TO id;
ALTER TABLE users RENAME COLUMN tenant_uuid_id TO tenant_id;
ALTER TABLE legal_cases RENAME COLUMN uuid_id TO id;
ALTER TABLE legal_cases RENAME COLUMN tenant_uuid_id TO tenant_id;

-- Passo 8: Adicionar PRIMARY KEYs
ALTER TABLE tenants ADD PRIMARY KEY (id);
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE legal_cases ADD PRIMARY KEY (id);

-- Passo 9: Adicionar FOREIGN KEYs com CASCADE
ALTER TABLE users 
  ADD CONSTRAINT users_tenant_id_fkey 
  FOREIGN KEY (tenant_id) 
  REFERENCES tenants(id) 
  ON DELETE CASCADE;

ALTER TABLE legal_cases 
  ADD CONSTRAINT legal_cases_tenant_id_fkey 
  FOREIGN KEY (tenant_id) 
  REFERENCES tenants(id) 
  ON DELETE CASCADE;

-- Passo 10: Recriar índices e constraints únicos
ALTER TABLE tenants ADD CONSTRAINT tenants_subdomain_unique UNIQUE (subdomain);
ALTER TABLE users ADD CONSTRAINT users_tenant_email_unique UNIQUE (tenant_id, email);

-- Verificação final
SELECT 'Tenants' as tabela, COUNT(*) as total FROM tenants
UNION ALL
SELECT 'Users' as tabela, COUNT(*) as total FROM users
UNION ALL
SELECT 'Legal Cases' as tabela, COUNT(*) as total FROM legal_cases;

-- Mostrar alguns exemplos
SELECT 'Exemplo de Tenant:' as info;
SELECT id, company_name, subdomain FROM tenants LIMIT 1;

SELECT 'Exemplo de User:' as info;
SELECT id, email, tenant_id FROM users LIMIT 1;

SELECT 'Exemplo de Legal Case:' as info;
SELECT id, tenant_id FROM legal_cases LIMIT 1;
