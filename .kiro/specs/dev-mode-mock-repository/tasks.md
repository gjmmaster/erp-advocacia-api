# Implementation Plan - Dev Mode Mock Repository

- [x] 1. Criar módulo de dados de seed


  - Criar arquivo `src/juridico/api/db/seed.clj`
  - Implementar função `generate-seed-data` que retorna mapa com todas as "tabelas"
  - Incluir 1 tenant demo com subdomain "demo"
  - Incluir 4 usuários: super-admin, master, e 2 operadores com senhas hasheadas
  - Incluir 3 clientes com dados completos (nome, CPF/CNPJ, email, telefone, endereço)
  - Incluir 5 processos com diferentes status e tipos vinculados aos clientes
  - Incluir 8 documentos vinculados aos processos
  - Incluir 10 entradas de histórico distribuídas entre os processos
  - Incluir contadores iniciais para auto-increment de IDs
  - Adicionar timestamps realistas (datas variadas nos últimos 6 meses)
  - _Requirements: 3.1, 3.2, 3.3_



- [ ] 2. Implementar funções helper para o repositório mock
  - Criar arquivo `src/juridico/api/db/mock.clj` com namespace
  - Implementar `next-id!` para auto-incrementar IDs usando contadores do atom
  - Implementar `add-timestamps` para adicionar created_at e updated_at automaticamente
  - Implementar `filter-active` para filtrar registros sem deleted_at
  - Implementar `matches-search?` para busca case-insensitive
  - Implementar `paginate` para paginar resultados com formato {:items :total :page :per-page}
  - Implementar `validate-unique-email!` para validar emails únicos
  - Implementar `validate-unique-cpf-cnpj!` para validar CPF/CNPJ únicos
  - Implementar `validate-unique-numero-processo!` para validar números de processo únicos


  - Adicionar logging com prefixo "[MOCK]" em todas as funções
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 5.1, 5.2, 5.3_

- [ ] 3. Implementar MockRepository - AuthRepository protocol
  - Criar `defrecord MockRepository` com campo `db-atom`
  - Implementar `encontrar-tenant-por-subdominio` buscando em (:tenants @db-atom)
  - Implementar `encontrar-usuario-por-email` filtrando por tenant-id e email
  - Implementar `encontrar-usuario-por-email-global` buscando em todos os tenants
  - Implementar `encontrar-super-admin-por-email` filtrando por role "super-admin"
  - Implementar `criar-tenant-e-usuario-master` com validação de email único e geração de senha temporária
  - Implementar `listar-usuarios-do-tenant` filtrando por tenant-id
  - Implementar `criar-usuario-operador` com validação de limite de operadores
  - Implementar `atualizar-operador` atualizando dados no atom
  - Implementar `deletar-operador` removendo do atom
  - Implementar `obter-operador-por-id` buscando por ID e tenant-id
  - Implementar `listar-tenants` retornando todos os tenants
  - Implementar `obter-tenant-por-id` buscando tenant por ID
  - Implementar `atualizar-tenant` atualizando dados do tenant
  - Implementar `criar-tenant` inserindo novo tenant com ID auto-incrementado
  - Implementar `deletar-tenant` removendo tenant do atom
  - Implementar `get-tenant-master-user` buscando usuário master do tenant
  - Implementar `count-processos`, `count-clientes`, `count-operadores`, `count-processos-ativos`


  - Implementar `update-user-password!` atualizando senha e flag temporary_password
  - Implementar `find-by-id` buscando usuário por ID
  - Adicionar logging detalhado em cada método
  - _Requirements: 1.1, 1.2, 1.4, 5.1, 5.2, 5.3, 7.1, 7.2_

- [ ] 4. Implementar MockRepository - ProcessoRepository protocol
  - Implementar `find-all-processos` com paginação, filtros (status, tipo, cliente-id) e busca
  - Implementar `find-processo-by-id` buscando por ID e validando tenant
  - Implementar `find-processo-by-numero` buscando por número de processo
  - Implementar `create-processo!` com validação de número único, auto-increment de ID, timestamps e entrada no histórico


  - Implementar `update-processo!` atualizando dados, timestamps e registrando alterações no histórico
  - Implementar `soft-delete-processo!` definindo deleted_at e deleted_by, e registrando no histórico
  - Implementar `search-processos` com busca case-insensitive em número, descrição e nome do cliente
  - Implementar `count-processos-by-status` contando processos por status
  - Adicionar logging detalhado em cada método
  - _Requirements: 1.1, 1.2, 1.4, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 5.1, 5.2, 7.1_

- [ ] 5. Implementar MockRepository - ClienteRepository protocol
  - Implementar `find-all-clientes` com paginação e busca
  - Implementar `find-cliente-by-id` buscando por ID e validando tenant


  - Implementar `find-cliente-by-cpf-cnpj` buscando por CPF/CNPJ
  - Implementar `create-cliente!` com validação de CPF/CNPJ único, auto-increment de ID e timestamps
  - Implementar `update-cliente!` atualizando dados e timestamp updated_at
  - Implementar `soft-delete-cliente!` definindo deleted_at
  - Implementar `search-clientes` com busca case-insensitive em nome, CPF/CNPJ e email
  - Implementar `count-processos-by-cliente` contando processos do cliente
  - Adicionar logging detalhado em cada método
  - _Requirements: 1.1, 1.2, 1.4, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 5.1, 5.2, 7.1_

