# Spec: Autenticação e Dashboard de Tenants

**Data de Criação:** 24 de Outubro de 2025  
**Status:** Pronto para Implementação  
**Tempo Estimado:** 6-7 dias

---

## 📋 Visão Geral

Esta spec define a implementação do sistema de autenticação e dashboard para os escritórios de advocacia (tenants). É a primeira fase do sistema voltado para usuários finais, permitindo que admins e operadores acessem o sistema através de seus subdomínios específicos.

---

## 🎯 Objetivos

1. ✅ Permitir acesso via subdomínio (ex: `escritorio-silva.seudominio.com`)
2. ✅ Implementar login seguro com validação de tenant
3. ✅ Criar dashboard com métricas básicas
4. ✅ Diferenciar permissões entre admin e operador
5. ✅ Garantir segurança com cookies HttpOnly e validação de tenant

---

## 📚 Documentos

### 1. [requirements.md](./requirements.md)
**O que:** Requisitos funcionais e não-funcionais  
**Quando ler:** Antes de começar a implementação  
**Conteúdo:**
- 8 requisitos principais com acceptance criteria
- Requisitos não-funcionais (segurança, performance, usabilidade)
- Fora do escopo desta fase

### 2. [design.md](./design.md)
**O que:** Arquitetura técnica detalhada  
**Quando ler:** Antes de implementar cada componente  
**Conteúdo:**
- Fluxo geral da aplicação
- Arquitetura de componentes
- Código de exemplo para cada parte
- Data models
- Estratégia de testes

### 3. [tasks.md](./tasks.md)
**O que:** Lista de tarefas de implementação  
**Quando ler:** Durante a implementação  
**Conteúdo:**
- 18 tasks organizadas em 7 fases
- Ordem de execução recomendada
- Tempo estimado por fase
- Checklist de conclusão

---

## 🚀 Como Começar

### 1. Ler Documentação
```bash
# Ordem recomendada:
1. README.md (este arquivo)
2. requirements.md
3. design.md
4. tasks.md
```

### 2. Configurar Ambiente Local

**Arquivo hosts (para testar subdomínios):**

Windows: `C:\Windows\System32\drivers\etc\hosts`
```
127.0.0.1 escritorio-silva.localhost
127.0.0.1 escritorio-santos.localhost
```

Mac/Linux: `/etc/hosts`
```
127.0.0.1 escritorio-silva.localhost
127.0.0.1 escritorio-santos.localhost
```

**Variáveis de ambiente:**

Backend (`.env`):
```bash
JWT_SECRET=seu-secret-forte-aqui
DATABASE_URL=postgresql://...
```

Frontend (`frontend-nextjs/.env.local`):
```bash
NEXT_PUBLIC_DOMAIN=localhost
BACKEND_URL=http://localhost:3000
JWT_SECRET=seu-secret-forte-aqui
```

### 3. Criar Tenant de Teste

```bash
# Usar o super admin para criar um tenant
curl -X POST http://localhost:3000/admin/provision-tenant \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <super-admin-token>" \
  -d '{
    "name": "Escritório Silva",
    "subdomain": "escritorio-silva",
    "admin_email": "admin@silva.com",
    "operator_limit": 5
  }'
```

### 4. Iniciar Implementação

Siga a ordem das tasks em `tasks.md`:

**Fase 1: Backend (1 dia)**
- Task 1: Endpoint de busca de tenant
- Task 2: Atualizar login
- Task 3: Endpoint de stats

**Fase 2: Frontend - Infraestrutura (0.5 dia)**
- Task 4: Middleware
- Task 5: Tipos TypeScript

**Fase 3: Frontend - Login (1 dia)**
- Task 6: Página de login
- Task 7: BFF API route

E assim por diante...

---

## 📊 Progresso

### Status Atual
- [ ] Requirements aprovados
- [ ] Design aprovado
- [ ] Tasks criadas
- [ ] Implementação iniciada
- [ ] Testes passando
- [ ] Deploy em produção

