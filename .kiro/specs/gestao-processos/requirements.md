# Requirements - Gestão de Processos Jurídicos

## Introdução

Este documento define os requisitos para o sistema de gestão de processos jurídicos do Legal ERP. O sistema permitirá que escritórios de advocacia gerenciem seus processos de forma eficiente, com controle de documentos, prazos, e vinculação com clientes.

## Objetivos

- Permitir cadastro e gestão completa de processos jurídicos
- Facilitar o acompanhamento de prazos e movimentações
- Organizar documentos relacionados aos processos
- Vincular processos a clientes e operadores
- Fornecer busca e filtros avançados
- Manter histórico completo de alterações

---

## Requisitos Funcionais

### Requisito 1: Cadastro de Processos

**User Story:** Como um operador do escritório, eu quero cadastrar novos processos jurídicos, para que eu possa gerenciar todos os casos do escritório em um único lugar.

#### Acceptance Criteria

1. WHEN o operador acessa a página de processos THEN o sistema SHALL exibir um botão "Novo Processo"
2. WHEN o operador clica em "Novo Processo" THEN o sistema SHALL exibir um formulário com os campos obrigatórios
3. WHEN o operador preenche os campos obrigatórios (número do processo, cliente, tipo, vara/tribunal) THEN o sistema SHALL validar o formato do número do processo
4. WHEN o número do processo já existe no tenant THEN o sistema SHALL exibir mensagem de erro "Processo já cadastrado"
5. WHEN todos os campos são válidos e o operador clica em "Salvar" THEN o sistema SHALL criar o processo e exibir mensagem de sucesso
6. WHEN o processo é criado THEN o sistema SHALL registrar data de criação, operador responsável e tenant_id
7. WHEN o processo é criado THEN o sistema SHALL redirecionar para a página de detalhes do processo

### Requisito 2: Listagem de Processos

**User Story:** Como um operador do escritório, eu quero visualizar todos os processos cadastrados, para que eu possa ter uma visão geral dos casos em andamento.

#### Acceptance Criteria

1. WHEN o operador acessa a página de processos THEN o sistema SHALL exibir uma lista paginada de processos
2. WHEN a lista é exibida THEN o sistema SHALL mostrar: número do processo, cliente, tipo, status, data de cadastro
3. WHEN há mais de 20 processos THEN o sistema SHALL implementar paginação
4. WHEN o operador clica em um processo THEN o sistema SHALL abrir a página de detalhes
5. WHEN não há processos cadastrados THEN o sistema SHALL exibir mensagem "Nenhum processo cadastrado"
6. WHEN a lista é carregada THEN o sistema SHALL ordenar por data de cadastro (mais recente primeiro)
7. WHEN o operador está em mobile THEN o sistema SHALL exibir cards em vez de tabela

### Requisito 3: Busca e Filtros

**User Story:** Como um operador do escritório, eu quero buscar e filtrar processos, para que eu possa encontrar rapidamente casos específicos.

#### Acceptance Criteria

1. WHEN o operador digita no campo de busca THEN o sistema SHALL buscar por número do processo, cliente ou descrição
2. WHEN o operador seleciona um filtro de status THEN o sistema SHALL exibir apenas processos com aquele status
3. WHEN o operador seleciona um filtro de tipo THEN o sistema SHALL exibir apenas processos daquele tipo
4. WHEN o operador seleciona um filtro de cliente THEN o sistema SHALL exibir apenas processos daquele cliente
5. WHEN múltiplos filtros são aplicados THEN o sistema SHALL combinar os filtros (AND logic)
6. WHEN o operador limpa os filtros THEN o sistema SHALL exibir todos os processos novamente
7. WHEN a busca não retorna resultados THEN o sistema SHALL exibir mensagem "Nenhum processo encontrado"

### Requisito 4: Edição de Processos

**User Story:** Como um operador do escritório, eu quero editar informações de processos existentes, para que eu possa manter os dados atualizados.

#### Acceptance Criteria

1. WHEN o operador acessa os detalhes de um processo THEN o sistema SHALL exibir um botão "Editar"
2. WHEN o operador clica em "Editar" THEN o sistema SHALL exibir um formulário preenchido com os dados atuais
3. WHEN o operador altera campos e clica em "Salvar" THEN o sistema SHALL validar os dados
4. WHEN os dados são válidos THEN o sistema SHALL atualizar o processo e exibir mensagem de sucesso
5. WHEN o processo é atualizado THEN o sistema SHALL registrar data de atualização e operador responsável
6. WHEN o operador clica em "Cancelar" THEN o sistema SHALL descartar as alterações
7. WHEN outro operador editou o processo simultaneamente THEN o sistema SHALL detectar conflito e alertar o usuário

### Requisito 5: Exclusão de Processos

**User Story:** Como um operador do escritório, eu quero excluir processos cadastrados incorretamente, para que eu possa manter a base de dados limpa.

#### Acceptance Criteria

