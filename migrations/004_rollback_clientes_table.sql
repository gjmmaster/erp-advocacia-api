-- Rollback: Drop clientes table
-- Description: Remove tabela de clientes
-- Date: 2025-11-04

BEGIN;

DROP TABLE IF EXISTS clientes CASCADE;

COMMIT;
