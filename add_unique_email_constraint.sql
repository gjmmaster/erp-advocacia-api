-- Script para adicionar constraint de email único
-- Necessário para auto-descoberta de tenant por email

-- Primeiro, verificar se existem emails duplicados
SELECT email, COUNT(*) as count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;

-- Se não houver duplicatas, adicionar constraint
-- IMPORTANTE: Execute este comando apenas se a query acima retornar 0 linhas
ALTER TABLE users ADD CONSTRAINT unique_email UNIQUE (email);

-- Verificar que o constraint foi criado
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'users' AND constraint_name = 'unique_email';