1. WHEN o operador acessa os detalhes de um processo THEN o sistema SHALL exibir um botão "Excluir"
2. WHEN o operador clica em "Excluir" THEN o sistema SHALL exibir modal de confirmação
3. WHEN o operador confirma a exclusão THEN o sistema SHALL realizar soft delete (marcar como excluído)
4. WHEN o processo é excluído THEN o sistema SHALL registrar data de exclusão e operador responsável
5. WHEN o processo tem documentos anexados THEN o sistema SHALL manter os documentos (não deletar)
6. WHEN o processo é excluído THEN o sistema SHALL redirecionar para a lista de processos
7. WHEN o operador não tem permissão THEN o sistema SHALL ocultar o botão "Excluir"

### Requisito 6: Detalhes do Processo

**User Story:** Como um operador do escritório, eu quero visualizar todos os detalhes de um processo, para que eu possa ter acesso completo às informações do caso.

#### Acceptance Criteria

1. WHEN o operador acessa os detalhes de um processo THEN o sistema SHALL exibir todas as informações cadastradas
2. WHEN os detalhes são exibidos THEN o sistema SHALL mostrar: dados básicos, cliente vinculado, documentos, histórico
3. WHEN há documentos anexados THEN o sistema SHALL listar os documentos com opção de download
4. WHEN há histórico de alterações THEN o sistema SHALL exibir timeline com data, operador e alteração
5. WHEN o processo tem prazos THEN o sistema SHALL destacar prazos próximos (< 7 dias)
6. WHEN o processo está arquivado THEN o sistema SHALL exibir badge "Arquivado"
7. WHEN o operador não tem permissão THEN o sistema SHALL ocultar informações sensíveis

### Requisito 7: Upload de Documentos

**User Story:** Como um operador do escritório, eu quero anexar documentos aos processos, para que eu possa manter toda a documentação organizada.

#### Acceptance Criteria

1. WHEN o operador acessa os detalhes de um processo THEN o sistema SHALL exibir seção "Documentos"
2. WHEN o operador clica em "Adicionar Documento" THEN o sistema SHALL abrir modal de upload
3. WHEN o operador seleciona um arquivo THEN o sistema SHALL validar tipo (PDF, DOC, DOCX, JPG, PNG) e tamanho (< 10MB)
4. WHEN o arquivo é válido THEN o sistema SHALL fazer upload e vincular ao processo
5. WHEN o upload é concluído THEN o sistema SHALL exibir o documento na lista
6. WHEN o operador clica em um documento THEN o sistema SHALL fazer download do arquivo
7. WHEN o operador clica em "Excluir documento" THEN o sistema SHALL solicitar confirmação e remover o vínculo

### Requisito 8: Status do Processo

**User Story:** Como um operador do escritório, eu quero atualizar o status dos processos, para que eu possa acompanhar o andamento dos casos.

#### Acceptance Criteria

1. WHEN o operador acessa os detalhes de um processo THEN o sistema SHALL exibir o status atual
2. WHEN o operador clica no status THEN o sistema SHALL exibir dropdown com opções disponíveis
3. WHEN o operador seleciona um novo status THEN o sistema SHALL atualizar imediatamente
4. WHEN o status é alterado THEN o sistema SHALL registrar no histórico (data, operador, status anterior, status novo)
5. WHEN o status é "Arquivado" THEN o sistema SHALL solicitar motivo do arquivamento
6. WHEN o processo é arquivado THEN o sistema SHALL ocultar da lista principal (filtro padrão)
7. WHEN o operador filtra por "Arquivados" THEN o sistema SHALL exibir processos arquivados

### Requisito 9: Vinculação com Clientes

**User Story:** Como um operador do escritório, eu quero vincular processos a clientes, para que eu possa ver todos os processos de um cliente específico.

#### Acceptance Criteria

1. WHEN o operador cria um processo THEN o sistema SHALL exigir seleção de cliente
2. WHEN o operador seleciona um cliente THEN o sistema SHALL validar se o cliente existe no tenant
3. WHEN o processo é vinculado a um cliente THEN o sistema SHALL permitir acesso aos dados do cliente
4. WHEN o operador acessa o perfil do cliente THEN o sistema SHALL listar todos os processos vinculados
5. WHEN o cliente é excluído THEN o sistema SHALL manter os processos (não deletar em cascata)
6. WHEN o operador altera o cliente vinculado THEN o sistema SHALL registrar no histórico
7. WHEN o processo tem múltiplos clientes THEN o sistema SHALL permitir adicionar clientes secundários

### Requisito 10: Controle de Permissões

**User Story:** Como um administrador do tenant, eu quero controlar quem pode criar, editar e excluir processos, para que eu possa manter a segurança dos dados.

#### Acceptance Criteria

