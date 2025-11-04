-- Migration: Create processos tables
-- Description: Tabelas para gestão de processos jurídicos (processos, documentos, histórico)
-- Date: 2025-11-04

BEGIN;

-- ============================================
-- Tabela: processos
-- ============================================
CREATE TABLE IF NOT EXISTS processos (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  numero_processo VARCHAR(50) NOT NULL,
  cliente_id BIGINT NOT NULL REFERENCES clientes(id),
  tipo VARCHAR(100) NOT NULL,
  vara_tribunal VARCHAR(200),
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

-- Índices da tabela processos
CREATE INDEX idx_processos_tenant ON processos(tenant_id);
CREATE INDEX idx_processos_cliente ON processos(cliente_id);
CREATE INDEX idx_processos_status ON processos(status);
CREATE INDEX idx_processos_numero ON processos(numero_processo);
CREATE INDEX idx_processos_deleted ON processos(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_processos_created_at ON processos(created_at DESC);

-- Comentários da tabela processos
COMMENT ON TABLE processos IS 'Processos jurídicos gerenciados pelos escritórios';
COMMENT ON COLUMN processos.tenant_id IS 'FK para tenants - isolamento multi-tenant';
COMMENT ON COLUMN processos.numero_processo IS 'Número único do processo (único por tenant)';
COMMENT ON COLUMN processos.cliente_id IS 'FK para clientes - cliente do processo';
COMMENT ON COLUMN processos.status IS 'Status: Em Andamento, Suspenso, Arquivado, Encerrado';
COMMENT ON COLUMN processos.deleted_at IS 'Soft delete - quando não NULL, registro está deletado';

-- ============================================
-- Tabela: processo_documentos
-- ============================================
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

-- Índices da tabela processo_documentos
CREATE INDEX idx_processo_docs_processo ON processo_documentos(processo_id);
CREATE INDEX idx_processo_docs_deleted ON processo_documentos(deleted_at) WHERE deleted_at IS NULL;

-- Comentários da tabela processo_documentos
COMMENT ON TABLE processo_documentos IS 'Documentos anexados aos processos';
COMMENT ON COLUMN processo_documentos.processo_id IS 'FK para processos - processo ao qual o documento pertence';
COMMENT ON COLUMN processo_documentos.caminho_storage IS 'Caminho do arquivo no storage (filesystem ou S3)';
COMMENT ON COLUMN processo_documentos.deleted_at IS 'Soft delete - quando não NULL, documento está deletado';

-- ============================================
-- Tabela: processo_historico
-- ============================================
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

-- Índices da tabela processo_historico
CREATE INDEX idx_processo_hist_processo ON processo_historico(processo_id);
CREATE INDEX idx_processo_hist_created ON processo_historico(created_at DESC);

-- Comentários da tabela processo_historico
COMMENT ON TABLE processo_historico IS 'Histórico de alterações dos processos (auditoria)';
COMMENT ON COLUMN processo_historico.acao IS 'Tipo de ação: criacao, edicao, exclusao, mudanca_status';
COMMENT ON COLUMN processo_historico.campo_alterado IS 'Nome do campo que foi alterado';
COMMENT ON COLUMN processo_historico.valor_anterior IS 'Valor antes da alteração';
COMMENT ON COLUMN processo_historico.valor_novo IS 'Valor após a alteração';

COMMIT;
