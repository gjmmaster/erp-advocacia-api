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
     Retorna o número de linhas afetadas."))