1. WHEN um operador acessa a página de processos THEN o sistema SHALL verificar permissões do usuário
2. WHEN o operador não tem permissão de leitura THEN o sistema SHALL redirecionar para página de acesso negado
3. WHEN o operador não tem permissão de criação THEN o sistema SHALL ocultar botão "Novo Processo"
4. WHEN o operador não tem permissão de edição THEN o sistema SHALL ocultar botão "Editar"
5. WHEN o operador não tem permissão de exclusão THEN o sistema SHALL ocultar botão "Excluir"
6. WHEN o operador tenta acessar via API sem permissão THEN o sistema SHALL retornar erro 403 Forbidden
7. WHEN o super admin está em modo impersonation THEN o sistema SHALL ter acesso total (read-only)

---

## Requisitos Não-Funcionais

### Performance

1. A listagem de processos DEVE carregar em menos de 2 segundos
2. A busca DEVE retornar resultados em menos de 1 segundo
3. O upload de documentos DEVE suportar arquivos de até 10MB
4. A paginação DEVE carregar 20 processos por página

### Segurança

1. Todos os endpoints DEVEM validar o tenant_id
2. Documentos DEVEM ser armazenados de forma segura
3. Histórico de alterações DEVE ser imutável
4. Soft delete DEVE ser usado para exclusões

### Usabilidade

1. Interface DEVE seguir o design system estabelecido
2. Formulários DEVEM ter validação em tempo real
3. Mensagens de erro DEVEM ser claras e acionáveis
4. Loading states DEVEM ser exibidos durante operações

### Compatibilidade

1. Sistema DEVE funcionar em Chrome, Firefox, Safari, Edge
2. Interface DEVE ser responsiva (mobile, tablet, desktop)
3. Sistema DEVE funcionar offline para leitura (PWA - futuro)

---

## Modelo de Dados

### Tabela: processos

```sql
CREATE TABLE processos (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  numero_processo VARCHAR(50) NOT NULL,
  cliente_id BIGINT NOT NULL REFERENCES clientes(id),
  tipo VARCHAR(100) NOT NULL,
  vara_tribunal VARCHAR(200),
  comarca VARCHAR(100),
  uf VARCHAR(2),
  status VARCHAR(50) DEFAULT 'Em Andamento',
  valor_causa DECIMAL(15,2),
  data_distribuicao DATE,
  descricao TEXT,
  observacoes TEXT,
  created_by BIGINT REFERENCES users(id),
  updated_by BIGINT REFERENCES users(id),
  deleted_by BIGINT REFERENCES users(id),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  deleted_at TIMESTAMP,
  UNIQUE(tenant_id, numero_processo)
);

CREATE INDEX idx_processos_tenant ON processos(tenant_id);
CREATE INDEX idx_processos_cliente ON processos(cliente_id);
CREATE INDEX idx_processos_status ON processos(status);
CREATE INDEX idx_processos_numero ON processos(numero_processo);
```

### Tabela: processo_documentos

```sql
CREATE TABLE processo_documentos (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  processo_id BIGINT NOT NULL REFERENCES processos(id),
  nome_arquivo VARCHAR(255) NOT NULL,
  tipo_arquivo VARCHAR(50),
  tamanho_bytes BIGINT,
  caminho_storage VARCHAR(500) NOT NULL,
  uploaded_by BIGINT REFERENCES users(id),
  created_at TIMESTAMP DEFAULT NOW(),
  deleted_at TIMESTAMP
);

CREATE INDEX idx_processo_docs_processo ON processo_documentos(processo_id);
```

### Tabela: processo_historico

```sql
CREATE TABLE processo_historico (
  id BIGINT PRIMARY KEY DEFAULT unique_rowid(),
  processo_id BIGINT NOT NULL REFERENCES processos(id),
  user_id BIGINT REFERENCES users(id),
  acao VARCHAR(50) NOT NULL,
  campo_alterado VARCHAR(100),
  valor_anterior TEXT,
  valor_novo TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_processo_hist_processo ON processo_historico(processo_id);
```

---

## Fluxos de Usuário

### Fluxo 1: Criar Novo Processo

1. Operador acessa /processos
2. Clica em "Novo Processo"
3. Preenche formulário (número, cliente, tipo, vara)
4. Sistema valida dados
5. Operador clica em "Salvar"
6. Sistema cria processo
7. Sistema redireciona para detalhes do processo

### Fluxo 2: Buscar Processo

1. Operador acessa /processos
2. Digita número do processo na busca
3. Sistema filtra lista em tempo real
4. Operador clica no processo encontrado
5. Sistema exibe detalhes

### Fluxo 3: Anexar Documento

1. Operador acessa detalhes do processo
2. Clica em "Adicionar Documento"
3. Seleciona arquivo do computador
4. Sistema valida tipo e tamanho
5. Sistema faz upload
6. Sistema exibe documento na lista

---

## Critérios de Aceitação Globais

1. ✅ Todos os endpoints devem validar tenant_id
2. ✅ Todas as operações devem ser registradas em audit log
3. ✅ Interface deve seguir o design system estabelecido
4. ✅ Código deve ter cobertura de testes > 80%
5. ✅ Documentação da API deve estar completa
6. ✅ Performance deve atender aos requisitos não-funcionais

---

**Versão**: 1.0  
**Data**: 03/11/2025  
**Status**: Aprovado para implementação
