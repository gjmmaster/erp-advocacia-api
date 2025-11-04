# Implementation Plan - Gestão de Processos Jurídicos

Este documento define o plano de implementação para o sistema de gestão de processos jurídicos, dividido em tarefas incrementais e testáveis.

---

## Tasks

- [x] 1. Criar tabela de clientes (pré-requisito)



  - Criar migration para tabela `clientes` com campos: id, tenant_id, nome, cpf_cnpj, email, telefone, endereco
  - Adicionar índices: tenant_id, cpf_cnpj único por tenant
  - Implementar soft delete (deleted_at)



  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 2. Criar migrations das tabelas de processos
  - Criar migration para tabela `processos` com todos os campos definidos no design
  - Criar migration para tabela `processo_documentos`



  - Criar migration para tabela `processo_historico`
  - Adicionar todos os índices necessários (tenant_id, cliente_id, status, numero_processo)
  - Adicionar constraints (unique, foreign keys, check)
  - _Requirements: 1.6, 2.1, 6.1_

- [ ] 3. Implementar protocols do backend
  - Criar namespace `juridico.api.db.protocols`
  - Definir protocol `ProcessoRepository` com todas as operações (find-all, find-by-id, create, update, soft-delete, search)
  - Definir protocol `DocumentoRepository` com operações de documentos
  - Definir protocol `HistoricoRepository` para auditoria
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1_

- [ ] 4. Implementar repository PostgreSQL
  - Criar implementação de `ProcessoRepository` em `juridico.api.db.postgres`
  - Implementar `find-all-processos` com paginação e filtros (status, tipo, cliente-id)
  - Implementar `find-processo-by-id` com validação de tenant
  - Implementar `find-processo-by-numero` para validação de duplicatas
  - Implementar `create-processo!` com validação de tenant e número único
  - Implementar `update-processo!` com registro de histórico
  - Implementar `soft-delete-processo!` marcando deleted_at
  - Implementar `search-processos` com busca por número, cliente ou descrição
  - _Requirements: 1.3, 1.4, 1.5, 2.1, 2.2, 3.1, 4.3, 4.4, 5.3, 5.4, 6.1_

- [ ] 5. Implementar repository de documentos
  - Implementar `DocumentoRepository` em `juridico.api.db.postgres`
  - Implementar `find-documentos-by-processo` listando documentos ativos
  - Implementar `create-documento!` registrando metadados do arquivo
  - Implementar `delete-documento!` com soft delete
  - _Requirements: 7.1, 7.4, 7.5, 7.7_

- [ ] 6. Implementar repository de histórico
  - Implementar `HistoricoRepository` em `juridico.api.db.postgres`
  - Implementar `add-historico!` para registrar alterações
  - Implementar `find-historico-by-processo` ordenado por data DESC
  - Registrar automaticamente: ação, campo alterado, valor anterior, valor novo, user_id
  - _Requirements: 4.5, 6.4, 8.4, 9.6_

- [ ] 7. Criar handlers de processos
  - Criar namespace `juridico.api.handlers.processos`
  - Implementar `list-processos` com paginação (page, per-page) e filtros (status, tipo, cliente-id)
  - Implementar `get-processo` validando tenant_id do JWT
  - Implementar `create-processo` com validação de dados e número único
  - Implementar `update-processo` com validação de tenant e registro de histórico
  - Implementar `delete-processo` com soft delete e registro de histórico
  - Implementar `search-processos` com busca full-text
  - Adicionar tratamento de erros (try-catch) em todos os handlers
  - _Requirements: 1.1, 1.2, 1.5, 1.7, 2.1, 2.4, 3.1, 3.2, 4.1, 4.4, 5.1, 5.3, 6.1_

