# Requirements Document - Autenticação e Dashboard de Tenants

**Data:** 24 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Em Revisão

---

## Introdução

Este documento especifica os requisitos para a implementação do sistema de autenticação e dashboard para os escritórios de advocacia (tenants). Esta é a primeira fase do sistema voltado para os usuários finais, permitindo que admins e operadores de cada escritório acessem o sistema através de seus subdomínios específicos.

### Contexto

- **Super Admin:** Já implementado e funcional
- **Backend:** API Clojure com autenticação JWT e multi-tenancy
- **Frontend:** Next.js 14 com BFF
- **Banco de Dados:** PostgreSQL (CockroachDB) com tabelas `tenants` e `users`

### Objetivo

Permitir que usuários de cada escritório (tenant) façam login através de seu subdomínio específico e acessem um dashboard personalizado com informações do seu escritório.

---

## Requirements

### Requirement 1: Identificação de Tenant por Subdomínio

**User Story:** Como um usuário de um escritório, eu quero acessar o sistema através do subdomínio do meu escritório (ex: `escritorio-silva.seudominio.com`), para que eu seja automaticamente direcionado ao contexto correto.

#### Acceptance Criteria

1. WHEN um usuário acessa `escritorio-silva.seudominio.com` THEN o sistema identifica o tenant pelo subdomínio
2. WHEN o subdomínio não existe THEN o sistema retorna erro 404 com mensagem "Escritório não encontrado"
3. WHEN o subdomínio é inválido THEN o sistema retorna erro 400 com mensagem "Subdomínio inválido"
4. WHEN o tenant está inativo THEN o sistema retorna erro 403 com mensagem "Escritório desativado"
5. IF o usuário acessa o domínio raiz (sem subdomínio) THEN o sistema redireciona para página de seleção ou login do super admin

---

### Requirement 2: Login de Usuários do Tenant

**User Story:** Como um admin ou operador de um escritório, eu quero fazer login com meu e-mail e senha, para que eu possa acessar o sistema do meu escritório.

#### Acceptance Criteria

1. WHEN um usuário acessa a página de login THEN o sistema exibe formulário com campos de e-mail e senha
2. WHEN o usuário submete credenciais válidas THEN o sistema autentica e redireciona para o dashboard
3. WHEN o usuário submete credenciais inválidas THEN o sistema exibe mensagem "E-mail ou senha incorretos"
4. WHEN o usuário pertence a outro tenant THEN o sistema exibe mensagem "Usuário não pertence a este escritório"
5. WHEN o usuário está inativo THEN o sistema exibe mensagem "Usuário desativado"
6. WHEN o login é bem-sucedido THEN o sistema cria sessão com token JWT em cookie HttpOnly
7. WHEN o login é bem-sucedido THEN o token JWT contém: user-id, email, role, tenant-id
8. IF o usuário já está autenticado THEN o sistema redireciona automaticamente para o dashboard

---

### Requirement 3: Controle de Acesso por Role

**User Story:** Como o sistema, eu quero diferenciar entre admins e operadores, para que eu possa aplicar permissões diferentes.

#### Acceptance Criteria

1. WHEN um usuário com role "master" faz login THEN o sistema concede acesso total ao escritório
2. WHEN um usuário com role "operador" faz login THEN o sistema concede acesso limitado
3. WHEN um operador tenta acessar funcionalidade de admin THEN o sistema retorna erro 403
4. WHEN o token JWT é validado THEN o sistema verifica se o usuário pertence ao tenant correto
5. IF o tenant-id do token não corresponde ao subdomínio THEN o sistema invalida a sessão

---

### Requirement 4: Dashboard do Tenant

**User Story:** Como um usuário autenticado, eu quero ver um dashboard com informações do meu escritório, para que eu tenha uma visão geral do sistema.

#### Acceptance Criteria

1. WHEN o usuário acessa o dashboard THEN o sistema exibe o nome do escritório
2. WHEN o dashboard carrega THEN o sistema exibe cards com métricas:
   - Total de processos
   - Total de clientes
   - Total de operadores
   - Processos ativos
3. WHEN o usuário é admin THEN o sistema exibe menu com todas as opções
4. WHEN o usuário é operador THEN o sistema exibe menu com opções limitadas
5. WHEN o dashboard carrega THEN o sistema exibe menu de navegação com:
   - Dashboard (home)
   - Processos
   - Clientes
   - Operadores (apenas admin)
   - Perfil
   - Sair

---

### Requirement 5: Logout

**User Story:** Como um usuário autenticado, eu quero fazer logout, para que eu possa encerrar minha sessão de forma segura.

#### Acceptance Criteria

1. WHEN o usuário clica em "Sair" THEN o sistema remove o cookie de autenticação
2. WHEN o logout é concluído THEN o sistema redireciona para a página de login
3. WHEN o usuário tenta acessar rota protegida após logout THEN o sistema redireciona para login
4. WHEN o logout ocorre THEN o sistema registra o evento nos logs

