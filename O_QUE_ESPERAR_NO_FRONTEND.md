# 👀 O Que Esperar no Frontend Após Migrations

**Data:** 04/11/2025

---

## 🎯 Fluxo Completo de Teste

### 1️⃣ Antes das Migrations

**O que você verá:**
- ✅ Sistema carrega normalmente
- ✅ Login funciona
- ✅ Dashboard aparece
- ✅ Menu lateral com "Processos" e "Clientes"
- ⚠️ Ao clicar em "Clientes": Mensagem "Nenhum cliente cadastrado"
- ⚠️ Ao clicar em "Processos": Mensagem "Nenhum processo cadastrado"

**Por quê?** As tabelas ainda não existem no banco, então as queries retornam arrays vazios.

---

### 2️⃣ Depois das Migrations

**O que você verá:**

#### A. Página de Clientes (`/dashboard/clientes`)

**Primeira vez (sem dados):**
```
┌─────────────────────────────────────────────────┐
│  Clientes                    [+ Novo Cliente]   │
├─────────────────────────────────────────────────┤
│                                                 │
│  [🔍 Buscar por nome, CPF/CNPJ ou email...]    │
│                                                 │
├─────────────────────────────────────────────────┤
│                                                 │
│              ┌─────────────────┐                │
│              │  📋             │                │
│              │                 │                │
│              │  Nenhum cliente │                │
│              │   cadastrado    │                │
│              │                 │                │
│              │ [Cadastrar      │                │
│              │  Primeiro       │                │
│              │  Cliente]       │                │
│              └─────────────────┘                │
│                                                 │
└─────────────────────────────────────────────────┘
```

**Depois de criar clientes:**
```
┌─────────────────────────────────────────────────────────────────────┐
│  Clientes                                      [+ Novo Cliente]     │
├─────────────────────────────────────────────────────────────────────┤
│  [🔍 Buscar...]  [Buscar]  [Limpar]                                │
├─────────────────────────────────────────────────────────────────────┤
│ Nome          │ CPF/CNPJ        │ Email           │ Telefone │ ... │
├───────────────┼─────────────────┼─────────────────┼──────────┼─────┤
│ João Silva    │ 123.456.789-00  │ joao@email.com  │ (11) ... │ 👁️✏️🗑️│
│ Maria Santos  │ 987.654.321-00  │ maria@email.com │ (21) ... │ 👁️✏️🗑️│
│ Empresa XYZ   │ 12.345.678/0001 │ xyz@empresa.com │ (11) ... │ 👁️✏️🗑️│
└─────────────────────────────────────────────────────────────────────┘
│ [← Anterior]  Página 1 • Total: 3 cliente(s)  [Próxima →]         │
└─────────────────────────────────────────────────────────────────────┘
```

#### B. Formulário de Novo Cliente (`/dashboard/clientes/novo`)

```
┌─────────────────────────────────────────────────┐
│  Novo Cliente                      [← Voltar]   │
├─────────────────────────────────────────────────┤
│                                                 │
│  Nome *                                         │
│  [_____________________________________]        │
│                                                 │
│  CPF/CNPJ              Telefone                 │
│  [________________]    [________________]       │
│                                                 │
│  Email                                          │
│  [_____________________________________]        │
│                                                 │
│  Endereço                                       │
│  [_____________________________________]        │
│  [_____________________________________]        │
│  [_____________________________________]        │
│                                                 │
│              [Cancelar]  [Salvar Cliente]       │
└─────────────────────────────────────────────────┘
```

**Ao clicar em "Salvar Cliente":**
- ✅ Mensagem: "Cliente criado com sucesso!"
- ✅ Redireciona para `/dashboard/clientes`
- ✅ Cliente aparece na listagem

#### C. Página de Processos (`/dashboard/processos`)

**Primeira vez (sem dados):**
```
┌─────────────────────────────────────────────────┐
│  Processos Jurídicos           [+ Novo Processo]│
├─────────────────────────────────────────────────┤
│  [🔍 Buscar...]  [Buscar]  [Todos os status ▼] │
├─────────────────────────────────────────────────┤
│                                                 │
│              ┌─────────────────┐                │
│              │  📁             │                │
│              │                 │                │
│              │  Nenhum processo│                │
│              │   cadastrado    │                │
│              │                 │                │
│              │ [Cadastrar      │                │
│              │  Primeiro       │                │
│              │  Processo]      │                │
│              └─────────────────┘                │
│                                                 │
└─────────────────────────────────────────────────┘
```