- [ ] 8. Criar handlers de documentos
  - Implementar `list-documentos` retornando documentos de um processo
  - Implementar `upload-documento` com validação de tipo (PDF, DOC, DOCX, JPG, PNG) e tamanho (< 10MB)
  - Implementar `download-documento` com validação de acesso
  - Implementar `delete-documento` com confirmação e soft delete
  - Integrar com storage (filesystem ou S3)
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.6, 7.7_

- [ ] 9. Criar handlers de histórico
  - Implementar `get-historico` retornando timeline de alterações
  - Formatar resposta com: data, operador, ação, campo alterado, valores anterior/novo
  - Ordenar por created_at DESC
  - _Requirements: 6.4, 8.4_

- [ ] 10. Implementar middleware de permissões
  - Criar `wrap-permission-check` validando role do usuário
  - Definir permissões: read-processos, create-processos, update-processos, delete-processos
  - Master tem todas as permissões
  - Operador tem read e create (configurável)
  - Super-admin em impersonation tem read-only
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

- [ ] 11. Configurar rotas do backend
  - Adicionar rotas em `juridico.api.routes`
  - GET /api/processos - listar com paginação e filtros
  - POST /api/processos - criar novo processo
  - GET /api/processos/:id - detalhes do processo
  - PUT /api/processos/:id - atualizar processo
  - DELETE /api/processos/:id - soft delete
  - GET /api/processos/search - busca
  - GET /api/processos/:id/documentos - listar documentos
  - POST /api/processos/:id/documentos - upload documento
  - DELETE /api/processos/:id/documentos/:doc-id - deletar documento
  - GET /api/processos/:id/historico - histórico de alterações
  - Aplicar middleware de JWT e tenant validation em todas as rotas
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1_

- [ ] 12. Criar API routes do Next.js (BFF)
  - Criar `frontend-nextjs/src/app/api/processos/route.ts` com GET e POST
  - Criar `frontend-nextjs/src/app/api/processos/[id]/route.ts` com GET, PUT, DELETE
  - Criar `frontend-nextjs/src/app/api/processos/[id]/documentos/route.ts` com GET e POST
  - Criar `frontend-nextjs/src/app/api/processos/[id]/historico/route.ts` com GET
  - Criar `frontend-nextjs/src/app/api/processos/search/route.ts` com GET
  - Validar sessão em todas as rotas
  - Fazer proxy para backend Clojure com JWT do usuário
  - Tratar erros e retornar status codes apropriados
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1_

- [ ] 13. Criar página de listagem de processos
  - Criar `frontend-nextjs/src/app/processos/page.tsx`
  - Implementar componente `ProcessosList` com tabela responsiva
  - Exibir: número, cliente, tipo, status (badge), data de cadastro
  - Implementar paginação (20 por página)
  - Adicionar botão "Novo Processo" (se tiver permissão)
  - Exibir mensagem "Nenhum processo cadastrado" quando vazio
  - Implementar loading state
  - Em mobile, exibir cards em vez de tabela
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 10.3_

- [ ] 14. Criar componente de filtros
  - Criar `ProcessosFilters` component
  - Adicionar campo de busca (número, cliente, descrição)
  - Adicionar filtro de status (dropdown)
  - Adicionar filtro de tipo (dropdown)
  - Adicionar filtro de cliente (dropdown)
  - Implementar busca em tempo real (debounce 300ms)
  - Adicionar botão "Limpar filtros"
  - Exibir "Nenhum processo encontrado" quando busca não retorna resultados
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [ ] 15. Criar formulário de processo
  - Criar `ProcessoForm` component com modo create/edit
  - Adicionar campos: número do processo, cliente (select), tipo, vara/tribunal, comarca, UF, valor da causa, data de distribuição, descrição, observações
  - Implementar validação em tempo real
  - Marcar campos obrigatórios: número, cliente, tipo
  - Validar formato do número do processo
  - Exibir erro se número já existe
  - Adicionar botões "Salvar" e "Cancelar"
  - Implementar loading state no botão
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 4.2, 4.3, 4.6_