---

### Requirement 6: Proteção de Rotas

**User Story:** Como o sistema, eu quero proteger todas as rotas do tenant, para que apenas usuários autenticados do tenant correto possam acessar.

#### Acceptance Criteria

1. WHEN um usuário não autenticado tenta acessar rota protegida THEN o sistema redireciona para login
2. WHEN um usuário de outro tenant tenta acessar THEN o sistema retorna erro 403
3. WHEN o token JWT expira THEN o sistema redireciona para login
4. WHEN o middleware valida o token THEN o sistema verifica:
   - Token é válido
   - Token não expirou
   - Usuário pertence ao tenant do subdomínio
   - Usuário tem permissão para a rota

---

### Requirement 7: Feedback Visual

**User Story:** Como um usuário, eu quero receber feedback visual das minhas ações, para que eu saiba o que está acontecendo.

#### Acceptance Criteria

1. WHEN o login está processando THEN o sistema exibe indicador de loading
2. WHEN ocorre erro THEN o sistema exibe mensagem de erro clara
3. WHEN o login é bem-sucedido THEN o sistema exibe mensagem de sucesso (opcional)
4. WHEN o dashboard está carregando THEN o sistema exibe skeleton loaders
5. WHEN uma ação é concluída THEN o sistema fornece feedback visual apropriado

---

### Requirement 8: Responsividade

**User Story:** Como um usuário, eu quero acessar o sistema de qualquer dispositivo, para que eu possa trabalhar de onde estiver.

#### Acceptance Criteria

1. WHEN o usuário acessa de desktop THEN o sistema exibe layout otimizado para desktop
2. WHEN o usuário acessa de tablet THEN o sistema exibe layout otimizado para tablet
3. WHEN o usuário acessa de mobile THEN o sistema exibe layout otimizado para mobile
4. WHEN o usuário redimensiona a janela THEN o sistema adapta o layout automaticamente

---

## Requisitos Não-Funcionais

### Segurança

1. Tokens JWT devem ser armazenados em cookies HttpOnly
2. Cookies devem ter flag Secure em produção
3. Cookies devem ter SameSite=lax para proteção CSRF
4. Senhas nunca devem ser expostas em logs ou respostas
5. Rate limiting deve ser aplicado ao endpoint de login

### Performance

1. Página de login deve carregar em < 1 segundo
2. Dashboard deve carregar em < 2 segundos
3. Validação de token deve ocorrer em < 100ms
4. Identificação de tenant por subdomínio deve ser instantânea

### Usabilidade

1. Mensagens de erro devem ser claras e acionáveis
2. Formulários devem ter validação client-side
3. Campos de formulário devem ter labels e placeholders
4. Botões devem ter estados de loading

### Compatibilidade

1. Suporte a navegadores modernos (Chrome, Firefox, Safari, Edge)
2. Suporte a dispositivos móveis (iOS, Android)
3. Funcionar com JavaScript habilitado

---

## Fora do Escopo (Não Implementar Nesta Fase)

1. ❌ Recuperação de senha (será implementada na próxima fase)
2. ❌ Alteração de senha (será implementada na próxima fase)
3. ❌ Perfil do usuário
4. ❌ Notificações
5. ❌ CRUD de processos (próxima fase)
6. ❌ CRUD de clientes (próxima fase)
7. ❌ Gestão de operadores (próxima fase)
8. ❌ Two-Factor Authentication (2FA)

---

## Dependências

### Backend

- ✅ Tabela `tenants` com coluna `subdomain`
- ✅ Tabela `users` com colunas `tenant_id`, `role`, `email`, `password_hash`
- ✅ Endpoint `/auth/login` (precisa ser adaptado)
- ✅ Middleware de autenticação JWT
- ✅ Rate limiting

### Frontend

- ✅ Next.js 14 com App Router
- ✅ Biblioteca de autenticação (jose)
- ✅ Cookies HttpOnly
- ✅ Middleware de proteção de rotas

---

## Critérios de Aceitação Geral

Esta feature será considerada completa quando:

1. ✅ Usuário pode acessar via subdomínio
2. ✅ Usuário pode fazer login com e-mail e senha
3. ✅ Sistema valida que usuário pertence ao tenant correto
4. ✅ Dashboard exibe informações básicas do escritório
5. ✅ Menu de navegação está funcional
6. ✅ Usuário pode fazer logout
7. ✅ Rotas estão protegidas por autenticação
8. ✅ Sistema diferencia entre admin e operador
9. ✅ Interface é responsiva
10. ✅ Todos os testes passam

---

## Próximos Passos

Após aprovação deste documento:

1. Criar documento de Design
2. Criar lista de Tasks
3. Implementar backend
4. Implementar frontend
5. Testar
6. Deploy

---

**Documento criado em:** 24/10/2025  
**Última atualização:** 24/10/2025  
**Status:** Aguardando Revisão