**Depois de criar processos:**
```
┌──────────────────────────────────────────────────────────────────────────┐
│  Processos Jurídicos                              [+ Novo Processo]      │
├──────────────────────────────────────────────────────────────────────────┤
│  [🔍 Buscar...]  [Buscar]  [Limpar]  [Todos os status ▼]               │
├──────────────────────────────────────────────────────────────────────────┤
│ Número        │ Cliente    │ Tipo    │ Status        │ Valor    │ ... │
├───────────────┼────────────┼─────────┼───────────────┼──────────┼─────┤
│ 0001234-56... │ João Silva │ Cível   │ Em Andamento  │ R$ 50k   │ 👁️✏️🗑️│
│ 0007890-12... │ Maria S.   │ Trabalh.│ Suspenso      │ R$ 30k   │ 👁️✏️🗑️│
│ 0005678-90... │ Empresa XYZ│ Criminal│ Arquivado     │ -        │ 👁️✏️🗑️│
└──────────────────────────────────────────────────────────────────────────┘
│ [← Anterior]  Página 1 • Total: 3 processo(s)  [Próxima →]             │
└──────────────────────────────────────────────────────────────────────────┘
```

**Badges de Status (coloridos):**
- 🔵 **Em Andamento** - Azul
- 🟠 **Suspenso** - Laranja
- ⚪ **Arquivado** - Cinza
- 🟢 **Finalizado** - Verde

#### D. Formulário de Novo Processo (`/dashboard/processos/novo`)

```
┌─────────────────────────────────────────────────┐
│  Novo Processo                     [← Voltar]   │
├─────────────────────────────────────────────────┤
│  Informações Básicas                            │
│                                                 │
│  Número do Processo *      Cliente *            │
│  [________________]        [João Silva      ▼] │
│                                                 │
│  Tipo *                    Status               │
│  [________________]        [Em Andamento    ▼] │
│                                                 │
├─────────────────────────────────────────────────┤
│  Localização                                    │
│                                                 │
│  Vara/Tribunal                                  │
│  [_____________________________________]        │
│                                                 │
│  Comarca                   UF                   │
│  [________________]        [__]                 │
│                                                 │
├─────────────────────────────────────────────────┤
│  Valores e Datas                                │
│                                                 │
│  Valor da Causa (R$)       Data de Distribuição│
│  [________________]        [__/__/____]         │
│                                                 │
├─────────────────────────────────────────────────┤
│  Detalhes                                       │
│                                                 │
│  Descrição                                      │
│  [_____________________________________]        │
│  [_____________________________________]        │
│                                                 │
│  Observações                                    │
│  [_____________________________________]        │
│  [_____________________________________]        │
│                                                 │
│              [Cancelar]  [Salvar Processo]      │
└─────────────────────────────────────────────────┘
```

**Ao clicar em "Salvar Processo":**
- ✅ Mensagem: "Processo criado com sucesso!"
- ✅ Redireciona para `/dashboard/processos`
- ✅ Processo aparece na listagem

---

## 🎬 Cenário de Teste Completo

### Passo 1: Criar Primeiro Cliente

1. Faça login no sistema
2. Clique em "Clientes" no menu lateral
3. Veja a mensagem "Nenhum cliente cadastrado"
4. Clique em "[+ Novo Cliente]"
5. Preencha:
   - Nome: "João Silva"
   - CPF/CNPJ: "123.456.789-00"
   - Email: "joao@example.com"
   - Telefone: "(11) 98765-4321"
6. Clique em "Salvar Cliente"
7. ✅ Veja o alerta: "Cliente criado com sucesso!"
8. ✅ Veja João Silva na listagem

### Passo 2: Criar Segundo Cliente

1. Clique em "[+ Novo Cliente]" novamente
2. Preencha:
   - Nome: "Maria Santos"
   - CPF/CNPJ: "987.654.321-00"
   - Email: "maria@example.com"
3. Clique em "Salvar Cliente"
4. ✅ Veja Maria Santos na listagem

### Passo 3: Criar Primeiro Processo

1. Clique em "Processos" no menu lateral
2. Veja a mensagem "Nenhum processo cadastrado"
3. Clique em "[+ Novo Processo]"
4. Preencha:
   - Número: "0001234-56.2025.8.26.0100"
   - Cliente: Selecione "João Silva"
   - Tipo: "Cível"
   - Status: "Em Andamento"
   - Vara/Tribunal: "1ª Vara Cível"
   - Valor da Causa: "50000"
   - Descrição: "Ação de cobrança de valores"
5. Clique em "Salvar Processo"
6. ✅ Veja o alerta: "Processo criado com sucesso!"
7. ✅ Veja o processo na listagem com badge azul "Em Andamento"

### Passo 4: Testar Busca

