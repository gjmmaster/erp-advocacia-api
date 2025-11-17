# 📖 Guia do Usuário - Sistema Jurídico Multi-Tenant

## 🎯 Visão Geral

Este sistema foi desenvolvido para gerenciar escritórios de advocacia de forma completa e profissional, oferecendo funcionalidades para gestão de processos, clientes, documentos e equipe.

---

## 👥 Tipos de Usuários

### 1. Super Admin
**Acesso:** `/super-admin/login`

**Responsabilidades:**
- Criar e gerenciar escritórios (tenants)
- Visualizar todos os dados do sistema
- Impersonar usuários para suporte
- Gerenciar configurações globais

### 2. Master (Administrador do Escritório)
**Acesso:** `/login`

**Responsabilidades:**
- Gerenciar usuários do escritório
- Gerenciar clientes
- Gerenciar processos
- Ver relatórios e estatísticas
- Configurar o escritório

### 3. Operador
**Acesso:** `/login`

**Responsabilidades:**
- Visualizar processos
- Adicionar movimentações
- Upload de documentos
- Gerenciar clientes atribuídos

---

## 🚀 Primeiros Passos

### 1. Primeiro Acesso (Super Admin)

1. Acesse `/super-admin/login`
2. Entre com as credenciais:
   - Email: `superadmin@example.com`
   - Senha: (configurada no ambiente)
3. Você será direcionado ao painel do Super Admin

### 2. Criando um Escritório

1. No painel do Super Admin, clique em **"+ Novo Tenant"**
2. Preencha os dados:
   - **Nome da Empresa:** Nome do escritório
   - **Subdomínio:** Identificador único (ex: silva)
   - **Email do Master:** Email do administrador
   - **Nome do Master:** Nome completo
3. Clique em **"Criar Tenant"**
4. **IMPORTANTE:** Anote a senha temporária gerada!
5. Envie as credenciais para o administrador do escritório

### 3. Primeiro Acesso (Escritório)

1. Acesse `/login`
2. Entre com o email fornecido
3. Use a senha temporária recebida
4. **Você será obrigado a trocar a senha** no primeiro acesso
5. Defina uma senha forte com:
   - Mínimo 8 caracteres
   - Letras maiúsculas e minúsculas
   - Números
   - Caracteres especiais

---

## 📊 Dashboard

### Visão Geral

O dashboard apresenta:
- **Estatísticas rápidas:** Processos, clientes, operadores
- **Processos recentes:** Últimas movimentações
- **Alertas:** Prazos próximos e ações pendentes
- **Gráficos:** Visualização de dados importantes

---

## 📁 Gestão de Processos

### Criando um Processo

1. Vá para **"Processos"** → **"+ Novo Processo"**
2. Preencha os dados obrigatórios:
   - Número do processo
   - Tipo (Trabalhista, Cível, etc.)
   - Cliente
   - Vara/Tribunal
   - Data de distribuição
3. Preencha dados opcionais:
   - Valor da causa
   - Descrição
   - Parte contrária
4. Clique em **"Salvar"**

### Editando um Processo

1. Clique no processo desejado
2. Clique em **"Editar"**
3. Modifique os campos necessários
4. Clique em **"Salvar Alterações"**

### Status de Processos

- **Em Andamento:** Processo ativo
- **Suspenso:** Temporariamente parado
- **Encerrado:** Finalizado
- **Arquivado:** Concluído e arquivado

### Adicionando Documentos

1. Abra o processo desejado
2. Vá para a aba **"Documentos"**
3. Clique em **"+ Upload de Documento"**
4. Selecione o arquivo (PDF, DOC, DOCX, JPG, PNG)
5. Escolha o tipo de documento
6. Adicione uma descrição (opcional)
7. Clique em **"Enviar"**

**Tamanho máximo:** 10MB por arquivo
**Formatos aceitos:** PDF, DOC, DOCX, JPG, PNG

### Registrando Movimentações

1. Abra o processo desejado
2. Vá para a aba **"Histórico"**
3. Clique em **"+ Nova Movimentação"**
4. Preencha:
   - Tipo (Audiência, Decisão, Petição, etc.)
   - Título
   - Descrição
   - Data do evento
5. Clique em **"Salvar"**

---

## 👥 Gestão de Clientes

### Cadastrando um Cliente

1. Vá para **"Clientes"** → **"+ Novo Cliente"**
2. Escolha o tipo:
   - **Pessoa Física:** CPF obrigatório
   - **Pessoa Jurídica:** CNPJ obrigatório
3. Preencha os dados:
   - Nome/Razão Social
   - CPF/CNPJ
   - Email
   - Telefone
   - Endereço completo
4. Clique em **"Salvar"**

### Visualizando Processos do Cliente

1. Clique no cliente desejado
2. Na página de detalhes, veja a lista de processos
3. Clique em um processo para ver detalhes

---

## 👤 Gestão de Usuários

*Disponível apenas para usuários Master*

### Criando um Usuário

1. Vá para **"Usuários"** → **"+ Novo Usuário"**
2. Preencha:
   - Email (único no sistema)
   - Nome completo
   - Função (Master ou Operador)
