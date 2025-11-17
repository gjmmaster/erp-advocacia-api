(ns juridico.api.db.protocols)

(defprotocol ProcessosRepository
  "Define o contrato para o repositório de processos jurídicos.
  A implementação deste protocolo deve gerenciar internamente o tenant-id,
  garantindo que todas as operações sejam isoladas por tenant."

  (listar-processos [this]
    "Retorna uma lista de todos os processos para o tenant atual.")

  (obter-processo-por-id [this id]
    "Busca um processo específico pelo seu ID para o tenant atual.
     Retorna o processo se encontrado, ou nil caso contrário.")

  (criar-processo [this processo]
    "Cria um novo processo para o tenant atual.
     `processo` é um mapa com os dados do novo processo.
     Retorna o processo recém-criado, possivelmente com o ID adicionado.")

  (atualizar-processo [this id dados-processo]
    "Atualiza um processo existente pelo seu ID.")

  (deletar-processo [this id]
    "Deleta um processo pelo seu ID."))

(defprotocol AuthRepository
  "Define o contrato para o repositório de autenticação e provisionamento."

  (encontrar-tenant-por-subdominio [this subdominio]
    "Busca um tenant pelo seu subdomínio. Retorna o tenant ou nil.")

  (encontrar-usuario-por-email [this tenant-id email]
    "Busca um usuário pelo seu e-mail, dentro do escopo de um tenant específico.")

  (encontrar-usuario-por-email-global [this email]
    "Busca um usuário por email em TODOS os tenants (para auto-descoberta).")

  (encontrar-super-admin-por-email [this email]
    "Busca um super admin pelo seu e-mail (sem tenant context).")

  (criar-tenant-e-usuario-master [this dados-provisionamento]
    "Cria um novo tenant e seu primeiro usuário (master).")

  ;; --- NOVAS FUNÇÕES ADICIONADAS AQUI ---
  (listar-usuarios-do-tenant [this tenant-id]
    "Retorna uma lista de todos os usuários de um tenant específico.")

  (criar-usuario-operador [this tenant-id dados-usuario]
    "Cria um novo usuário com a role 'operador' para um tenant específico.")

  (atualizar-operador [this tenant-id user-id dados-usuario]
    "Atualiza os dados de um operador.")

  (deletar-operador [this tenant-id user-id]
    "Deleta um usuário operador.")

  (obter-operador-por-id [this tenant-id user-id]
    "Busca um operador específico pelo seu ID.")

  ;; --- Funções de Gestão de Tenants (Super Admin) ---

  (listar-tenants [this]
    "Retorna uma lista de todos os tenants no sistema.")

  (obter-tenant-por-id [this tenant-id]
    "Busca um tenant específico pelo seu ID.")

  (atualizar-tenant [this tenant-id dados-tenant]
    "Atualiza os dados de um tenant específico.")

  (criar-tenant [this dados-tenant]
    "Cria um novo tenant.")

  (deletar-tenant [this tenant-id]
    "Deleta um tenant específico.")

  (get-tenant-master-user [this tenant-id]
    "Busca o usuário master de um tenant específico.")

  ;; --- Funções de Estatísticas do Dashboard ---

  (count-processos [this tenant-id]
    "Retorna o total de processos de um tenant.")

  (count-clientes [this tenant-id]
    "Retorna o total de clientes de um tenant.")

  (count-operadores [this tenant-id]
    "Retorna o total de operadores de um tenant.")

  (count-processos-ativos [this tenant-id]
    "Retorna o total de processos ativos de um tenant.")
  
  ;; --- Funções de Gestão de Senha ---
  
  (update-user-password! [this user-id new-password-hash temporary-password]
    "Atualiza a senha de um usuário e a flag temporary_password.
     Retorna o número de linhas afetadas.")
  
  ;; --- Funções de Impersonation ---
  
  (find-by-id [this user-id]
    "Busca um usuário pelo seu ID (para impersonation).
     Retorna o usuário ou nil."))


;; ============================================
;; Protocols para Gestão de Processos Jurídicos
;; ============================================

(defprotocol ProcessoRepository
  "Define o contrato para operações com processos jurídicos.
  Todas as operações são isoladas por tenant."

  (find-all-processos [this tenant-id opts]
    "Lista processos com paginação e filtros.
     opts: {:page 1 :per-page 20 :status nil :tipo nil :cliente-id nil :search nil}
     Retorna: {:processos [...] :total count :page page :per-page per-page}")

  (find-processo-by-id [this tenant-id processo-id]
    "Busca processo por ID validando tenant.
     Retorna o processo ou nil se não encontrado.")

  (find-processo-by-numero [this tenant-id numero]
    "Busca processo por número validando tenant.
     Usado para validar duplicatas.
     Retorna o processo ou nil se não encontrado.")

  (create-processo! [this processo-data]
    "Cria novo processo.
     processo-data: {:tenant-id :numero-processo :cliente-id :tipo :created-by ...}
     Retorna o processo criado com ID.")

  (update-processo! [this tenant-id processo-id updates user-id]
    "Atualiza processo existente.
     updates: mapa com campos a atualizar
     user-id: ID do usuário que está fazendo a atualização
     Retorna o processo atualizado.")

  (soft-delete-processo! [this tenant-id processo-id user-id]
    "Marca processo como deletado (soft delete).
     Retorna true se deletado com sucesso.")

  (search-processos [this tenant-id query opts]
    "Busca full-text em processos.
     query: termo de busca (número, cliente, descrição)
     opts: {:page 1 :per-page 20}
     Retorna: {:processos [...] :total count}")

  (count-processos-by-status [this tenant-id status]
    "Conta processos por status.
     Retorna número inteiro."))

(defprotocol DocumentoRepository
  "Define o contrato para operações com documentos de processos."

  (find-documentos-by-processo [this processo-id]
    "Lista documentos de um processo (apenas ativos, excluindo soft deleted).
     Retorna lista de documentos.")

  (find-documento-by-id [this documento-id]
    "Busca documento por ID.
     Retorna o documento ou nil.")

  (create-documento! [this documento-data]
    "Registra novo documento.
     documento-data: {:processo-id :nome-arquivo :tipo-arquivo :tamanho-bytes 
                      :caminho-storage :uploaded-by}
     Retorna o documento criado com ID.")

  (soft-delete-documento! [this documento-id]
    "Marca documento como deletado (soft delete).
     Retorna true se deletado com sucesso.")

  (count-documentos-by-processo [this processo-id]
    "Conta documentos ativos de um processo.
     Retorna número inteiro."))

(defprotocol HistoricoRepository
  "Define o contrato para operações com histórico de processos.
  Histórico é imutável - apenas inserção e leitura."

  (add-historico! [this historico-data]
    "Adiciona entrada no histórico.
     historico-data: {:processo-id :user-id :acao :campo-alterado 
                      :valor-anterior :valor-novo}
     acao: 'criacao', 'edicao', 'exclusao', 'mudanca_status'
     Retorna o registro de histórico criado.")

  (find-historico-by-processo [this processo-id opts]
    "Lista histórico de um processo ordenado por data DESC.
     opts: {:limit 50 :offset 0}
     Retorna lista de registros de histórico.")

  (count-historico-by-processo [this processo-id]
    "Conta registros de histórico de um processo.
     Retorna número inteiro."))

(defprotocol ClienteRepository
  "Define o contrato para operações com clientes.
  Todas as operações são isoladas por tenant."

  (find-all-clientes [this tenant-id opts]
    "Lista clientes com paginação.
     opts: {:page 1 :per-page 20 :search nil}
     Retorna: {:clientes [...] :total count}")

  (find-cliente-by-id [this tenant-id cliente-id]
    "Busca cliente por ID validando tenant.
     Retorna o cliente ou nil.")

  (find-cliente-by-cpf-cnpj [this tenant-id cpf-cnpj]
    "Busca cliente por CPF/CNPJ validando tenant.
     Usado para validar duplicatas.
     Retorna o cliente ou nil.")

  (create-cliente! [this cliente-data]
    "Cria novo cliente.
     cliente-data: {:tenant-id :nome :cpf-cnpj :email :telefone :endereco}
     Retorna o cliente criado com ID.")

  (update-cliente! [this tenant-id cliente-id updates]
    "Atualiza cliente existente.
     Retorna o cliente atualizado.")

  (soft-delete-cliente! [this tenant-id cliente-id]
    "Marca cliente como deletado (soft delete).
     Retorna true se deletado com sucesso.")

  (search-clientes [this tenant-id query opts]
    "Busca clientes por nome, CPF/CNPJ ou email.
     Retorna: {:clientes [...] :total count}")

  (count-processos-by-cliente [this cliente-id]
    "Conta processos de um cliente.
     Retorna número inteiro."))

(defprotocol UserRepository
  "Define o contrato para operações com usuários.
  Todas as operações são isoladas por tenant."

  (list-users-by-tenant [this tenant-id]
    "Lista todos os usuários de um tenant.
     Retorna lista de usuários.")

  (get-user-by-id [this user-id]
    "Busca usuário por ID.
     Retorna o usuário ou nil.")

  (create-user [this user-data]
    "Cria novo usuário.
     user-data: {:tenant-id :email :full-name :role :password-hash :temporary-password :requires-password-change :active}
     Retorna o usuário criado com ID.")

  (update-user [this user-id updates]
    "Atualiza usuário existente.
     updates: mapa com campos a atualizar
     Retorna o usuário atualizado.")

  (soft-delete-user [this user-id]
    "Desativa um usuário (soft delete).
     Retorna true se desativado com sucesso.")

  (generate-temp-password [this]
    "Gera uma senha temporária aleatória.
     Retorna string com a senha."))