1. Na página de processos, digite "cobrança" na busca
2. Clique em "Buscar"
3. ✅ Veja apenas o processo que contém "cobrança"
4. Clique em "Limpar"
5. ✅ Veja todos os processos novamente

### Passo 5: Testar Filtros

1. No dropdown "Todos os status", selecione "Em Andamento"
2. ✅ Veja apenas processos com status "Em Andamento"
3. Selecione "Todos os status"
4. ✅ Veja todos os processos novamente

### Passo 6: Testar Deletar

1. Clique no ícone 🗑️ de um processo
2. ✅ Veja a confirmação: "Tem certeza que deseja deletar este processo?"
3. Clique em "OK"
4. ✅ Veja o alerta: "Processo deletado com sucesso!"
5. ✅ Processo desaparece da listagem

---

## 🎨 Detalhes Visuais

### Cores e Estilos

**Botões:**
- Primário (Criar): Azul (#4F46E5) com hover
- Secundário (Cancelar): Cinza com borda
- Ações (👁️✏️🗑️): Hover com fundo colorido

**Tabelas:**
- Fundo: Branco/Cinza claro (dark mode)
- Hover: Cinza mais escuro
- Bordas: Sutis

**Formulários:**
- Inputs: Bordas arredondadas
- Focus: Borda azul
- Labels: Negrito

**Badges de Status:**
- Em Andamento: Azul claro (#e3f2fd) com texto azul
- Suspenso: Laranja claro (#fff3e0) com texto laranja
- Arquivado: Cinza claro (#f5f5f5) com texto cinza
- Finalizado: Verde claro (#e8f5e9) com texto verde

### Responsividade

**Desktop (> 1024px):**
- Sidebar fixa à esquerda
- Tabelas com todas as colunas
- Formulários em 2 colunas

**Tablet (768-1023px):**
- Sidebar colapsável
- Tabelas com scroll horizontal
- Formulários em 2 colunas

**Mobile (< 768px):**
- Sidebar como drawer
- Tabelas com scroll horizontal
- Formulários em 1 coluna
- Botões full-width

---

## ⚠️ Possíveis Problemas e Soluções

### Problema 1: "Nenhum cliente cadastrado" mesmo após criar

**Causa:** Migrations não foram aplicadas ou erro no backend

**Solução:**
1. Verifique se migrations foram aplicadas: `SHOW TABLES;` no SQL
2. Verifique logs do backend no Render
3. Teste a API diretamente: `curl /api/tenant/clientes`

### Problema 2: Erro ao criar cliente/processo

**Causa:** Validação falhou ou problema de conexão

**Solução:**
1. Abra o console do navegador (F12)
2. Veja a mensagem de erro
3. Verifique se todos os campos obrigatórios estão preenchidos
4. Verifique logs do backend

### Problema 3: Dropdown de clientes vazio ao criar processo

**Causa:** Nenhum cliente foi criado ainda

**Solução:**
1. Crie pelo menos um cliente primeiro
2. Volte para criar processo
3. Cliente aparecerá no dropdown

### Problema 4: Página em branco ou erro 500

**Causa:** Erro no backend ou migrations não aplicadas

**Solução:**
1. Verifique logs do backend no Render
2. Verifique se DATABASE_URL está correta
3. Verifique se migrations foram aplicadas
4. Tente fazer logout e login novamente

---

## ✅ Checklist de Validação

Após aplicar migrations, teste:

- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] Menu "Clientes" aparece
- [ ] Menu "Processos" aparece
- [ ] Página de clientes carrega (vazia)
- [ ] Botão "+ Novo Cliente" funciona
- [ ] Formulário de cliente aparece
- [ ] Consegue criar cliente
- [ ] Cliente aparece na listagem
- [ ] Busca de clientes funciona
- [ ] Página de processos carrega (vazia)
- [ ] Botão "+ Novo Processo" funciona
- [ ] Formulário de processo aparece
- [ ] Dropdown de clientes tem opções
- [ ] Consegue criar processo
- [ ] Processo aparece na listagem
- [ ] Badges de status aparecem coloridos
- [ ] Busca de processos funciona
- [ ] Filtro de status funciona
- [ ] Consegue deletar cliente/processo
- [ ] Paginação funciona (se > 20 itens)

---

## 🎉 Sucesso!

Se tudo acima funcionar, você tem um **sistema completo e funcional**!

**Próximos passos:**
- Criar mais clientes e processos
- Testar busca e filtros
- Testar em diferentes dispositivos
- Implementar páginas de detalhes e edição

---

**Criado em:** 04/11/2025  
**Status:** Guia completo de teste  
**Tempo de teste:** ~15 minutos