- [x] 6. Implementar MockRepository - DocumentoRepository e HistoricoRepository protocols

  - Implementar `find-documentos-by-processo` filtrando por processo-id e deleted_at nulo
  - Implementar `find-documento-by-id` buscando documento por ID
  - Implementar `create-documento!` com auto-increment de ID e timestamps
  - Implementar `soft-delete-documento!` definindo deleted_at
  - Implementar `count-documentos-by-processo` contando documentos ativos do processo
  - Implementar `add-historico!` inserindo entrada no histórico com auto-increment de ID
  - Implementar `find-historico-by-processo` com paginação (limit/offset) ordenado por data DESC
  - Implementar `count-historico-by-processo` contando entradas de histórico


  - Adicionar logging detalhado em cada método
  - _Requirements: 1.1, 1.2, 1.4, 4.1, 4.2, 5.1, 5.2, 7.1_

- [ ] 7. Criar função construtora do MockRepository
  - Implementar `create-mock-repository` que cria atom com dados de seed
  - Chamar `generate-seed-data` do módulo seed
  - Criar instância de MockRepository com o atom


  - Logar resumo dos dados de seed criados (quantidade de tenants, usuários, clientes, processos)
  - Logar credenciais dos usuários de seed para facilitar acesso
  - Retornar instância do MockRepository
  - _Requirements: 1.3, 3.1, 3.4, 5.4_

- [x] 8. Criar módulo factory para seleção de repositório


  - Criar arquivo `src/juridico/api/db/core.clj`
  - Implementar função `create-repository` que verifica variável de ambiente DEV_MODE
  - Se DEV_MODE=true, retornar MockRepository criado por `create-mock-repository`
  - Se DEV_MODE=false ou ausente, retornar PostgresRepository com datasource
  - Logar claramente qual modo está sendo usado (DEV ou PRODUCTION)
  - Criar `def repository` como delay para lazy-loading
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 7.5_

- [x] 9. Atualizar core.clj para usar repository factory


  - Importar `juridico.api.db.core` no namespace de `juridico.api.core`
  - Substituir criação direta de PostgresRepository por chamada a `create-repository`
  - Atualizar handlers para usar a instância do repositório retornada pela factory
  - Garantir que tenant-id seja passado corretamente quando necessário
  - Testar que aplicação inicia corretamente em ambos os modos
  - _Requirements: 2.5, 7.1, 7.2, 7.3, 7.4_

- [ ] 10. Criar scripts de inicialização para modo dev
  - Criar `dev-mock.sh` para Linux/Mac que define DEV_MODE=true e executa lein run



  - Criar `dev-mock.ps1` para Windows que define DEV_MODE=true e executa lein run
  - Criar `dev-full-mock.sh` que inicia backend em modo dev e frontend simultaneamente
  - Criar `dev-full-mock.ps1` versão Windows do script acima
  - Adicionar mensagens coloridas indicando modo DEV ativo
  - Exibir credenciais dos usuários de seed nos scripts
  - Adicionar verificação se backend já está rodando
  - Tornar scripts executáveis (chmod +x no Linux/Mac)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 11. Criar documentação de uso do modo dev
  - Criar arquivo `MODO_DEV_MOCK.md` na raiz do projeto
  - Documentar diferença entre modo dev (mock) e produção (PostgreSQL)
  - Listar todos os usuários de seed com credenciais
  - Explicar como alternar entre mock e PostgreSQL via DEV_MODE
  - Incluir exemplos de como adicionar novos dados de seed
  - Documentar limitações do modo mock (dados não persistem, performance, etc.)
  - Adicionar seção de troubleshooting para problemas comuns
  - Incluir exemplos de uso dos scripts de inicialização
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 12. Testar integração completa
  - Iniciar aplicação com DEV_MODE=true usando script dev-mock
  - Fazer login com super-admin (admin@demo.com)
  - Fazer login com master user (master@demo.com)
  - Fazer login com operador (operador1@demo.com)
  - Listar clientes e verificar que 3 clientes de seed aparecem
  - Criar novo cliente e verificar que ID é auto-incrementado corretamente
  - Editar cliente e verificar que updated_at é atualizado
  - Deletar cliente (soft delete) e verificar que não aparece mais na listagem
  - Listar processos e verificar que 5 processos de seed aparecem
  - Criar novo processo e verificar que entrada é adicionada ao histórico
  - Editar processo e verificar que alterações são registradas no histórico
  - Deletar processo e verificar soft delete funciona
  - Listar documentos de um processo
  - Visualizar histórico de um processo
  - Reiniciar aplicação e verificar que dados de seed são recriados
  - Iniciar aplicação com DEV_MODE=false e verificar que PostgreSQL é usado
  - Verificar que todos os logs aparecem corretamente com prefixo [MOCK] ou [POSTGRES]
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 2.5, 3.4, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 5.1, 5.2, 5.3, 5.4, 5.5, 7.1, 7.2, 7.3, 7.4, 7.5_
