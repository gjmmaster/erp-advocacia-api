-- ============================================
-- SCRIPT CONSOLIDADO - APLICAR TODAS AS MIGRATIONS
-- Execute este script no console SQL do CockroachDB
-- ============================================

-- Migration 1: Criar tabela de clientes
-- ============================================

BEGIN;

CREATE TABLE IF NOT EXISTS clientes (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  nome VARCHAR(255) NOT NULL,
  cpf_cnpj VARCHAR(20),
  email VARCHAR(255),
  telefone VARCHAR(20),
  endereco TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_clientes_tenant ON clientes(tenant_id);
CREATE UNIQUE INDEX idx_clientes_cpf_tenant ON clientes(tenant_id, cpf_cnpj) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_deleted ON clientes(deleted_at) WHERE deleted_at IS NULL;

COMMIT;

-- Migration 2: Criar tabelas de processos
-- ============================================

BEGIN;

-- Tabela: processos
CREATE TABLE IF NOT EXISTS processos (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  numero_processo VARCHAR(50) NOT NULL,
  cliente_id BIGINT NOT NULL REFERENCES clientes(id),
  tipo VARCHAR(100) NOT NULL,
  vara VARCHAR(200),
  comarca VARCHAR(100),
  uf VARCHAR(2),
  status VARCHAR(50) DEFAULT 'Em Andamento',
  valor_causa DECIMAL(15,2),
  data_distribuicao DATE,
  descricao TEXT,
  observacoes TEXT,
  created_by BIGINT REFERENCES users(id),
  updated_by BIGINT REFERENCES users(id),
  deleted_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  deleted_at TIMESTAMPTZ,
  CONSTRAINT unique_processo_tenant UNIQUE(tenant_id, numero_processo)
);

CREATE INDEX idx_processos_tenant ON processos(tenant_id);
CREATE INDEX idx_processos_cliente ON processos(cliente_id);
CREATE INDEX idx_processos_status ON processos(status);
CREATE INDEX idx_processos_numero ON processos(numero_processo);
CREATE INDEX idx_processos_deleted ON processos(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_processos_created_at ON processos(created_at DESC);

-- Tabela: processo_documentos
CREATE TABLE IF NOT EXISTS processo_documentos (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  processo_id BIGINT NOT NULL REFERENCES processos(id) ON DELETE CASCADE,
  nome_arquivo VARCHAR(255) NOT NULL,
  tipo_arquivo VARCHAR(50),
  tamanho_bytes BIGINT,
  caminho_storage VARCHAR(500) NOT NULL,
  uploaded_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_processo_docs_processo ON processo_documentos(processo_id);
CREATE INDEX idx_processo_docs_deleted ON processo_documentos(deleted_at) WHERE deleted_at IS NULL;

-- Tabela: processo_historico
CREATE TABLE IF NOT EXISTS processo_historico (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  processo_id BIGINT NOT NULL REFERENCES processos(id) ON DELETE CASCADE,
  user_id BIGINT REFERENCES users(id),
  acao VARCHAR(50) NOT NULL,
  campo_alterado VARCHAR(100),
  valor_anterior TEXT,
  valor_novo TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_processo_hist_processo ON processo_historico(processo_id);
CREATE INDEX idx_processo_hist_created ON processo_historico(created_at DESC);

COMMIT;

-- ============================================
-- VERIFICAÇÃO
-- ============================================

-- Verificar se as tabelas foram criadas
SHOW TABLES;

-- Deve mostrar:
-- - clientes
-- - processos
-- - processo_documentos
-- - processo_historico

-- ============================================
-- PRONTO!
-- ============================================
-- Agora você pode:
-- 1. Recarregar a página do processo
-- 2. Ver a seção de documentos
-- 3. Fazer upload de arquivos
-- ============================================