3. Clique em **"Criar Usuário"**
4. **IMPORTANTE:** Uma senha temporária será gerada
5. Anote a senha e envie para o novo usuário

### Editando um Usuário

1. Na lista de usuários, clique em **✏️ Editar**
2. Modifique os dados necessários
3. Clique em **"Salvar"**

### Resetando Senha

1. Na lista de usuários, clique em **🔑 Resetar Senha**
2. Confirme a ação
3. Uma nova senha temporária será gerada
4. Anote e envie para o usuário

### Desativando um Usuário

1. Na lista de usuários, clique em **🗑️ Desativar**
2. Confirme a ação
3. O usuário não poderá mais acessar o sistema

---

## 🔍 Busca

### Busca Global

1. Use a barra de busca no topo
2. Digite o termo desejado
3. Pressione Enter
4. Veja resultados de:
   - Processos
   - Clientes
   - Documentos

### Filtros

Use os filtros disponíveis em cada seção:
- **Processos:** Status, tipo, cliente, data
- **Clientes:** Tipo, nome
- **Documentos:** Tipo, data

---

## 🔐 Segurança

### Política de Senhas

- **Mínimo:** 8 caracteres
- **Recomendado:** 12+ caracteres
- **Deve conter:**
  - Letras maiúsculas (A-Z)
  - Letras minúsculas (a-z)
  - Números (0-9)
  - Caracteres especiais (!@#$%^&*)

### Boas Práticas

1. **Nunca compartilhe sua senha**
2. **Troque senhas temporárias imediatamente**
3. **Use senhas únicas** para cada sistema
4. **Faça logout** ao sair
5. **Não acesse** de computadores públicos

### Troca de Senha

1. Clique no seu nome (canto superior direito)
2. Vá para **"Perfil"**
3. Clique em **"Alterar Senha"**
4. Digite a senha atual
5. Digite a nova senha (2x)
6. Clique em **"Salvar"**

---

## 🎨 Interface

### Navegação

**Menu Lateral:**
- 📊 Dashboard
- 📁 Processos
- 👥 Clientes
- 👤 Usuários (apenas Master)
- ⚙️ Perfil

**Header:**
- 🔍 Busca global
- 🔔 Notificações
- 👤 Menu do usuário
- 🚪 Logout

### Atalhos de Teclado

| Atalho | Ação |
|--------|------|
| `Ctrl + K` | Busca rápida |
| `Ctrl + N` | Novo processo |
| `Ctrl + ,` | Configurações |
| `Esc` | Fechar modal |

---

## 📱 Responsividade

O sistema é totalmente responsivo e funciona em:
- 💻 Desktop (1920x1080+)
- 💻 Laptop (1366x768+)
- 📱 Tablet (768x1024+)
- 📱 Celular (375x667+)

---

## ❓ Perguntas Frequentes

### Como recupero minha senha?

Entre em contato com o administrador do seu escritório (usuário Master). Apenas ele pode resetar sua senha.

### Posso acessar de qualquer lugar?

Sim! O sistema é baseado na web e pode ser acessado de qualquer dispositivo com internet.

### Os dados são seguros?

Sim! Utilizamos:
- Criptografia SSL/TLS
- Senhas com hash bcrypt
- Autenticação JWT
- Backup automático diário

### Há limite de armazenamento?

Cada escritório tem um limite de 10GB para documentos. Entre em contato para aumentar.

### Posso exportar meus dados?

Sim! Use a funcionalidade de relatórios para exportar em PDF ou Excel.

### Como funciona o modo de impersonation?

Apenas o Super Admin pode usar este recurso para suporte técnico. Quando ativo, um banner laranja aparecerá indicando o modo.

---

## 🆘 Suporte

### Problemas Técnicos

1. Verifique sua conexão com a internet
2. Limpe o cache do navegador
3. Tente em outro navegador
4. Entre em contato com suporte

### Dúvidas sobre Funcionalidades

- Consulte este guia
- Assista aos vídeos tutoriais
- Entre em contato com o administrador

### Contato

- **Email:** suporte@legalerp.com
- **Telefone:** (11) 3000-0000
- **WhatsApp:** (11) 99000-0000
- **Horário:** Segunda a Sexta, 9h-18h

---

## 📚 Recursos Adicionais

- [Documentação da API](./API_DOCUMENTATION.md)
- [Roadmap de Implementação](./IMPLEMENTATION_ROADMAP.md)
- [Vídeos Tutoriais](#) - Em breve
- [Base de Conhecimento](#) - Em breve

---

## 📝 Changelog

### Versão 2.0.0 (17/11/2025)
- ✅ Gestão de usuários por tenant
- ✅ Sistema de documentos
- ✅ Histórico de processos
- ✅ Impersonation
- ✅ Troca de senha obrigatória

### Versão 1.0.0 (01/10/2025)
- ✅ Lançamento inicial
- ✅ CRUD de processos
- ✅ CRUD de clientes
- ✅ Autenticação JWT
- ✅ Multi-tenancy

---

**Última atualização:** 17 de Novembro de 2025
**Versão do Guia:** 2.0
