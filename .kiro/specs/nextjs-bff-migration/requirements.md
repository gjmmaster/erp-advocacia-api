# Requirements Document - Migração para Next.js BFF

## Introduction

Este documento define os requisitos para migrar o frontend atual (Vite + React) para Next.js 14 com App Router, implementando um BFF (Backend for Frontend) que resolve a vulnerabilidade de armazenamento inseguro de JWT no localStorage através de cookies HttpOnly gerenciados server-side.

### Contexto

**Problema Atual:**
- Frontend Vite + React armazena JWT no `localStorage`
- Vulnerável a ataques XSS (Cross-Site Scripting)
- Token acessível via JavaScript malicioso
- Sem renovação automática de sessão

**Solução Proposta:**
- Migrar para Next.js 14 (App Router)
- Implementar BFF com API Routes
- Cookies HttpOnly + Secure (gerenciados server-side)
- Renovação automática de sessão
- Middleware de autenticação global

### Objetivos

1. **Segurança:** Eliminar vulnerabilidade XSS do JWT
2. **Manutenibilidade:** Código mais simples e padrão
3. **Robustez:** Menos pontos de falha
4. **Escalabilidade:** Preparar para crescimento futuro

---

## Requirements

### Requirement 1: Estrutura Base do Projeto Next.js

**User Story:** Como desenvolvedor, eu quero uma estrutura de projeto Next.js bem organizada, para que o código seja fácil de manter e escalar.

#### Acceptance Criteria

1. WHEN o projeto Next.js é criado THEN SHALL usar Next.js 14 com App Router
2. WHEN a estrutura de pastas é definida THEN SHALL seguir as convenções do Next.js 14
3. WHEN as dependências são instaladas THEN SHALL incluir todas as bibliotecas necessárias (axios, jose para JWT, etc.)
4. WHEN o TypeScript é configurado THEN SHALL ter tipos estritos habilitados
5. WHEN o ESLint é configurado THEN SHALL usar regras recomendadas do Next.js
6. WHEN variáveis de ambiente são definidas THEN SHALL ter `.env.local` e `.env.example`
7. WHEN o projeto é inicializado THEN SHALL compilar sem erros

### Requirement 2: Migração de Componentes React

**User Story:** Como desenvolvedor, eu quero migrar os componentes React existentes para Next.js, para que a interface do usuário permaneça funcional e consistente.

#### Acceptance Criteria

1. WHEN componentes são migrados THEN SHALL manter a mesma funcionalidade
2. WHEN componentes são migrados THEN SHALL usar Client Components (`'use client'`) quando necessário
3. WHEN estilos CSS são migrados THEN SHALL manter a mesma aparência visual
4. WHEN componentes usam hooks THEN SHALL funcionar corretamente no Next.js
5. WHEN componentes fazem requisições THEN SHALL usar as novas API routes do BFF
6. WHEN rotas são definidas THEN SHALL usar o sistema de roteamento do App Router
7. WHEN navegação é implementada THEN SHALL usar `next/navigation` (useRouter, usePathname)

### Requirement 3: Implementação do BFF - Autenticação

**User Story:** Como usuário, eu quero fazer login de forma segura, para que minha sessão seja protegida contra ataques XSS.

#### Acceptance Criteria

1. WHEN usuário faz login THEN SHALL enviar credenciais para API route `/api/auth/login`
2. WHEN API route recebe credenciais THEN SHALL fazer proxy para backend Clojure
3. WHEN backend retorna JWT THEN SHALL armazenar em cookie HttpOnly + Secure
4. WHEN cookie é definido THEN SHALL ter flags: `httpOnly: true`, `secure: true`, `sameSite: 'lax'`
5. WHEN cookie é definido THEN SHALL ter duração de 7 dias
6. WHEN login é bem-sucedido THEN SHALL redirecionar para dashboard
7. WHEN login falha THEN SHALL retornar erro apropriado sem expor detalhes técnicos

### Requirement 4: Implementação do BFF - Renovação de Sessão

**User Story:** Como usuário, eu quero que minha sessão seja renovada automaticamente, para que eu não precise fazer login repetidamente.

#### Acceptance Criteria

1. WHEN token está próximo de expirar THEN SHALL renovar automaticamente
2. WHEN renovação é necessária THEN SHALL chamar API route `/api/auth/refresh`
3. WHEN API route refresh é chamada THEN SHALL validar cookie existente
4. WHEN cookie é válido THEN SHALL gerar novo token no backend
5. WHEN novo token é gerado THEN SHALL atualizar cookie HttpOnly
6. WHEN renovação falha THEN SHALL redirecionar para login
7. WHEN renovação é bem-sucedida THEN SHALL ser transparente para o usuário

### Requirement 5: Implementação do BFF - Proxy de Requisições

**User Story:** Como desenvolvedor, eu quero que todas as requisições ao backend passem pelo BFF, para que a autenticação seja gerenciada de forma centralizada.