- [ ] 16. Criar página de novo processo
  - Criar `frontend-nextjs/src/app/processos/novo/page.tsx`
  - Renderizar `ProcessoForm` em modo create
  - Redirecionar para detalhes após criação bem-sucedida
  - Exibir toast de sucesso/erro
  - _Requirements: 1.1, 1.2, 1.5, 1.7_

- [ ] 17. Criar página de detalhes do processo
  - Criar `frontend-nextjs/src/app/processos/[id]/page.tsx`
  - Criar componente `ProcessoDetails` exibindo todos os dados
  - Exibir badge de status
  - Exibir badge "Arquivado" se aplicável
  - Adicionar botão "Editar" (se tiver permissão)
  - Adicionar botão "Excluir" (se tiver permissão)
  - Implementar seções: Dados Básicos, Cliente, Documentos, Histórico
  - _Requirements: 6.1, 6.2, 6.6, 6.7, 10.4, 10.5_

- [ ] 18. Implementar seção de documentos
  - Criar componente `DocumentosList`
  - Listar documentos com: nome, tipo, tamanho, data, usuário que fez upload
  - Adicionar botão "Adicionar Documento"
  - Implementar modal de upload com drag-and-drop
  - Validar tipo de arquivo (PDF, DOC, DOCX, JPG, PNG)
  - Validar tamanho (< 10MB)
  - Exibir progresso do upload
  - Adicionar botão de download em cada documento
  - Adicionar botão de excluir com confirmação
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

- [ ] 19. Implementar timeline de histórico
  - Criar componente `HistoricoTimeline`
  - Exibir timeline vertical com: data/hora, operador, ação, campo alterado, valores anterior/novo
  - Ordenar por data DESC (mais recente primeiro)
  - Exibir ícones diferentes por tipo de ação (criação, edição, exclusão, status)
  - Formatar datas de forma amigável (ex: "há 2 horas", "ontem")
  - _Requirements: 6.4, 8.4_

- [ ] 20. Implementar edição de status
  - Adicionar dropdown de status no `ProcessoDetails`
  - Opções: Em Andamento, Suspenso, Arquivado, Encerrado
  - Atualizar status ao selecionar (sem botão salvar)
  - Se status = "Arquivado", abrir modal solicitando motivo
  - Registrar alteração no histórico
  - Exibir toast de sucesso
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 21. Implementar página de edição
  - Criar `frontend-nextjs/src/app/processos/[id]/editar/page.tsx`
  - Renderizar `ProcessoForm` em modo edit com dados carregados
  - Validar dados antes de salvar
  - Detectar conflitos de edição simultânea (comparar updated_at)
  - Exibir alerta se outro operador editou simultaneamente
  - Redirecionar para detalhes após salvar
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

- [ ] 22. Implementar exclusão de processo
  - Adicionar modal de confirmação ao clicar em "Excluir"
  - Exibir mensagem: "Tem certeza? Os documentos serão mantidos."
  - Implementar soft delete (marcar deleted_at)
  - Registrar exclusão no histórico
  - Redirecionar para lista de processos
  - Exibir toast de sucesso
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [ ] 23. Implementar filtro de arquivados
  - Adicionar toggle "Mostrar Arquivados" nos filtros
  - Por padrão, ocultar processos arquivados da lista
  - Ao ativar toggle, incluir arquivados nos resultados
  - Exibir badge "Arquivado" nos processos arquivados
  - _Requirements: 8.6, 8.7_

- [ ] 24. Adicionar estilos CSS
  - Criar `ProcessosList.module.css` seguindo design system
  - Criar `ProcessoForm.module.css` com validação visual
  - Criar `ProcessoDetails.module.css` com layout responsivo
  - Criar `DocumentosList.module.css` com cards de documentos
  - Criar `HistoricoTimeline.module.css` com timeline vertical
  - Garantir responsividade mobile (breakpoints: 768px, 1024px)
  - Usar variáveis CSS do design system (cores, espaçamentos, tipografia)
  - _Requirements: 2.7_

