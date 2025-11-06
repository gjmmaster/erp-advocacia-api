(ns juridico.api.db.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [juridico.api.db.protocols :refer [ProcessosRepository AuthRepository
                                                ProcessoRepository DocumentoRepository
                                                HistoricoRepository ClienteRepository]]
            [environ.core :refer [env]]
            [buddy.hashers :as hashers]
            [buddy.core.nonce :as nonce]
            [buddy.core.codecs :as codecs]
            [clojure.string :as str]))

;; --- FUNÇÃO HELPER PARA GERAÇÃO DE SENHA SEGURA ---
(defn- generate-secure-temp-password
  "Gera uma senha temporária criptograficamente segura de 12 caracteres alfanuméricos."
  []
  (-> (nonce/random-bytes 16)           ; 16 bytes = 128 bits de entropia
      (codecs/bytes->b64-str)           ; Converte para Base64
      (str/replace #"[^a-zA-Z0-9]" "")  ; Remove caracteres especiais
      (subs 0 12)))                     ; Pega os primeiros 12 caracteres

;; --- FUNÇÃO HELPER PARA PARSE DA URL DO BANCO ---
(defn- parse-db-url [db-url]
  (if-not db-url
    (throw (Exception. "A variável de ambiente DATABASE_URL não foi configurada."))
    (let [pattern #"postgres(?:ql)?://([^:]+):([^@]+)@([^:]+):(\d+)/([^?]+)"
          matcher (re-matcher pattern db-url)]
      (if (.find matcher)
        (let [[_ user password host port dbname] (re-groups matcher)]
          {:dbtype   "postgresql"
           :host     host
           :port     (Integer/parseInt port)
           :dbname   dbname
           :user     user
           :password password
           :sslmode  "require"})
        (throw (Exception. (str "Formato da DATABASE_URL inválido: " db-url)))))))

;; --- CONFIGURAÇÃO DA CONEXÃO ---
(def datasource
  (delay
    (let [db-spec (parse-db-url (env :database-url))]
      (jdbc/get-datasource db-spec))))

;; --- IMPLEMENTAÇÃO CONCRETA PARA POSTGRESQL ---
(defrecord PostgresRepository [db-conn tenant-id]

  ;; --- Implementação do Protocolo de Processos ---
  ProcessosRepository
  (listar-processos [this]
    (sql/query db-conn ["SELECT * FROM legal_cases WHERE tenant_id = ?" tenant-id]))

  (obter-processo-por-id [this id]
    (first (sql/query db-conn ["SELECT * FROM legal_cases WHERE id = ? AND tenant_id = ?" id tenant-id])))

  (criar-processo [this processo]
    (sql/insert! db-conn :legal_cases (assoc processo :tenant_id tenant-id)))

  (atualizar-processo [this id dados-processo]
    (sql/update! db-conn :legal_cases dados-processo {:id id :tenant_id tenant-id}))

  (deletar-processo [this id]
    (sql/delete! db-conn :legal_cases {:id id :tenant_id tenant-id}))

  ;; --- Implementação do Protocolo de Autenticação ---
  AuthRepository
  (encontrar-tenant-por-subdominio [this subdominio]
    (first (sql/query db-conn ["SELECT * FROM tenants WHERE subdomain = ?" subdominio])))

  (encontrar-usuario-por-email [this tenant-id email]
    (first (sql/query db-conn ["SELECT * FROM users WHERE tenant_id = ? AND email = ?" tenant-id email])))

  (encontrar-usuario-por-email-global [this email]
    "Busca usuário por email em TODOS os tenants (para auto-descoberta)"
    (let [result (first (sql/query db-conn 
                          ["SELECT u.*, t.company_name as tenant_name, t.is_active as tenant_active
                            FROM users u
                            JOIN tenants t ON u.tenant_id = t.id
                            WHERE LOWER(u.email) = LOWER(?)"
                           email]))]
      (println "[POSTGRES] encontrar-usuario-por-email-global result:" result)
      (println "[POSTGRES] :tenants/tenant_active value:" (:tenants/tenant_active result))
      ;; Retornar com o campo correto (sem namespace para facilitar acesso)
      (when result
        (assoc result :tenant_active (:tenants/tenant_active result)))))

  (encontrar-super-admin-por-email [this email]
    (first (sql/query db-conn ["SELECT * FROM users WHERE email = ? AND role = 'super-admin'" email])))

  (criar-tenant-e-usuario-master [this {:keys [company_name email operator_limit]}]
    (jdbc/with-transaction [tx db-conn]
      (let [subdomain (-> company_name str/lower-case (str/replace #"[^a-z0-9-]" "-"))
            temp-password (generate-secure-temp-password)
            new-tenant (sql/insert! tx :tenants
                                    {:company_name company_name
                                     :subdomain subdomain
                                     :operator_limit (or operator_limit 4)}
                                    {:return-keys ["id"]})
            
            ;; CORREÇÃO FINAL: Extrai o valor usando a chave correta :tenants/id
            new-tenant-id (:tenants/id new-tenant)]
            
        (if-not new-tenant-id
          (throw (Exception. (str "Falha ao obter o ID do novo tenant. Resposta do DB: " new-tenant)))
          
          (let [new-user (sql/insert! tx :users 
                                      {:tenant_id new-tenant-id
                                       :email email
                                       :password_hash (hashers/encrypt temp-password)
                                       :role "master"
                                       :temporary_password true}  ;; ⭐ NOVO: Marcar senha como temporária
                                      {:return-keys true})]
            {:tenant {:id new-tenant-id :subdomain subdomain :company_name company_name}
             :user {:email email :temp_password temp-password}})))))

  ;; --- IMPLEMENTAÇÃO DAS NOVAS FUNÇÕES ---
  (listar-usuarios-do-tenant [this tenant-id]
    ;; Retorna todos os utilizadores, mas omite o hash da palavra-passe por segurança
    (sql/query db-conn ["SELECT id, email, full_name, role FROM users WHERE tenant_id = ?" tenant-id]))

  (obter-operador-por-id [this tenant-id user-id]
    (first (sql/query db-conn ["SELECT id, email, full_name, role FROM users WHERE tenant_id = ? AND id = ?" tenant-id user-id])))

  (criar-usuario-operador [this tenant-id {:keys [email password full_name]}]
    (jdbc/with-transaction [tx db-conn]
      (let [tenant (first (sql/query tx ["SELECT operator_limit FROM tenants WHERE id = ?" tenant-id]))
            operator-limit (get tenant :tenants/operator_limit 4) ; Usa get e o novo default 4
            current-operators (count (sql/query tx ["SELECT id FROM users WHERE tenant_id = ? AND role = 'operador'" tenant-id]))]
        (if (< current-operators operator-limit)
          (sql/insert! tx :users {:tenant_id     tenant-id
                                   :email         email
                                   :full_name     full_name
                                   :password_hash (hashers/encrypt password)
                                   :role          "operador"})
          (throw (ex-info "Limite de operadores atingido para este tenant."
                          {:type :limite-excedido
                           :tenant-id tenant-id
                           :limit operator-limit
                           :current current-operators}))))))

  (atualizar-operador [this tenant-id user-id dados-usuario]
    ;; Apenas o full_name pode ser alterado por enquanto.
    ;; A lógica para alterar senha seria mais complexa.
    (sql/update! db-conn :users (select-keys dados-usuario [:full_name])
                 {:id user-id :tenant_id tenant-id :role "operador"}))

  (deletar-operador [this tenant-id user-id]
    ;; Garante que apenas operadores sejam deletados por esta função
    (sql/delete! db-conn :users {:id user-id :tenant_id tenant-id :role "operador"}))

  ;; --- Implementação das Funções de Gestão de Tenants ---

  (listar-tenants [this]
    (let [results (sql/query db-conn ["SELECT id, company_name, subdomain, created_at, operator_limit FROM tenants"])]
      (println "=== POSTGRES: listar-tenants ===")
      (println "Resultados brutos:" results)
      ;; Retorna IDs como strings para evitar perda de precisão no JavaScript
      (mapv #(let [tenant-map {:id (str (:tenants/id %))
                               :company_name (:tenants/company_name %)
                               :subdomain (:tenants/subdomain %)
                               :created_at (str (:tenants/created_at %))
                               :operator_limit (int (:tenants/operator_limit %))}]
               (println "Tenant processado:" tenant-map)
               tenant-map)
            results)))

  (obter-tenant-por-id [this tenant-id]
    (println "=== POSTGRES: obter-tenant-por-id ===")
    (println "Tenant ID recebido:" tenant-id "Tipo:" (type tenant-id))
    ;; Converte string para Long se necessário
    (let [id-long (if (string? tenant-id) 
                    (Long/parseLong tenant-id) 
                    tenant-id)]
      (println "ID convertido para Long:" id-long)
      (when-let [result (first (sql/query db-conn ["SELECT id, company_name, subdomain, created_at, operator_limit FROM tenants WHERE id = ?" id-long]))]
        (println "Resultado bruto:" result)
        ;; Retorna ID como string para evitar perda de precisão no JavaScript
        (let [tenant-map {:id (str (:tenants/id result))
                          :company_name (:tenants/company_name result)
                          :subdomain (:tenants/subdomain result)
                          :created_at (str (:tenants/created_at result))
                          :operator_limit (int (:tenants/operator_limit result))}]
          (println "Tenant processado:" tenant-map)
          tenant-map))))

  (atualizar-tenant [this tenant-id dados-tenant]
    ;; Apenas company_name e operator_limit podem ser alterados
    (println "=== POSTGRES: atualizar-tenant ===")
    (println "Tenant ID:" tenant-id)
    (println "Dados tenant:" dados-tenant)
    ;; Converte string para Long se necessário
    (let [id-long (if (string? tenant-id) 
                    (Long/parseLong tenant-id) 
                    tenant-id)
          dados-filtrados (select-keys dados-tenant [:company_name :operator_limit])]
      (println "ID convertido para Long:" id-long)
      (println "Dados filtrados:" dados-filtrados)
      (let [resultado (sql/update! db-conn :tenants dados-filtrados {:id id-long})]
        (println "Resultado do UPDATE:" resultado)
        resultado)))

  (criar-tenant [this {:keys [company_name subdomain operator_limit]}]
    (let [subdomain-to-use (or subdomain (-> company_name str/lower-case (str/replace #"[^a-z0-9-]" "-")))]
      (sql/insert! db-conn :tenants
                   {:company_name company_name
                    :subdomain subdomain-to-use
                    :operator_limit (or operator_limit 4)}
                   {:return-keys true})))

  (deletar-tenant [this tenant-id]
    (println "=== POSTGRES: deletar-tenant ===")
    (println "Tenant ID:" tenant-id)
    ;; Converte string para Long se necessário
    (let [id-long (if (string? tenant-id) 
                    (Long/parseLong tenant-id) 
                    tenant-id)]
      (println "ID convertido para Long:" id-long)
      (let [resultado (sql/delete! db-conn :tenants {:id id-long})]
        (println "Resultado do DELETE:" resultado)
        resultado)))

  ;; --- Funções de Estatísticas do Dashboard ---
  
  (count-processos [this tenant-id]
    (let [id-long (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          result (jdbc/execute-one! db-conn 
                   ["SELECT COUNT(*) as count FROM legal_cases WHERE tenant_id = ?" id-long])]
      (:count result 0)))

  (count-clientes [this tenant-id]
    ;; Por enquanto retorna 0, será implementado quando criar tabela de clientes
    0)

  (count-operadores [this tenant-id]
    (let [id-long (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          result (jdbc/execute-one! db-conn 
                   ["SELECT COUNT(*) as count FROM users WHERE tenant_id = ? AND role = 'operador'" id-long])]
      (:count result 0)))

  (count-processos-ativos [this tenant-id]
    (let [id-long (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          result (jdbc/execute-one! db-conn 
                   ["SELECT COUNT(*) as count FROM legal_cases WHERE tenant_id = ? AND status = 'ativo'" id-long])]
      (:count result 0)))
  
  ;; --- Funções de Gestão de Senha ---
  
  (update-user-password! [this user-id new-password-hash temporary-password]
    "Atualiza a senha de um usuário e a flag temporary_password.
     Usa transação para garantir atomicidade."
    (println "[POSTGRES] Atualizando senha para user-id:" user-id)
    (jdbc/with-transaction [tx db-conn]
      (let [id-long (if (string? user-id) (Long/parseLong user-id) user-id)
            result (sql/update! tx :users
                               {:password_hash new-password-hash
                                :temporary_password temporary-password}
                               {:id id-long})]
        (println "[POSTGRES] Linhas afetadas:" (:next.jdbc/update-count result))
        result)))
  
  ;; --- Funções de Impersonation ---
  
  (get-tenant-master-user [this tenant-id]
    "Busca o usuário master de um tenant específico."
    (println "=== POSTGRES: get-tenant-master-user ===")
    (println "Tenant ID:" tenant-id)
    (let [id-long (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          result (first (sql/query db-conn ["SELECT id, email, role, tenant_id FROM users WHERE tenant_id = ? AND role = 'master' LIMIT 1" id-long]))]
      (println "Resultado da query:" result)
      (when result
        (let [user-map {:id (:users/id result)
                       :email (:users/email result)
                       :role (:users/role result)
                       :tenant_id (:users/tenant_id result)}]
          (println "User map retornado:" user-map)
          user-map))))
  
  (find-by-id [this user-id]
    "Busca um usuário pelo seu ID (para impersonation).
     Retorna o usuário ou nil."
    (println "=== POSTGRES: find-by-id ===")
    (println "User ID:" user-id)
    (let [id-long (if (string? user-id) (Long/parseLong user-id) user-id)
          result (first (sql/query db-conn ["SELECT id, email, role, tenant_id FROM users WHERE id = ?" id-long]))]
      (println "Resultado da query:" result)
      (when result
        (let [user-map {:id (:users/id result)
                       :email (:users/email result)
                       :role (:users/role result)
                       :tenant_id (:users/tenant_id result)}]
          (println "User map retornado:" user-map)
          user-map))))

  ;; ============================================
  ;; Implementação do ProcessoRepository
  ;; ============================================

  ProcessoRepository

  (find-all-processos [this tenant-id opts]
    (let [{:keys [page per-page status tipo cliente-id search]} opts
          page (or page 1)
          per-page (or per-page 20)
          offset (* (dec page) per-page)
          base-query "SELECT p.*, c.nome as cliente_nome 
                      FROM processos p 
                      LEFT JOIN clientes c ON p.cliente_id = c.id 
                      WHERE p.tenant_id = ? AND p.deleted_at IS NULL"
          conditions []
          params [tenant-id]]
      
      ;; Adiciona filtros dinamicamente
      (let [[query params] (cond-> [base-query params]
                             status
                             (fn [[q p]] [(str q " AND p.status = ?") (conj p status)])
                             
                             tipo
                             (fn [[q p]] [(str q " AND p.tipo = ?") (conj p tipo)])
                             
                             cliente-id
                             (fn [[q p]] [(str q " AND p.cliente_id = ?") (conj p cliente-id)])
                             
                             search
                             (fn [[q p]] [(str q " AND (p.numero_processo ILIKE ? OR p.descricao ILIKE ?)")
                                         (conj p (str "%" search "%") (str "%" search "%"))]))]
        
        ;; Query de contagem
        (let [count-query (str/replace query #"SELECT p\.\*, c\.nome as cliente_nome" "SELECT COUNT(*)")
              total (:count (jdbc/execute-one! db-conn (into [count-query] params)))
              
              ;; Query de dados com paginação
              final-query (str query " ORDER BY p.created_at DESC LIMIT ? OFFSET ?")
              processos (jdbc/execute! db-conn (into [final-query] (concat params [per-page offset])))]
          
          {:processos processos
           :total total
           :page page
           :per-page per-page}))))

  (find-processo-by-id [this tenant-id processo-id]
    (jdbc/execute-one! db-conn
      ["SELECT p.*, c.nome as cliente_nome 
        FROM processos p 
        LEFT JOIN clientes c ON p.cliente_id = c.id 
        WHERE p.id = ? AND p.tenant_id = ? AND p.deleted_at IS NULL"
       processo-id tenant-id]))

  (find-processo-by-numero [this tenant-id numero]
    (jdbc/execute-one! db-conn
      ["SELECT * FROM processos 
        WHERE tenant_id = ? AND numero_processo = ? AND deleted_at IS NULL"
       tenant-id numero]))

  (create-processo! [this processo-data]
    (jdbc/with-transaction [tx db-conn]
      (let [result (sql/insert! tx :processos processo-data {:return-keys true})
            processo-id (:processos/id result)]
        
        ;; Adiciona entrada no histórico
        (sql/insert! tx :processo_historico
          {:processo_id processo-id
           :user_id (:created_by processo-data)
           :acao "criacao"
           :campo_alterado nil
           :valor_anterior nil
           :valor_novo "Processo criado"})
        
        result)))

  (update-processo! [this tenant-id processo-id updates user-id]
    (jdbc/with-transaction [tx db-conn]
      ;; Busca valores anteriores para histórico
      (let [old-processo (jdbc/execute-one! tx
                           ["SELECT * FROM processos WHERE id = ? AND tenant_id = ?"
                            processo-id tenant-id])
            
            ;; Atualiza processo
            updated (sql/update! tx :processos
                      (assoc updates :updated_by user-id :updated_at (java.time.Instant/now))
                      {:id processo-id :tenant_id tenant-id})]
        
        ;; Registra alterações no histórico
        (doseq [[campo novo-valor] updates]
          (let [campo-str (name campo)
                valor-anterior (get old-processo (keyword (str "processos/" campo-str)))]
            (when (not= valor-anterior novo-valor)
              (sql/insert! tx :processo_historico
                {:processo_id processo-id
                 :user_id user-id
                 :acao "edicao"
                 :campo_alterado campo-str
                 :valor_anterior (str valor-anterior)
                 :valor_novo (str novo-valor)}))))
        
        updated)))

  (soft-delete-processo! [this tenant-id processo-id user-id]
    (jdbc/with-transaction [tx db-conn]
      (let [result (sql/update! tx :processos
                     {:deleted_at (java.time.Instant/now)
                      :deleted_by user-id}
                     {:id processo-id :tenant_id tenant-id})]
        
        ;; Registra exclusão no histórico
        (sql/insert! tx :processo_historico
          {:processo_id processo-id
           :user_id user-id
           :acao "exclusao"
           :campo_alterado nil
           :valor_anterior nil
           :valor_novo "Processo excluído"})
        
        (pos? (:next.jdbc/update-count result)))))

  (search-processos [this tenant-id query opts]
    (let [{:keys [page per-page]} opts
          page (or page 1)
          per-page (or per-page 20)
          offset (* (dec page) per-page)
          search-term (str "%" query "%")
          
          ;; Query de contagem
          count-result (jdbc/execute-one! db-conn
                         ["SELECT COUNT(*) as count FROM processos p
                           LEFT JOIN clientes c ON p.cliente_id = c.id
                           WHERE p.tenant_id = ? AND p.deleted_at IS NULL
                           AND (p.numero_processo ILIKE ? OR p.descricao ILIKE ? OR c.nome ILIKE ?)"
                          tenant-id search-term search-term search-term])
          
          ;; Query de dados
          processos (jdbc/execute! db-conn
                      ["SELECT p.*, c.nome as cliente_nome 
                        FROM processos p 
                        LEFT JOIN clientes c ON p.cliente_id = c.id 
                        WHERE p.tenant_id = ? AND p.deleted_at IS NULL
                        AND (p.numero_processo ILIKE ? OR p.descricao ILIKE ? OR c.nome ILIKE ?)
                        ORDER BY p.created_at DESC
                        LIMIT ? OFFSET ?"
                       tenant-id search-term search-term search-term per-page offset])]
      
      {:processos processos
       :total (:count count-result)
       :page page
       :per-page per-page}))

  (count-processos-by-status [this tenant-id status]
    (let [result (jdbc/execute-one! db-conn
                   ["SELECT COUNT(*) as count FROM processos 
                     WHERE tenant_id = ? AND status = ? AND deleted_at IS NULL"
                    tenant-id status])]
      (:count result 0)))

  ;; ============================================
  ;; Implementação do DocumentoRepository
  ;; ============================================

  DocumentoRepository

  (find-documentos-by-processo [this processo-id]
    (jdbc/execute! db-conn
      ["SELECT * FROM processo_documentos 
        WHERE processo_id = ? AND deleted_at IS NULL 
        ORDER BY created_at DESC"
       processo-id]))

  (find-documento-by-id [this documento-id]
    (jdbc/execute-one! db-conn
      ["SELECT * FROM processo_documentos 
        WHERE id = ? AND deleted_at IS NULL"
       documento-id]))

  (create-documento! [this documento-data]
    (sql/insert! db-conn :processo_documentos documento-data {:return-keys true}))

  (soft-delete-documento! [this documento-id]
    (let [result (sql/update! db-conn :processo_documentos
                   {:deleted_at (java.time.Instant/now)}
                   {:id documento-id})]
      (pos? (:next.jdbc/update-count result))))

  (count-documentos-by-processo [this processo-id]
    (let [result (jdbc/execute-one! db-conn
                   ["SELECT COUNT(*) as count FROM processo_documentos 
                     WHERE processo_id = ? AND deleted_at IS NULL"
                    processo-id])]
      (:count result 0)))

  ;; ============================================
  ;; Implementação do HistoricoRepository
  ;; ============================================

  HistoricoRepository

  (add-historico! [this historico-data]
    (sql/insert! db-conn :processo_historico historico-data {:return-keys true}))

  (find-historico-by-processo [this processo-id opts]
    (let [{:keys [limit offset]} opts
          limit (or limit 50)
          offset (or offset 0)]
      (jdbc/execute! db-conn
        ["SELECT h.*, u.email as user_email, u.full_name as user_name
          FROM processo_historico h
          LEFT JOIN users u ON h.user_id = u.id
          WHERE h.processo_id = ?
          ORDER BY h.created_at DESC
          LIMIT ? OFFSET ?"
         processo-id limit offset])))

  (count-historico-by-processo [this processo-id]
    (let [result (jdbc/execute-one! db-conn
                   ["SELECT COUNT(*) as count FROM processo_historico 
                     WHERE processo_id = ?"
                    processo-id])]
      (:count result 0)))

  ;; ============================================
  ;; Implementação do ClienteRepository
  ;; ============================================

  ClienteRepository

  (find-all-clientes [this tenant-id opts]
    (println "=== [DB] find-all-clientes INICIADO ===")
    (println "[DB] tenant-id:" tenant-id)
    (println "[DB] opts:" opts)
    
    (let [{:keys [page per-page search]} opts
          page (or page 1)
          per-page (or per-page 20)
          offset (* (dec page) per-page)]
      
      (println "[DB] page:" page "per-page:" per-page "offset:" offset)
      
      (try
        (let [;; Query base
              base-query "SELECT * FROM clientes WHERE tenant_id = ? AND deleted_at IS NULL"
              
              ;; Adiciona busca se fornecida
              [query params] (if search
                              (do
                                (println "[DB] Aplicando filtro de busca:" search)
                                [(str base-query " AND (nome ILIKE ? OR cpf_cnpj ILIKE ? OR email ILIKE ?)")
                                 [tenant-id (str "%" search "%") (str "%" search "%") (str "%" search "%")]])
                              (do
                                (println "[DB] Sem filtro de busca")
                                [base-query [tenant-id]]))
              
              ;; Query de contagem
              count-query (str/replace query #"SELECT \*" "SELECT COUNT(*)")
              _ (println "[DB] Executando COUNT query...")
              total (:count (jdbc/execute-one! db-conn (into [count-query] params)))
              _ (println "[DB] Total de clientes:" total)
              
              ;; Query de dados com paginação
              final-query (str query " ORDER BY nome ASC LIMIT ? OFFSET ?")
              _ (println "[DB] Executando SELECT query...")
              clientes (jdbc/execute! db-conn (into [final-query] (concat params [per-page offset])))]
          
          (println "[DB] ✅ Query executada com sucesso!")
          (println "[DB] Clientes retornados:" (count clientes))
          
          {:clientes clientes
           :total total
           :page page
           :per-page per-page})
        (catch Exception e
          (println "[DB] ❌ EXCEÇÃO ao buscar clientes:")
          (println "[DB] Mensagem:" (.getMessage e))
          (.printStackTrace e)
          (throw e)))))

  (find-cliente-by-id [this tenant-id cliente-id]
    (jdbc/execute-one! db-conn
      ["SELECT * FROM clientes 
       WHERE id = ? AND tenant_id = ? AND deleted_at IS NULL"
       cliente-id tenant-id]))

  (find-cliente-by-cpf-cnpj [this tenant-id cpf-cnpj]
    (jdbc/execute-one! db-conn
      ["SELECT * FROM clientes 
       WHERE cpf_cnpj = ? AND tenant_id = ? AND deleted_at IS NULL"
       cpf-cnpj tenant-id]))

  (create-cliente! [this cliente-data]
    (println "=== [DB] create-cliente! INICIADO ===")
    (println "[DB] cliente-data recebido:" cliente-data)
    (println "[DB] tenant_id:" (:tenant_id cliente-data))
    (println "[DB] nome:" (:nome cliente-data))
    (println "[DB] cpf_cnpj:" (:cpf_cnpj cliente-data))
    (println "[DB] email:" (:email cliente-data))
    
    (try
      (let [result (jdbc/execute-one! db-conn
                     ["INSERT INTO clientes (tenant_id, nome, cpf_cnpj, email, telefone, endereco, created_at, updated_at)
                       VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                       RETURNING *"
                      (:tenant_id cliente-data)
                      (:nome cliente-data)
                      (:cpf_cnpj cliente-data)
                      (:email cliente-data)
                      (:telefone cliente-data)
                      (:endereco cliente-data)])]
        (println "[DB] ✅ INSERT executado com sucesso!")
        (println "[DB] Cliente criado com ID:" (:clientes/id result))
        result)
      (catch Exception e
        (println "[DB] ❌ EXCEÇÃO no INSERT:")
        (println "[DB] Mensagem:" (.getMessage e))
        (println "[DB] Causa:" (.getCause e))
        (.printStackTrace e)
        (throw e))))

  (update-cliente! [this tenant-id cliente-id updates]
    (let [set-clause (str/join ", " (map #(str (name %) " = ?") (keys updates)))
          values (concat (vals updates) [cliente-id tenant-id])
          query (str "UPDATE clientes SET " set-clause ", updated_at = NOW() 
                      WHERE id = ? AND tenant_id = ? AND deleted_at IS NULL
                      RETURNING *")]
      (jdbc/execute-one! db-conn (into [query] values))))

  (soft-delete-cliente! [this tenant-id cliente-id]
    (jdbc/execute-one! db-conn
      ["UPDATE clientes SET deleted_at = NOW() 
        WHERE id = ? AND tenant_id = ? AND deleted_at IS NULL
        RETURNING *"
       cliente-id tenant-id]))

  (count-processos-by-cliente [this cliente-id]
    (let [result (jdbc/execute-one! db-conn
                   ["SELECT COUNT(*) as count FROM processos 
                     WHERE cliente_id = ? AND deleted_at IS NULL"
                    cliente-id])]
      (:count result 0)))

  (search-clientes [this tenant-id query opts]
    (let [{:keys [page per-page]} opts
          page (or page 1)
          per-page (or per-page 20)
          offset (* (dec page) per-page)
          search-term (str "%" query "%")
          
          count-result (jdbc/execute-one! db-conn
                         ["SELECT COUNT(*) as count FROM clientes 
                           WHERE tenant_id = ? AND deleted_at IS NULL
                           AND (nome ILIKE ? OR cpf_cnpj ILIKE ? OR email ILIKE ?)"
                          tenant-id search-term search-term search-term])
          
          clientes (jdbc/execute! db-conn
                     ["SELECT * FROM clientes 
                       WHERE tenant_id = ? AND deleted_at IS NULL
                       AND (nome ILIKE ? OR cpf_cnpj ILIKE ? OR email ILIKE ?)
                       ORDER BY nome ASC
                       LIMIT ? OFFSET ?"
                      tenant-id search-term search-term search-term per-page offset])]
      
      {:clientes clientes
       :total (:count count-result 0)
       :page page
       :per-page per-page})))

;; --- FUNÇÃO CONSTRUTora ---
(defn create-repository
  ([] (->PostgresRepository @datasource nil))
  ([tenant-id] (->PostgresRepository @datasource tenant-id)))