### Fases
- [ ] Fase 1: Backend (0/3 tasks)
- [ ] Fase 2: Frontend - Infraestrutura (0/2 tasks)
- [ ] Fase 3: Frontend - Login (0/2 tasks)
- [ ] Fase 4: Frontend - Dashboard (0/4 tasks)
- [ ] Fase 5: Frontend - Logout e Proteção (0/2 tasks)
- [ ] Fase 6: Testes (0/3 tasks)
- [ ] Fase 7: Documentação e Deploy (0/2 tasks)

**Total:** 0/18 tasks completas (0%)

---

## 🎯 Entregas

Ao final desta spec, você terá:

### Backend
- ✅ Endpoint para buscar tenant por subdomínio
- ✅ Login validando tenant + credenciais
- ✅ Endpoint de estatísticas do dashboard
- ✅ Validação de tenant-id em cada requisição

### Frontend
- ✅ Middleware para extrair e validar subdomínio
- ✅ Página de login do tenant
- ✅ Dashboard com 4 métricas
- ✅ Layout com menu de navegação
- ✅ Funcionalidade de logout
- ✅ Proteção de rotas

### Segurança
- ✅ Cookies HttpOnly
- ✅ Validação de tenant em cada requisição
- ✅ Diferenciação de permissões (admin vs operador)

---

## 🧪 Como Testar

### Teste Manual

1. **Acesso via subdomínio:**
   ```
   http://escritorio-silva.localhost:3001/login
   ```

2. **Login:**
   - E-mail: admin@silva.com
   - Senha: (senha criada no provisionamento)

3. **Dashboard:**
   - Verificar se métricas aparecem
   - Verificar se menu está correto
   - Verificar se nome do escritório aparece

4. **Logout:**
   - Clicar em "Sair"
   - Verificar redirecionamento para login

5. **Proteção de rotas:**
   - Tentar acessar `/dashboard` sem login
   - Deve redirecionar para `/login`

### Testes Automatizados

```bash
# Backend
cd backend
lein test

# Frontend
cd frontend-nextjs
npm test
```

---

## 🐛 Troubleshooting

### Problema: Subdomínio não funciona localmente

**Solução:**
1. Verificar arquivo hosts
2. Reiniciar navegador
3. Limpar cache DNS: `ipconfig /flushdns` (Windows) ou `sudo dscacheutil -flushcache` (Mac)

### Problema: Erro 404 ao buscar tenant

**Solução:**
1. Verificar se tenant existe no banco
2. Verificar se subdomain está correto
3. Verificar logs do backend

### Problema: Login não funciona

**Solução:**
1. Verificar se JWT_SECRET é o mesmo no backend e frontend
2. Verificar se usuário pertence ao tenant correto
3. Verificar logs do backend e frontend

### Problema: Dashboard não carrega

**Solução:**
1. Verificar se token está sendo enviado
2. Verificar se tenant-id do token corresponde ao subdomínio
3. Verificar logs do BFF

---

## 📞 Suporte

### Documentação Relacionada
- `SECURITY_ANALYSIS.md` - Análise de segurança do sistema
- `frontend-nextjs/README.md` - Documentação do frontend
- `docs/status.md` - Status geral do projeto

### Próximas Specs
Após concluir esta spec, as próximas fases serão:
1. **Recuperação e Alteração de Senha** (1 semana)
2. **CRUD de Processos Jurídicos** (2 semanas)
3. **CRUD de Clientes** (1 semana)
4. **Gestão de Operadores** (3 dias)
5. **Gestão de Documentos** (1 semana)

---

## ✅ Critérios de Aceitação

Esta spec será considerada completa quando:

1. ✅ Usuário pode acessar via subdomínio
2. ✅ Usuário pode fazer login com e-mail e senha
3. ✅ Sistema valida que usuário pertence ao tenant correto
4. ✅ Dashboard exibe 4 métricas básicas
5. ✅ Menu de navegação está funcional
6. ✅ Usuário pode fazer logout
7. ✅ Rotas estão protegidas por autenticação
8. ✅ Sistema diferencia entre admin e operador
9. ✅ Interface é responsiva
10. ✅ Todos os testes passam
11. ✅ Sistema funciona em produção
12. ✅ Documentação está completa

---

**Boa sorte com a implementação! 🚀**

---

**Criado em:** 24/10/2025  
**Última atualização:** 24/10/2025  
**Versão:** 1.0