#### Acceptance Criteria

1. WHEN componente faz requisição THEN SHALL chamar API route do BFF (não backend direto)
2. WHEN API route recebe requisição THEN SHALL extrair JWT do cookie
3. WHEN JWT é extraído THEN SHALL adicionar header `Authorization: Bearer <token>`
4. WHEN requisição é feita ao backend THEN SHALL usar axios com configuração apropriada
5. WHEN backend retorna resposta THEN SHALL repassar para o cliente
6. WHEN backend retorna erro 401 THEN SHALL tentar renovar sessão
7. WHEN erro persiste THEN SHALL retornar erro apropriado ao cliente

### Requirement 6: Middleware de Autenticação Global

**User Story:** Como desenvolvedor, eu quero um middleware que proteja rotas automaticamente, para que não seja necessário validar autenticação em cada página.

#### Acceptance Criteria

1. WHEN middleware é executado THEN SHALL verificar presença de cookie de autenticação
2. WHEN rota é protegida AND cookie não existe THEN SHALL redirecionar para login
3. WHEN rota é pública THEN SHALL permitir acesso sem autenticação
4. WHEN cookie existe THEN SHALL validar JWT
5. WHEN JWT é inválido THEN SHALL redirecionar para login
6. WHEN JWT é válido THEN SHALL permitir acesso à rota
7. WHEN middleware executa THEN SHALL adicionar headers de segurança (CSP, X-Frame-Options, etc.)

### Requirement 7: Páginas e Rotas do Next.js

**User Story:** Como usuário, eu quero acessar as mesmas funcionalidades do sistema, para que minha experiência não seja interrompida pela migração.

#### Acceptance Criteria

1. WHEN usuário acessa `/super-admin/login` THEN SHALL exibir página de login
2. WHEN usuário acessa `/super-admin/dashboard` THEN SHALL exibir dashboard (protegido)
3. WHEN usuário não autenticado acessa rota protegida THEN SHALL redirecionar para login
4. WHEN usuário autenticado acessa rota protegida THEN SHALL exibir conteúdo
5. WHEN usuário faz logout THEN SHALL limpar cookie e redirecionar para login
6. WHEN página carrega THEN SHALL verificar autenticação server-side (SSR)
7. WHEN navegação ocorre THEN SHALL manter estado de autenticação

### Requirement 8: Gestão de Tenants (Super Admin)

**User Story:** Como super admin, eu quero gerenciar tenants através do BFF, para que minhas ações sejam seguras e auditáveis.

#### Acceptance Criteria

1. WHEN super admin lista tenants THEN SHALL chamar `/api/admin/tenants` (GET)
2. WHEN super admin cria tenant THEN SHALL chamar `/api/admin/tenants` (POST)
3. WHEN super admin edita tenant THEN SHALL chamar `/api/admin/tenants/[id]` (PUT)
4. WHEN super admin deleta tenant THEN SHALL chamar `/api/admin/tenants/[id]` (DELETE)
5. WHEN API route é chamada THEN SHALL validar role de super-admin
6. WHEN role não é super-admin THEN SHALL retornar erro 403
7. WHEN operação é bem-sucedida THEN SHALL retornar resposta apropriada

### Requirement 9: Tratamento de Erros e Logging

**User Story:** Como desenvolvedor, eu quero logs detalhados e tratamento de erros robusto, para que problemas sejam identificados e resolvidos rapidamente.

#### Acceptance Criteria

1. WHEN erro ocorre no BFF THEN SHALL logar detalhes completos (stack trace, request, etc.)
2. WHEN erro é retornado ao cliente THEN SHALL ser mensagem genérica (sem detalhes técnicos)
3. WHEN requisição é feita THEN SHALL logar método, URL e status code
4. WHEN autenticação falha THEN SHALL logar tentativa com IP e timestamp
5. WHEN erro 500 ocorre THEN SHALL logar contexto completo para debugging
6. WHEN ambiente é produção THEN SHALL usar nível de log apropriado (error, warn, info)
7. WHEN logs são gerados THEN SHALL incluir request ID para rastreamento

### Requirement 10: Configuração e Deploy

**User Story:** Como DevOps, eu quero configurar e fazer deploy do Next.js facilmente, para que o sistema esteja disponível em produção.

#### Acceptance Criteria

1. WHEN projeto é configurado THEN SHALL ter variáveis de ambiente documentadas
2. WHEN build é executado THEN SHALL gerar bundle otimizado
3. WHEN deploy é feito THEN SHALL funcionar no Vercel ou Render
4. WHEN ambiente é produção THEN SHALL usar HTTPS obrigatório
5. WHEN cookies são definidos THEN SHALL usar flag `secure: true` em produção
6. WHEN CORS é configurado THEN SHALL permitir apenas origens confiáveis
7. WHEN healthcheck é chamado THEN SHALL retornar status do serviço

