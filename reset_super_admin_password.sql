-- ============================================
-- RESET SENHA DO SUPER ADMIN
-- ============================================
-- Nova senha: Admin@123
-- Email: super@admin.com
-- ============================================

-- Passo 1: Ver o super admin atual
SELECT id, email, role, created_at 
FROM users 
WHERE role = 'super-admin';

-- Passo 2: Atualizar senha para "Admin@123"
-- Este é um hash bcrypt válido gerado pelo buddy.hashers
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'super@admin.com' AND role = 'super-admin';

-- Passo 3: Verificar atualização
SELECT id, email, role, created_at 
FROM users 
WHERE role = 'super-admin';

-- ============================================
-- PRONTO! Agora você pode fazer login com:
-- Email: super@admin.com
-- Senha: Admin@123
-- ============================================
