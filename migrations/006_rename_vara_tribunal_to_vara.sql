-- Migration: Rename vara_tribunal to vara
-- Description: Simplifica o nome da coluna vara_tribunal para apenas vara
-- Date: 2025-11-06

BEGIN;

ALTER TABLE processos RENAME COLUMN vara_tribunal TO vara;

COMMIT;