### Requirement 11: Testes e Validação

**User Story:** Como desenvolvedor, eu quero testes automatizados, para que a qualidade do código seja garantida.

#### Acceptance Criteria

1. WHEN testes são executados THEN SHALL validar API routes
2. WHEN testes são executados THEN SHALL validar middleware de autenticação
3. WHEN testes são executados THEN SHALL validar fluxo de login completo
4. WHEN testes são executados THEN SHALL validar renovação de sessão
5. WHEN testes são executados THEN SHALL validar tratamento de erros
6. WHEN testes são executados THEN SHALL ter cobertura mínima de 70%
7. WHEN testes falham THEN SHALL impedir deploy

### Requirement 12: Migração de Dados e Compatibilidade

**User Story:** Como usuário existente, eu quero que minha sessão continue funcionando após a migração, para que não precise fazer login novamente.

#### Acceptance Criteria

1. WHEN migração ocorre THEN SHALL manter compatibilidade com backend Clojure
2. WHEN backend retorna JWT THEN SHALL ser compatível com formato atual
3. WHEN usuário tem sessão ativa THEN SHALL poder continuar usando (transição suave)
4. WHEN endpoints do backend mudam THEN SHALL atualizar apenas o BFF (não componentes)
5. WHEN formato de resposta muda THEN SHALL adaptar no BFF (não componentes)
6. WHEN erro de compatibilidade ocorre THEN SHALL logar e alertar
7. WHEN rollback é necessário THEN SHALL ser possível reverter para Vite

---

## Non-Functional Requirements

### Performance

1. **Tempo de Resposta:** API routes devem responder em < 200ms (95th percentile)
2. **First Contentful Paint:** < 1.5s
3. **Time to Interactive:** < 3s
4. **Bundle Size:** < 500KB (gzipped)

### Security

1. **Cookies:** HttpOnly + Secure + SameSite
2. **Headers:** CSP, X-Frame-Options, X-Content-Type-Options
3. **HTTPS:** Obrigatório em produção
4. **Rate Limiting:** Implementado no middleware

### Scalability

1. **Concurrent Users:** Suportar 1000+ usuários simultâneos
2. **Horizontal Scaling:** Stateless (pode escalar horizontalmente)
3. **Cache:** Implementar cache de respostas quando apropriado

### Maintainability

1. **Code Style:** ESLint + Prettier configurados
2. **TypeScript:** Tipos estritos em todo o código
3. **Documentation:** Comentários JSDoc em funções complexas
4. **Git:** Commits semânticos (feat, fix, docs, etc.)

---

## Success Criteria

A migração será considerada bem-sucedida quando:

1. ✅ Todos os componentes funcionam no Next.js
2. ✅ JWT não está mais no localStorage
3. ✅ Autenticação funciona via cookies HttpOnly
4. ✅ Renovação de sessão é automática
5. ✅ Todos os testes passam
6. ✅ Deploy em produção é bem-sucedido
7. ✅ Nenhum bug crítico em 48h após deploy
8. ✅ Performance é igual ou melhor que antes
9. ✅ Usuários não percebem diferença na UX
10. ✅ Documentação está completa

---

## Out of Scope

O que NÃO será feito nesta migração:

❌ Mudanças no backend Clojure (apenas ajustes mínimos se necessário)
❌ Redesign da interface do usuário
❌ Novas funcionalidades além da migração
❌ Migração de banco de dados
❌ Mudanças em APIs existentes do backend
❌ Implementação de SSR para todas as páginas (apenas onde faz sentido)
❌ Otimizações de performance além do padrão Next.js

---

## Risks and Mitigations

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Incompatibilidade com backend | Média | Alto | Testes de integração extensivos |
| Bugs em produção | Média | Alto | Deploy gradual + rollback plan |
| Performance degradada | Baixa | Médio | Benchmarks antes e depois |
| Problemas com cookies | Baixa | Alto | Testes em múltiplos browsers |
| Custo de infraestrutura | Baixa | Baixo | Monitorar uso no Vercel/Render |

---

## Dependencies

### Externas
- Next.js 14.x
- React 18.x
- TypeScript 5.x
- Axios
- jose (JWT handling)

### Internas
- Backend Clojure (deve continuar funcionando)
- Banco de dados PostgreSQL (sem mudanças)

---

## Timeline Estimate

- **Fase 1 - Setup:** 2-3 horas
- **Fase 2 - Migração de Componentes:** 6-8 horas
- **Fase 3 - Implementação do BFF:** 4-6 horas
- **Fase 4 - Testes e Deploy:** 4-6 horas

**Total:** 16-23 horas (3-4 dias de trabalho)

---

**Documento criado em:** 10 de Outubro de 2025  
**Versão:** 1.0  
**Status:** Aguardando Aprovação
