-- Migration: Create clientes table
-- Description: Tabela de clientes para vincular aos processos jurídicos
-- Date: 2025-11-04

BEGIN;

-- Criar tabela de clientes
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

-- Criar índices
CREATE INDEX idx_clientes_tenant ON clientes(tenant_id);
CREATE UNIQUE INDEX idx_clientes_cpf_tenant ON clientes(tenant_id, cpf_cnpj) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_deleted ON clientes(deleted_at) WHERE deleted_at IS NULL;

-- Comentários
COMMENT ON TABLE clientes IS 'Clientes dos escritórios de advocacia (multi-tenant)';
COMMENT ON COLUMN clientes.tenant_id IS 'FK para tenants - isolamento multi-tenant';
COMMENT ON COLUMN clientes.cpf_cnpj IS 'CPF ou CNPJ do cliente (único por tenant)';
COMMENT ON COLUMN clientes.deleted_at IS 'Soft delete - quando não NULL, registro está deletado';

COMMIT;
