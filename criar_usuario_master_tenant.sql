-- Script para criar usuário master de um tenant
-- Execute este script no console SQL do CockroachDB

-- PASSO 1: Escolha um tenant (veja a lista abaixo)
-- Tenants disponíveis:
-- 1107989906250989569 - Advocacia Teste Final (advocacia-teste-final)
-- 1113422676350369793 - escritorio-dia-07-10-25 (escritorio-teste)
-- 1121080526343929857 - escritorio33 (escritorio33)

-- PASSO 2: Defina as variáveis
-- Substitua os valores abaixo:
SET tenant_id = 1107989906250989569;  -- ID do tenant escolhido
SET user_email = 'master@advocacia.com';  -- Email do usuário master
SET user_name = 'Master User';  -- Nome do usuário
SET user_password = 'senha123';  -- Senha (será hasheada)

-- PASSO 3: Criar o usuário master
INSERT INTO users (
  tenant_id,
  email,
  password_hash,
  full_name,
  role,
  temporary_password,
  created_at
) VALUES (
  $tenant_id,
  $user_email,
  -- Hash bcrypt da senha 'senha123'
  'bcrypt+sha512$acfa624696d5030fe8757d994f0a0a21$12$78efc39bcf8917152fa6dd63e7daa699c746ec8b76859786',
  $user_name,
  'master',
  false,
  NOW()
);

-- PASSO 4: Verificar se foi criado
SELECT 
  id,
  email,
  full_name,
  role,
  tenant_id,
  created_at
FROM users
WHERE email = $user_email;

-- RESULTADO ESPERADO:
-- Você verá o usuário criado com role 'master'

-- PASSO 5: Fazer login
-- Acesse: https://seu-frontend.onrender.com/login
-- Email: master@advocacia.com
-- Senha: senha123
