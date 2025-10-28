# Testes de Frontend - Autenticação de Tenants

**Data:** 28/10/2025  
**Status:** Pronto para Execução

---

## Overview

Este documento contém os testes manuais para validar o frontend Next.js relacionado à autenticação de tenants.

---

## Pré-requisitos

1. Backend rodando em `http://localhost:3000`
2. Frontend rodando em `http://localhost:3001`
3. Arquivo hosts configurado com subdomínios de teste
4. Tenants e usuários cadastrados no banco

---

## Setup de Hosts

### Windows
Editar `C:\Windows\System32\drivers\etc\hosts`:

```
127.0.0.1 escritorio-silva.localhost
127.0.0.1 escritorio-santos.localhost
127.0.0.1 escritorio-inativo.localhost
```

### Mac/Linux
Editar `/etc/hosts`:

```
127.0.0.1 escritorio-silva.localhost
127.0.0.1 escritorio-santos.localhost
127.0.0.1 escritorio-inativo.localhost
```

---

## Teste 1: Middleware - Extração de Subdomínio

### Caso 1.1: Acesso com Subdomínio Válido

**Ação:**
1. Abrir navegador
2. Acessar `http://escritorio-silva.localhost:3001/login`

**Resultado Esperado:**
- Página de login é exibida
- Não há redirecionamento
- Console do navegador não mostra erros
- DevTools > Network > Headers mostra:
  - `x-tenant-id: 1`
  - `x-tenant-subdomain: escritorio-silva`

**Status:** [ ] Passou

---

### Caso 1.2: Acesso sem Subdomínio

**Ação:**
1. Acessar `http://localhost:3001/login`

**Resultado Esperado:**
- Redireciona para página de erro ou super-admin
- Ou mostra mensagem "Subdomínio não encontrado"

**Status:** [ ] Passou

---

### Caso 1.3: Acesso com Subdomínio Inválido

**Ação:**
1. Adicionar ao hosts: `127.0.0.1 nao-existe.localhost`
2. Acessar `http://nao-existe.localhost:3001/login`

**Resultado Esperado:**
- Mostra erro "Escritório não encontrado"
- Status 404

**Status:** [ ] Passou

---

### Caso 1.4: Acesso com Tenant Inativo

**Ação:**
1. Acessar `http://escritorio-inativo.localhost:3001/login`

**Resultado Esperado:**
- Mostra erro "Escritório inativo"
- Status 403

**Status:** [ ] Passou

---

## Teste 2: Página de Login

### Caso 2.1: Renderização da Página

**Ação:**
1. Acessar `http://escritorio-silva.localhost:3001/login`

**Resultado Esperado:**
- Formulário de login é exibido
- Campos visíveis:
  - Email (input type="email")
  - Senha (input type="password")
  - Botão "Entrar"
- Nome do escritório aparece no topo
- Design responsivo funciona

**Status:** [ ] Passou

---

### Caso 2.2: Validação Client-Side

**Ação:**
1. Clicar em "Entrar" sem preencher campos

**Resultado Esperado:**
- Validação HTML5 impede submit
- Mensagens de erro aparecem

**Ação:**
2. Preencher email inválido (ex: "teste")
3. Clicar em "Entrar"

**Resultado Esperado:**
- Validação de email impede submit

**Status:** [ ] Passou

---

### Caso 2.3: Login Bem-Sucedido

**Ação:**
1. Preencher:
   - Email: `admin@silva.com`
   - Senha: `test123`
2. Clicar em "Entrar"

**Resultado Esperado:**
- Botão mostra estado de loading
- Após ~1s, redireciona para `/dashboard`
- Cookie `access_token` é criado (HttpOnly)
- Não há erros no console

**Status:** [ ] Passou

---

### Caso 2.4: Login com Credenciais Inválidas

**Ação:**
1. Preencher:
   - Email: `admin@silva.com`
   - Senha: `senha-errada`
2. Clicar em "Entrar"

**Resultado Esperado:**
- Mensagem de erro aparece: "Credenciais inválidas"
- Não redireciona
- Campos permanecem preenchidos
- Botão volta ao estado normal

**Status:** [ ] Passou

---

### Caso 2.5: Login de Usuário de Outro Tenant

**Ação:**
1. Acessar `http://escritorio-silva.localhost:3001/login`
2. Preencher:
   - Email: `admin@santos.com` (usuário do tenant 2)
   - Senha: `test123`
3. Clicar em "Entrar"

**Resultado Esperado:**
- Mensagem de erro: "Usuário não pertence a este escritório"
- Não redireciona

**Status:** [ ] Passou

---

### Caso 2.6: Estado de Loading

**Ação:**
1. Preencher credenciais válidas
2. Clicar em "Entrar"
3. Observar botão durante requisição

**Resultado Esperado:**
- Botão fica desabilitado
- Texto muda para "Entrando..." ou spinner aparece
- Campos ficam desabilitados

**Status:** [ ] Passou

---

## Teste 3: Dashboard

### Caso 3.1: Acesso Autenticado

**Ação:**
1. Fazer login com sucesso
2. Verificar dashboard em `http://escritorio-silva.localhost:3001/dashboard`

