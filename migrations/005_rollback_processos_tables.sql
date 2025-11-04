-- Rollback: Drop processos tables
-- Description: Remove tabelas de processos, documentos e histórico
-- Date: 2025-11-04

BEGIN;

-- Ordem inversa de criação (por causa das FKs)
DROP TABLE IF EXISTS processo_historico CASCADE;
DROP TABLE IF EXISTS processo_documentos CASCADE;
DROP TABLE IF EXISTS processos CASCADE;

COMMIT;