- [ ] 25. Implementar controle de permissões no frontend
  - Criar hook `usePermissions` para verificar permissões do usuário
  - Ocultar botão "Novo Processo" se não tiver permissão de criação
  - Ocultar botão "Editar" se não tiver permissão de edição
  - Ocultar botão "Excluir" se não tiver permissão de exclusão
  - Exibir mensagem "Acesso negado" se tentar acessar sem permissão
  - Super-admin em impersonation: acesso read-only (sem botões de ação)
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.7_

- [ ] 26. Adicionar validações e mensagens de erro
  - Implementar validação de formato de número de processo
  - Exibir erro "Processo já cadastrado" se número duplicado
  - Exibir mensagens de erro claras e acionáveis
  - Implementar toast notifications para sucesso/erro
  - Adicionar validação de campos obrigatórios em tempo real
  - Destacar campos com erro (borda vermelha)
  - _Requirements: 1.3, 1.4, 1.5_

- [ ] 27. Implementar loading states
  - Adicionar skeleton loading na listagem
  - Adicionar spinner no botão "Salvar" durante submit
  - Adicionar progress bar no upload de documentos
  - Adicionar loading overlay ao carregar detalhes
  - Desabilitar botões durante operações
  - _Requirements: Performance, Usabilidade_

- [ ] 28. Criar migration script e documentação
  - Criar script `migrations/004_create_processos_system.sql` com todas as tabelas
  - Criar script de rollback `migrations/004_rollback_processos_system.sql`
  - Criar script shell `run_migration_processos.sh` para executar migration
  - Atualizar `docs/DATABASE_SCHEMA.md` com novas tabelas
  - Criar `PROCESSOS_DEPLOY_GUIDE.md` com instruções de deploy
  - _Requirements: Deployment_

- [ ]* 29. Adicionar testes backend
  - Criar testes unitários para validações em `processos-test.clj`
  - Criar testes de integração para repository operations
  - Criar testes para handlers (mock de database)
  - Testar casos de erro (processo duplicado, tenant inválido, etc)
  - Testar soft delete e histórico
  - _Requirements: Qualidade_

- [ ]* 30. Adicionar testes frontend
  - Criar testes para `ProcessosList` component
  - Criar testes para `ProcessoForm` component (validações)
  - Criar testes para `ProcessoDetails` component
  - Criar testes para filtros e busca
  - Testar permissões (botões ocultos/visíveis)
  - _Requirements: Qualidade_

---

## Notas de Implementação

### Ordem de Execução

1. **Fase 1 - Database & Backend Core** (Tasks 1-6)
   - Criar estrutura de dados
   - Implementar camada de acesso a dados

2. **Fase 2 - Backend API** (Tasks 7-11)
   - Implementar lógica de negócio
   - Criar endpoints REST

3. **Fase 3 - Frontend BFF** (Task 12)
   - Criar camada BFF no Next.js

4. **Fase 4 - Frontend UI** (Tasks 13-27)
   - Implementar interface do usuário
   - Adicionar interatividade e validações

5. **Fase 5 - Deploy & Tests** (Tasks 28-30)
   - Preparar para produção
   - Adicionar testes (opcional)

### Dependências Importantes

- Task 1 deve ser executada antes de Task 2 (clientes é FK de processos)
- Tasks 3-6 devem ser executadas antes de Task 7 (handlers dependem de repository)
- Task 11 deve ser executada antes de Task 12 (BFF depende de backend)
- Task 12 deve ser executada antes de Tasks 13-27 (UI depende de API)

### Tarefas Opcionais

- Tasks marcadas com `*` são opcionais (testes)
- Podem ser puladas para MVP mais rápido
- Recomendadas para produção

---

**Versão**: 1.0  
**Data**: 04/11/2025  
**Status**: Pronto para execução