**Resultado Esperado:**
- Dashboard é exibido
- Layout com sidebar/header aparece
- Nome do escritório aparece no header
- Menu de navegação está visível
- Componente de estatísticas é exibido

**Status:** [ ] Passou

---

### Caso 3.2: Acesso Não Autenticado

**Ação:**
1. Limpar cookies
2. Acessar `http://escritorio-silva.localhost:3001/dashboard`

**Resultado Esperado:**
- Redireciona para `/login`
- Não mostra conteúdo do dashboard

**Status:** [ ] Passou

---

### Caso 3.3: Carregamento de Estatísticas

**Ação:**
1. Fazer login
2. Observar área de estatísticas no dashboard

**Resultado Esperado:**
- Skeleton loaders aparecem primeiro
- Após ~1s, cards com números aparecem:
  - Total de Processos
  - Processos Ativos
  - Total de Clientes
  - Total de Operadores
- Números são válidos (>= 0)

**Status:** [ ] Passou

---

### Caso 3.4: Erro ao Carregar Estatísticas

**Ação:**
1. Parar o backend
2. Fazer login (usar token válido existente)
3. Acessar dashboard

**Resultado Esperado:**
- Mensagem de erro aparece
- Botão "Tentar novamente" é exibido
- Clicar no botão tenta recarregar

**Status:** [ ] Passou

---

### Caso 3.5: Menu de Navegação

**Ação:**
1. Fazer login como admin (role: master)
2. Verificar menu lateral

**Resultado Esperado:**
- Menu mostra:
  - Dashboard (ativo)
  - Processos (desabilitado, "Em breve")
  - Clientes (desabilitado, "Em breve")
  - Operadores (desabilitado, "Em breve")

**Ação:**
2. Fazer login como operador (role: operator)

**Resultado Esperado:**
- Menu NÃO mostra item "Operadores"

**Status:** [ ] Passou

---

## Teste 4: Logout

### Caso 4.1: Logout Bem-Sucedido

**Ação:**
1. Fazer login
2. Clicar no botão "Sair" no menu

**Resultado Esperado:**
- Redireciona para `/login`
- Cookie `access_token` é removido
- Tentar acessar `/dashboard` redireciona para login

**Status:** [ ] Passou

---

### Caso 4.2: Logout com Erro

**Ação:**
1. Fazer login
2. Parar o backend
3. Clicar em "Sair"

**Resultado Esperado:**
- Mesmo com erro, redireciona para login
- Cookie é removido localmente

**Status:** [ ] Passou

---

## Teste 5: Proteção de Rotas

### Caso 5.1: Acesso a Rota Protegida sem Autenticação

**Ação:**
1. Limpar cookies
2. Acessar `http://escritorio-silva.localhost:3001/dashboard`

**Resultado Esperado:**
- Redireciona para `/login`

**Status:** [ ] Passou

---

### Caso 5.2: Acesso com Token de Outro Tenant

**Ação:**
1. Fazer login em `escritorio-silva.localhost:3001`
2. Copiar cookie `access_token`
3. Abrir nova aba anônima
4. Acessar `escritorio-santos.localhost:3001/dashboard`
5. Adicionar cookie do tenant 1 manualmente

**Resultado Esperado:**
- Middleware detecta incompatibilidade
- Redireciona para login
- Ou mostra erro 403

**Status:** [ ] Passou

---

### Caso 5.3: Acesso com Token Expirado

**Ação:**
1. Fazer login
2. Aguardar 15 minutos (expiração do token)
3. Tentar acessar dashboard ou fazer requisição

**Resultado Esperado:**
- Redireciona para login
- Mensagem "Sessão expirada"

**Status:** [ ] Passou

---

## Teste 6: Responsividade

### Caso 6.1: Desktop (> 768px)

**Ação:**
1. Acessar em tela desktop
2. Fazer login e navegar

**Resultado Esperado:**
- Sidebar sempre visível
- Layout em 2 colunas
- Todos os elementos bem posicionados

**Status:** [ ] Passou

---

### Caso 6.2: Mobile (< 768px)

**Ação:**
1. Redimensionar para mobile (375px)
2. Fazer login e navegar

**Resultado Esperado:**
- Sidebar escondida por padrão
- Botão de menu (☰) aparece
- Clicar no botão abre sidebar
- Overlay escurece fundo
- Clicar no overlay fecha sidebar

**Status:** [ ] Passou

---

### Caso 6.3: Tablet (768px - 1024px)

**Ação:**
1. Redimensionar para tablet
2. Verificar layout

**Resultado Esperado:**
- Layout se adapta corretamente
- Cards de estatísticas em grid responsivo

**Status:** [ ] Passou

---

## Teste 7: Integração Completa

### Caso 7.1: Fluxo Completo - Tenant 1

**Ação:**
1. Acessar `http://escritorio-silva.localhost:3001`
2. Redireciona para `/login`
3. Fazer login com `admin@silva.com`
4. Verificar dashboard
5. Verificar estatísticas
6. Fazer logout
7. Verificar redirecionamento

**Resultado Esperado:**
- Todos os passos funcionam sem erros
- Dados corretos são exibidos

**Status:** [ ] Passou

---

### Caso 7.2: Fluxo Completo - Tenant 2

**Ação:**
1. Acessar `http://escritorio-santos.localhost:3001`
2. Fazer login com `admin@santos.com`
3. Verificar que dados são do tenant 2

**Resultado Esperado:**
- Isolamento de dados funciona
- Estatísticas mostram dados apenas do tenant 2

**Status:** [ ] Passou

---

### Caso 7.3: Troca de Tenant

**Ação:**
1. Fazer login no tenant 1
2. Abrir nova aba
3. Acessar tenant 2
4. Verificar que precisa fazer novo login

**Resultado Esperado:**
- Cada tenant tem sessão independente
- Não há vazamento de autenticação

**Status:** [ ] Passou

---

## Teste 8: Segurança

### Caso 8.1: Cookies HttpOnly

**Ação:**
1. Fazer login
2. Abrir DevTools > Application > Cookies
3. Verificar cookie `access_token`

**Resultado Esperado:**
- Cookie tem flag `HttpOnly: true`
- Cookie tem flag `Secure: true` (em produção)
- Cookie tem `SameSite: Lax`

**Status:** [ ] Passou

---

### Caso 8.2: Token não Exposto no JavaScript

**Ação:**
1. Fazer login
2. Abrir console
3. Tentar acessar: `document.cookie`

**Resultado Esperado:**
- Token não aparece na string de cookies
- JavaScript não consegue ler o token

**Status:** [ ] Passou

---

### Caso 8.3: XSS Protection

**Ação:**
1. Tentar injetar script no campo de email:
   ```
   <script>alert('XSS')</script>
   ```
2. Submeter formulário

**Resultado Esperado:**
- Script não é executado
- Input é sanitizado

**Status:** [ ] Passou

---

## Teste 9: Performance

### Caso 9.1: Tempo de Carregamento

**Ação:**
1. Fazer login
2. Medir tempo até dashboard completo

**Resultado Esperado:**
- Login completa em < 2s
- Dashboard carrega em < 2s
- Estatísticas aparecem em < 1s

**Status:** [ ] Passou

---

### Caso 9.2: Tamanho dos Bundles

**Ação:**
1. Abrir DevTools > Network
2. Carregar página de login
3. Verificar tamanho dos arquivos JS/CSS

**Resultado Esperado:**
- Bundle principal < 500KB
- CSS < 50KB
- Imagens otimizadas

**Status:** [ ] Passou

---

## Teste 10: Acessibilidade

### Caso 10.1: Navegação por Teclado

**Ação:**
1. Acessar login
2. Usar apenas Tab e Enter para navegar e submeter

**Resultado Esperado:**
- Todos os elementos são acessíveis
- Ordem de foco faz sentido
- Enter submete o formulário

**Status:** [ ] Passou

---

### Caso 10.2: Screen Reader

**Ação:**
1. Ativar screen reader
2. Navegar pela página

**Resultado Esperado:**
- Labels são lidos corretamente
- Erros são anunciados
- Botões têm aria-labels

**Status:** [ ] Passou

---

## Checklist de Validação

### Middleware
- [ ] Extrai subdomínio corretamente
- [ ] Valida tenant no backend
- [ ] Adiciona headers corretos
- [ ] Trata erros apropriadamente

### Página de Login
- [ ] Renderiza corretamente
- [ ] Validação client-side funciona
- [ ] Login bem-sucedido redireciona
- [ ] Erros são exibidos corretamente
- [ ] Estado de loading funciona

### Dashboard
- [ ] Acesso autenticado funciona
- [ ] Acesso não autenticado redireciona
- [ ] Estatísticas carregam corretamente
- [ ] Erros são tratados
- [ ] Menu de navegação funciona

### Logout
- [ ] Logout bem-sucedido funciona
- [ ] Cookie é removido
- [ ] Redireciona para login

### Proteção de Rotas
- [ ] Rotas protegidas requerem autenticação
- [ ] Token de outro tenant é rejeitado
- [ ] Token expirado é rejeitado

### Responsividade
- [ ] Desktop funciona
- [ ] Mobile funciona
- [ ] Tablet funciona

### Segurança
- [ ] Cookies são HttpOnly
- [ ] Token não é acessível via JS
- [ ] XSS protection funciona

### Performance
- [ ] Carregamento é rápido
- [ ] Bundles são otimizados

### Acessibilidade
- [ ] Navegação por teclado funciona
- [ ] Screen reader funciona

---

## Resultados dos Testes

### Resumo
- Total de casos de teste: 35
- Casos passados: [ ]
- Casos falhados: [ ]
- Taxa de sucesso: [ ]%

### Problemas Encontrados
1. [ ] Nenhum problema encontrado
2. [ ] Listar problemas aqui...

---

## Notas

- Executar testes em diferentes navegadores (Chrome, Firefox, Safari)
- Testar em diferentes resoluções
- Documentar qualquer comportamento inesperado
- Atualizar este documento conforme necessário

---

**Documento criado em:** 28/10/2025  
**Última atualização:** 28/10/2025  
**Status:** Pronto para Execução
