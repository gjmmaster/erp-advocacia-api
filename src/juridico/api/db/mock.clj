(ns juridico.api.db.mock
  (:require [juridico.api.db.protocols :refer [AuthRepository ProcessoRepository 
                                                 DocumentoRepository HistoricoRepository 
                                                 ClienteRepository ProcessosRepository]]
            [juridico.api.db.seed :as seed]
            [buddy.hashers :as hashers]
            [buddy.core.nonce :as nonce]
            [buddy.core.codecs :as codecs]
            [clojure.string :as str])
  (:import [java.time Instant]))

;; ============================================
;; Helper Functions
;; ============================================

(defn- next-id!
  "Auto-incrementa e retorna o próximo ID para a entidade especificada."
  [db-atom counter-key]
  (let [current (get-in @db-atom [:counters counter-key])]
    (swap! db-atom update-in [:counters counter-key] inc)
    (inc current)))

(defn- add-timestamps
  "Adiciona created_at e updated_at com timestamp atual."
  [data]
  (let [now (Instant/now)]
    (assoc data
           :created_at now
           :updated_at now)))

(defn- update-timestamp
  "Atualiza apenas o updated_at com timestamp atual."
  [data]
  (assoc data :updated_at (Instant/now)))

(defn- filter-active
  "Filtra registros que não foram soft-deleted (deleted_at é nil)."
  [records]
  (filter #(nil? (:deleted_at %)) (vals records)))

(defn- matches-search?
  "Verifica se o texto contém o termo de busca (case-insensitive)."
  [text search-term]
  (when (and text search-term)
    (str/includes? (str/lower-case (str text))
                   (str/lower-case search-term))))

(defn- paginate
  "Pagina uma coleção de items retornando formato padrão."
  [items page per-page]
  (let [offset (* (dec page) per-page)
        paginated-items (take per-page (drop offset items))]
    {:items paginated-items
     :total (count items)
     :page page
     :per-page per-page}))

(defn- validate-unique-email!
  "Valida se o email já existe no sistema. Lança exceção se duplicado."
  [db-atom email & [exclude-id]]
  (let [existing (some #(and (= (:email %) email)
                            (not= (:id %) exclude-id))
                      (vals (:users @db-atom)))]
    (when existing
      (println "[MOCK] ❌ Validação falhou: Email já cadastrado -" email)
      (throw (ex-info "Email já cadastrado"
                      {:type :duplicate-email
                       :email email})))))

(defn- validate-unique-cpf-cnpj!
  "Valida se o CPF/CNPJ já existe para o tenant. Lança exceção se duplicado."
  [db-atom tenant-id cpf-cnpj & [exclude-id]]
  (let [existing (some #(and (= (:tenant_id %) tenant-id)
                            (= (:cpf_cnpj %) cpf-cnpj)
                            (nil? (:deleted_at %))
                            (not= (:id %) exclude-id))
                      (vals (:clientes @db-atom)))]
    (when existing
      (println "[MOCK] ❌ Validação falhou: CPF/CNPJ já cadastrado -" cpf-cnpj)
      (throw (ex-info "CPF/CNPJ já cadastrado"
                      {:type :duplicate-cpf-cnpj
                       :cpf-cnpj cpf-cnpj})))))

(defn- validate-unique-numero-processo!
  "Valida se o número do processo já existe para o tenant. Lança exceção se duplicado."
  [db-atom tenant-id numero-processo & [exclude-id]]
  (let [existing (some #(and (= (:tenant_id %) tenant-id)
                            (= (:numero_processo %) numero-processo)
                            (nil? (:deleted_at %))
                            (not= (:id %) exclude-id))
                      (vals (:processos @db-atom)))]
    (when existing
      (println "[MOCK] ❌ Validação falhou: Número de processo já cadastrado -" numero-processo)
      (throw (ex-info "Número de processo já cadastrado"
                      {:type :duplicate-numero-processo
                       :numero-processo numero-processo})))))

(defn- generate-secure-temp-password
  "Gera uma senha temporária criptograficamente segura de 12 caracteres alfanuméricos."
  []
  (-> (nonce/random-bytes 16)
      (codecs/bytes->b64-str)
      (str/replace #"[^a-zA-Z0-9]" "")
      (subs 0 12)))

(defn- log-operation
  "Loga uma operação do repositório mock com formatação consistente."
  [operation & args]
  (println (str "[MOCK] " operation) (str/join " " args)))

;; ============================================
;; MockRepository Implementation
;; ============================================

(defrecord MockRepository [db-atom]
  
  ;; ============================================
  ;; AuthRepository Implementation
  ;; ============================================
  
  AuthRepository
  
  (encontrar-tenant-por-subdominio [this subdominio]
    (log-operation "🔍 Buscando tenant por subdomínio:" subdominio)
    (let [result (first (filter #(= (:subdomain %) subdominio)
                               (vals (:tenants @db-atom))))]
      (if result
        (log-operation "✅ Tenant encontrado:" (:company_name result))
        (log-operation "❌ Tenant não encontrado"))
      result))
  
  (encontrar-usuario-por-email [this tenant-id email]
    (log-operation "🔍 Buscando usuário por email:" email "tenant:" tenant-id)
    (let [result (first (filter #(and (= (:tenant_id %) tenant-id)
                                     (= (:email %) email))
                               (vals (:users @db-atom))))]
      (if result
        (log-operation "✅ Usuário encontrado:" (:full_name result))
        (log-operation "❌ Usuário não encontrado"))
      result))
  
  (encontrar-usuario-por-email-global [this email]
    (log-operation "🔍 Buscando usuário por email (global):" email)
    (let [user (first (filter #(= (str/lower-case (:email %))
                                  (str/lower-case email))
                             (vals (:users @db-atom))))]
      (when user
        (let [tenant (get (:tenants @db-atom) (:tenant_id user))
              result (assoc user
                           :tenant_name (:company_name tenant)
                           :tenant_active (:is_active tenant))]
          (log-operation "✅ Usuário encontrado:" (:full_name user) "tenant:" (:company_name tenant))
          result))))
  
  (encontrar-super-admin-por-email [this email]
    (log-operation "🔍 Buscando super-admin por email:" email)
    (let [result (first (filter #(and (= (:email %) email)
                                     (= (:role %) "super-admin"))
                               (vals (:users @db-atom))))]
      (if result
        (log-operation "✅ Super-admin encontrado:" (:full_name result))
        (log-operation "❌ Super-admin não encontrado"))
      result))
  
  (criar-tenant-e-usuario-master [this {:keys [company_name email operator_limit]}]
    (log-operation "➕ Criando tenant e usuário master:" company_name email)
    (validate-unique-email! db-atom email)
    
    (let [subdomain (-> company_name str/lower-case (str/replace #"[^a-z0-9-]" "-"))
          temp-password (generate-secure-temp-password)
          tenant-id (next-id! db-atom :tenant-id)
          user-id (next-id! db-atom :user-id)
          
          new-tenant (add-timestamps
                      {:id tenant-id
                       :company_name company_name
                       :subdomain subdomain
                       :operator_limit (or operator_limit 4)
                       :is_active true})
          
          new-user (add-timestamps
                    {:id user-id
                     :tenant_id tenant-id
                     :email email
                     :password_hash (hashers/encrypt temp-password)
                     :full_name "Master User"
                     :role "master"
                     :temporary_password true})]
      
      (swap! db-atom assoc-in [:tenants tenant-id] new-tenant)
      (swap! db-atom assoc-in [:users user-id] new-user)
      
      (log-operation "✅ Tenant criado com ID:" tenant-id)
      (log-operation "✅ Usuário master criado com ID:" user-id)
      
      {:tenant {:id tenant-id
                :subdomain subdomain
                :company_name company_name}
       :user {:email email
              :temp_password temp-password}}))
  
  (listar-usuarios-do-tenant [this tenant-id]
    (log-operation "📋 Listando usuários do tenant:" tenant-id)
    (let [users (filter #(= (:tenant_id %) tenant-id)
                       (vals (:users @db-atom)))
          result (map #(select-keys % [:id :email :full_name :role]) users)]
      (log-operation "✅ Retornando" (count result) "usuários")
      result))
  
  (criar-usuario-operador [this tenant-id {:keys [email password full_name]}]
    (log-operation "➕ Criando operador:" email "tenant:" tenant-id)
    (validate-unique-email! db-atom email)
    
    (let [tenant (get (:tenants @db-atom) tenant-id)
          operator-limit (:operator_limit tenant 4)
          current-operators (count (filter #(and (= (:tenant_id %) tenant-id)
                                                (= (:role %) "operador"))
                                         (vals (:users @db-atom))))]
      
      (if (< current-operators operator-limit)
        (let [user-id (next-id! db-atom :user-id)
              new-user (add-timestamps
                        {:id user-id
                         :tenant_id tenant-id
                         :email email
                         :password_hash (hashers/encrypt password)
                         :full_name full_name
                         :role "operador"
                         :temporary_password false})]
          
          (swap! db-atom assoc-in [:users user-id] new-user)
          (log-operation "✅ Operador criado com ID:" user-id)
          new-user)
        
        (do
          (log-operation "❌ Limite de operadores atingido:" current-operators "/" operator-limit)
          (throw (ex-info "Limite de operadores atingido para este tenant."
                         {:type :limite-excedido
                          :tenant-id tenant-id
                          :limit operator-limit
                          :current current-operators}))))))
  
  (atualizar-operador [this tenant-id user-id dados-usuario]
    (log-operation "✏️ Atualizando operador:" user-id "tenant:" tenant-id)
    (let [user (get (:users @db-atom) user-id)]
      (if (and user
              (= (:tenant_id user) tenant-id)
              (= (:role user) "operador"))
        (let [updated-user (-> user
                              (merge (select-keys dados-usuario [:full_name]))
                              update-timestamp)]
          (swap! db-atom assoc-in [:users user-id] updated-user)
          (log-operation "✅ Operador atualizado")
          updated-user)
        (do
          (log-operation "❌ Operador não encontrado")
          nil))))
  
  (deletar-operador [this tenant-id user-id]
    (log-operation "🗑️ Deletando operador:" user-id "tenant:" tenant-id)
    (let [user (get (:users @db-atom) user-id)]
      (if (and user
              (= (:tenant_id user) tenant-id)
              (= (:role user) "operador"))
        (do
          (swap! db-atom update :users dissoc user-id)
          (log-operation "✅ Operador deletado")
          true)
        (do
          (log-operation "❌ Operador não encontrado")
          false))))
  
  (obter-operador-por-id [this tenant-id user-id]
    (log-operation "🔍 Buscando operador por ID:" user-id "tenant:" tenant-id)
    (let [user (get (:users @db-atom) user-id)]
      (if (and user
              (= (:tenant_id user) tenant-id))
        (do
          (log-operation "✅ Operador encontrado:" (:full_name user))
          (select-keys user [:id :email :full_name :role]))
        (do
          (log-operation "❌ Operador não encontrado")
          nil))))
  
  (listar-tenants [this]
    (log-operation "📋 Listando todos os tenants")
    (let [tenants (vals (:tenants @db-atom))
          result (map #(-> %
                          (update :id str)
                          (update :created_at str)
                          (update :updated_at str)
                          (select-keys [:id :company_name :subdomain :created_at :operator_limit]))
                     tenants)]
      (log-operation "✅ Retornando" (count result) "tenants")
      result))
  
  (obter-tenant-por-id [this tenant-id]
    (log-operation "🔍 Buscando tenant por ID:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          tenant (get (:tenants @db-atom) id)]
      (if tenant
        (let [result (-> tenant
                        (update :id str)
                        (update :created_at str)
                        (update :updated_at str)
                        (select-keys [:id :company_name :subdomain :created_at :operator_limit]))]
          (log-operation "✅ Tenant encontrado:" (:company_name tenant))
          result)
        (do
          (log-operation "❌ Tenant não encontrado")
          nil))))
  
  (atualizar-tenant [this tenant-id dados-tenant]
    (log-operation "✏️ Atualizando tenant:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          tenant (get (:tenants @db-atom) id)]
      (if tenant
        (let [updated-tenant (-> tenant
                                (merge (select-keys dados-tenant [:company_name :operator_limit]))
                                update-timestamp)]
          (swap! db-atom assoc-in [:tenants id] updated-tenant)
          (log-operation "✅ Tenant atualizado")
          updated-tenant)
        (do
          (log-operation "❌ Tenant não encontrado")
          nil))))
  
  (criar-tenant [this {:keys [company_name subdomain operator_limit]}]
    (log-operation "➕ Criando tenant:" company_name)
    (let [tenant-id (next-id! db-atom :tenant-id)
          subdomain-to-use (or subdomain
                              (-> company_name str/lower-case (str/replace #"[^a-z0-9-]" "-")))
          new-tenant (add-timestamps
                      {:id tenant-id
                       :company_name company_name
                       :subdomain subdomain-to-use
                       :operator_limit (or operator_limit 4)
                       :is_active true})]
      
      (swap! db-atom assoc-in [:tenants tenant-id] new-tenant)
      (log-operation "✅ Tenant criado com ID:" tenant-id)
      new-tenant))
  
  (deletar-tenant [this tenant-id]
    (log-operation "🗑️ Deletando tenant:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          tenant (get (:tenants @db-atom) id)]
      (if tenant
        (do
          (swap! db-atom update :tenants dissoc id)
          (log-operation "✅ Tenant deletado")
          true)
        (do
          (log-operation "❌ Tenant não encontrado")
          false))))
  
  (get-tenant-master-user [this tenant-id]
    (log-operation "🔍 Buscando usuário master do tenant:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          user (first (filter #(and (= (:tenant_id %) id)
                                   (= (:role %) "master"))
                             (vals (:users @db-atom))))]
      (if user
        (let [result (select-keys user [:id :email :role :tenant_id])]
          (log-operation "✅ Master user encontrado:" (:email user))
          result)
        (do
          (log-operation "❌ Master user não encontrado")
          nil))))
  
  (count-processos [this tenant-id]
    (log-operation "🔢 Contando processos do tenant:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          count (count (filter #(= (:tenant_id %) id)
                              (vals (:processos @db-atom))))]
      (log-operation "✅ Total de processos:" count)
      count))
  
  (count-clientes [this tenant-id]
    (log-operation "🔢 Contando clientes do tenant:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          count (count (filter #(and (= (:tenant_id %) id)
                                    (nil? (:deleted_at %)))
                              (vals (:clientes @db-atom))))]
      (log-operation "✅ Total de clientes:" count)
      count))
  
  (count-operadores [this tenant-id]
    (log-operation "🔢 Contando operadores do tenant:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          count (count (filter #(and (= (:tenant_id %) id)
                                    (= (:role %) "operador"))
                              (vals (:users @db-atom))))]
      (log-operation "✅ Total de operadores:" count)
      count))
  
  (count-processos-ativos [this tenant-id]
    (log-operation "🔢 Contando processos ativos do tenant:" tenant-id)
    (let [id (if (string? tenant-id) (Long/parseLong tenant-id) tenant-id)
          count (count (filter #(and (= (:tenant_id %) id)
                                    (= (:status %) "ativo"))
                              (vals (:processos @db-atom))))]
      (log-operation "✅ Total de processos ativos:" count)
      count))
  
  (update-user-password! [this user-id new-password-hash temporary-password]
    (log-operation "🔐 Atualizando senha do usuário:" user-id)
    (let [id (if (string? user-id) (Long/parseLong user-id) user-id)
          user (get (:users @db-atom) id)]
      (if user
        (let [updated-user (-> user
                              (assoc :password_hash new-password-hash
                                     :temporary_password temporary-password)
                              update-timestamp)]
          (swap! db-atom assoc-in [:users id] updated-user)
          (log-operation "✅ Senha atualizada")
          {:next.jdbc/update-count 1})
        (do
          (log-operation "❌ Usuário não encontrado")
          {:next.jdbc/update-count 0}))))
  
  (find-by-id [this user-id]
    (log-operation "🔍 Buscando usuário por ID:" user-id)
    (let [id (if (string? user-id) (Long/parseLong user-id) user-id)
          user (get (:users @db-atom) id)]
      (if user
        (let [result (select-keys user [:id :email :role :tenant_id])]
          (log-operation "✅ Usuário encontrado:" (:email user))
          result)
        (do
          (log-operation "❌ Usuário não encontrado")
          nil))))
  
  ;; ============================================
  ;; ProcessoRepository Implementation
  ;; ============================================
  
  ProcessoRepository
  
  (find-all-processos [this tenant-id opts]
    (log-operation "📋 Listando processos - tenant:" tenant-id "opts:" opts)
    (let [{:keys [page per-page status tipo cliente-id search]} opts
          page (or page 1)
          per-page (or per-page 20)
          
          ;; Filtra processos do tenant e ativos
          all-processos (->> (vals (:processos @db-atom))
                            (filter #(= (:tenant_id %) tenant-id))
                            (filter #(nil? (:deleted_at %))))
          
          ;; Aplica filtros
          filtered (cond->> all-processos
                     status
                     (filter #(= (:status %) status))
                     
                     tipo
                     (filter #(= (:tipo %) tipo))
                     
                     cliente-id
                     (filter #(= (:cliente_id %) cliente-id))
                     
                     search
                     (filter #(or (matches-search? (:numero_processo %) search)
                                 (matches-search? (:descricao %) search))))
          
          ;; Adiciona nome do cliente
          with-cliente (map (fn [p]
                             (let [cliente (get (:clientes @db-atom) (:cliente_id p))]
                               (assoc p :cliente_nome (:nome cliente))))
                           filtered)
          
          ;; Ordena por data de criação DESC
          sorted (sort-by :created_at #(compare %2 %1) with-cliente)
          
          ;; Pagina resultados
          result (paginate sorted page per-page)]
      
      (log-operation "✅ Retornando" (count (:items result)) "de" (:total result) "processos")
      {:processos (:items result)
       :total (:total result)
       :page page
       :per-page per-page}))
  
  (find-processo-by-id [this tenant-id processo-id]
    (log-operation "🔍 Buscando processo por ID:" processo-id "tenant:" tenant-id)
    (let [processo (get (:processos @db-atom) processo-id)]
      (if (and processo
              (= (:tenant_id processo) tenant-id)
              (nil? (:deleted_at processo)))
        (let [cliente (get (:clientes @db-atom) (:cliente_id processo))
              result (assoc processo :cliente_nome (:nome cliente))]
          (log-operation "✅ Processo encontrado:" (:numero_processo processo))
          result)
        (do
          (log-operation "❌ Processo não encontrado")
          nil))))
  
  (find-processo-by-numero [this tenant-id numero]
    (log-operation "🔍 Buscando processo por número:" numero "tenant:" tenant-id)
    (let [result (first (filter #(and (= (:tenant_id %) tenant-id)
                                     (= (:numero_processo %) numero)
                                     (nil? (:deleted_at %)))
                               (vals (:processos @db-atom))))]
      (if result
        (log-operation "✅ Processo encontrado:" (:numero_processo result))
        (log-operation "❌ Processo não encontrado"))
      result))
  
  (create-processo! [this processo-data]
    (log-operation "➕ Criando processo:" (:numero_processo processo-data))
    (validate-unique-numero-processo! db-atom
                                     (:tenant_id processo-data)
                                     (:numero_processo processo-data))
    
    (let [processo-id (next-id! db-atom :processo-id)
          new-processo (-> processo-data
                          (assoc :id processo-id)
                          (assoc :deleted_at nil :deleted_by nil)
                          add-timestamps)
          
          ;; Adiciona entrada no histórico
          historico-id (next-id! db-atom :historico-id)
          historico-entry {:id historico-id
                          :processo_id processo-id
                          :user_id (:created_by processo-data)
                          :acao "criacao"
                          :campo_alterado nil
                          :valor_anterior nil
                          :valor_novo "Processo criado"
                          :created_at (Instant/now)}]
      
      (swap! db-atom assoc-in [:processos processo-id] new-processo)
      (swap! db-atom assoc-in [:processo_historico historico-id] historico-entry)
      
      (log-operation "✅ Processo criado com ID:" processo-id)
      new-processo))
  
  (update-processo! [this tenant-id processo-id updates user-id]
    (log-operation "✏️ Atualizando processo:" processo-id "tenant:" tenant-id)
    (let [old-processo (get (:processos @db-atom) processo-id)]
      (if (and old-processo
              (= (:tenant_id old-processo) tenant-id)
              (nil? (:deleted_at old-processo)))
        (let [updated-processo (-> old-processo
                                  (merge updates)
                                  (assoc :updated_by user-id)
                                  update-timestamp)]
          
          (swap! db-atom assoc-in [:processos processo-id] updated-processo)
          
          ;; Registra alterações no histórico
          (doseq [[campo novo-valor] updates]
            (let [valor-anterior (get old-processo campo)]
              (when (not= valor-anterior novo-valor)
                (let [historico-id (next-id! db-atom :historico-id)
                      historico-entry {:id historico-id
                                      :processo_id processo-id
                                      :user_id user-id
                                      :acao "edicao"
                                      :campo_alterado (name campo)
                                      :valor_anterior (str valor-anterior)
                                      :valor_novo (str novo-valor)
                                      :created_at (Instant/now)}]
                  (swap! db-atom assoc-in [:processo_historico historico-id] historico-entry)))))
          
          (log-operation "✅ Processo atualizado")
          updated-processo)
        (do
          (log-operation "❌ Processo não encontrado")
          nil))))
  
  (soft-delete-processo! [this tenant-id processo-id user-id]
    (log-operation "🗑️ Deletando processo (soft):" processo-id "tenant:" tenant-id)
    (let [processo (get (:processos @db-atom) processo-id)]
      (if (and processo
              (= (:tenant_id processo) tenant-id)
              (nil? (:deleted_at processo)))
        (let [updated-processo (assoc processo
                                     :deleted_at (Instant/now)
                                     :deleted_by user-id)
              
              ;; Registra exclusão no histórico
              historico-id (next-id! db-atom :historico-id)
              historico-entry {:id historico-id
                              :processo_id processo-id
                              :user_id user-id
                              :acao "exclusao"
                              :campo_alterado nil
                              :valor_anterior nil
                              :valor_novo "Processo excluído"
                              :created_at (Instant/now)}]
          
          (swap! db-atom assoc-in [:processos processo-id] updated-processo)
          (swap! db-atom assoc-in [:processo_historico historico-id] historico-entry)
          
          (log-operation "✅ Processo deletado")
          true)
        (do
          (log-operation "❌ Processo não encontrado")
          false))))
  
  (search-processos [this tenant-id query opts]
    (log-operation "🔍 Buscando processos - tenant:" tenant-id "query:" query)
    (let [{:keys [page per-page]} opts
          page (or page 1)
          per-page (or per-page 20)
          
          ;; Busca em processos e clientes
          all-processos (->> (vals (:processos @db-atom))
                            (filter #(= (:tenant_id %) tenant-id))
                            (filter #(nil? (:deleted_at %))))
          
          filtered (filter (fn [p]
                            (let [cliente (get (:clientes @db-atom) (:cliente_id p))]
                              (or (matches-search? (:numero_processo p) query)
                                  (matches-search? (:descricao p) query)
                                  (matches-search? (:nome cliente) query))))
                          all-processos)
          
          ;; Adiciona nome do cliente
          with-cliente (map (fn [p]
                             (let [cliente (get (:clientes @db-atom) (:cliente_id p))]
                               (assoc p :cliente_nome (:nome cliente))))
                           filtered)
          
          ;; Ordena por data de criação DESC
          sorted (sort-by :created_at #(compare %2 %1) with-cliente)
          
          ;; Pagina resultados
          result (paginate sorted page per-page)]
      
      (log-operation "✅ Retornando" (count (:items result)) "de" (:total result) "processos")
      {:processos (:items result)
       :total (:total result)
       :page page
       :per-page per-page}))
  
  (count-processos-by-status [this tenant-id status]
    (log-operation "🔢 Contando processos por status:" status "tenant:" tenant-id)
    (let [count (count (filter #(and (= (:tenant_id %) tenant-id)
                                    (= (:status %) status)
                                    (nil? (:deleted_at %)))
                              (vals (:processos @db-atom))))]
      (log-operation "✅ Total de processos com status" status ":" count)
      count))
  
  ;; ============================================
  ;; ClienteRepository Implementation
  ;; ============================================
  
  ClienteRepository
  
  (find-all-clientes [this tenant-id opts]
    (log-operation "📋 Listando clientes - tenant:" tenant-id "opts:" opts)
    (let [{:keys [page per-page search]} opts
          page (or page 1)
          per-page (or per-page 20)
          
          ;; Filtra clientes do tenant e ativos
          all-clientes (->> (vals (:clientes @db-atom))
                           (filter #(= (:tenant_id %) tenant-id))
                           (filter #(nil? (:deleted_at %))))
          
          ;; Aplica busca se fornecida
          filtered (if search
                     (filter #(or (matches-search? (:nome %) search)
                                 (matches-search? (:cpf_cnpj %) search)
                                 (matches-search? (:email %) search))
                            all-clientes)
                     all-clientes)
          
          ;; Ordena por nome
          sorted (sort-by :nome filtered)
          
          ;; Pagina resultados
          result (paginate sorted page per-page)]
      
      (log-operation "✅ Retornando" (count (:items result)) "de" (:total result) "clientes")
      {:clientes (:items result)
       :total (:total result)
       :page page
       :per-page per-page}))
  
  (find-cliente-by-id [this tenant-id cliente-id]
    (log-operation "🔍 Buscando cliente por ID:" cliente-id "tenant:" tenant-id)
    (let [cliente (get (:clientes @db-atom) cliente-id)]
      (if (and cliente
              (= (:tenant_id cliente) tenant-id)
              (nil? (:deleted_at cliente)))
        (do
          (log-operation "✅ Cliente encontrado:" (:nome cliente))
          cliente)
        (do
          (log-operation "❌ Cliente não encontrado")
          nil))))
  
  (find-cliente-by-cpf-cnpj [this tenant-id cpf-cnpj]
    (log-operation "🔍 Buscando cliente por CPF/CNPJ:" cpf-cnpj "tenant:" tenant-id)
    (let [result (first (filter #(and (= (:tenant_id %) tenant-id)
                                     (= (:cpf_cnpj %) cpf-cnpj)
                                     (nil? (:deleted_at %)))
                               (vals (:clientes @db-atom))))]
      (if result
        (log-operation "✅ Cliente encontrado:" (:nome result))
        (log-operation "❌ Cliente não encontrado"))
      result))
  
  (create-cliente! [this cliente-data]
    (log-operation "➕ Criando cliente:" (:nome cliente-data))
    (validate-unique-cpf-cnpj! db-atom
                              (:tenant_id cliente-data)
                              (:cpf_cnpj cliente-data))
    
    (let [cliente-id (next-id! db-atom :cliente-id)
          new-cliente (-> cliente-data
                         (assoc :id cliente-id)
                         (assoc :deleted_at nil)
                         add-timestamps)]
      
      (swap! db-atom assoc-in [:clientes cliente-id] new-cliente)
      (log-operation "✅ Cliente criado com ID:" cliente-id)
      new-cliente))
  
  (update-cliente! [this tenant-id cliente-id updates]
    (log-operation "✏️ Atualizando cliente:" cliente-id "tenant:" tenant-id)
    (let [cliente (get (:clientes @db-atom) cliente-id)]
      (if (and cliente
              (= (:tenant_id cliente) tenant-id)
              (nil? (:deleted_at cliente)))
        (let [updated-cliente (-> cliente
                                 (merge updates)
                                 update-timestamp)]
          (swap! db-atom assoc-in [:clientes cliente-id] updated-cliente)
          (log-operation "✅ Cliente atualizado")
          updated-cliente)
        (do
          (log-operation "❌ Cliente não encontrado")
          nil))))
  
  (soft-delete-cliente! [this tenant-id cliente-id]
    (log-operation "🗑️ Deletando cliente (soft):" cliente-id "tenant:" tenant-id)
    (let [cliente (get (:clientes @db-atom) cliente-id)]
      (if (and cliente
              (= (:tenant_id cliente) tenant-id)
              (nil? (:deleted_at cliente)))
        (let [updated-cliente (assoc cliente :deleted_at (Instant/now))]
          (swap! db-atom assoc-in [:clientes cliente-id] updated-cliente)
          (log-operation "✅ Cliente deletado")
          updated-cliente)
        (do
          (log-operation "❌ Cliente não encontrado")
          nil))))
  
  (search-clientes [this tenant-id query opts]
    (log-operation "🔍 Buscando clientes - tenant:" tenant-id "query:" query)
    (let [{:keys [page per-page]} opts
          page (or page 1)
          per-page (or per-page 20)
          
          all-clientes (->> (vals (:clientes @db-atom))
                           (filter #(= (:tenant_id %) tenant-id))
                           (filter #(nil? (:deleted_at %))))
          
          filtered (filter #(or (matches-search? (:nome %) query)
                               (matches-search? (:cpf_cnpj %) query)
                               (matches-search? (:email %) query))
                          all-clientes)
          
          sorted (sort-by :nome filtered)
          result (paginate sorted page per-page)]
      
      (log-operation "✅ Retornando" (count (:items result)) "de" (:total result) "clientes")
      {:clientes (:items result)
       :total (:total result)
       :page page
       :per-page per-page}))
  
  (count-processos-by-cliente [this cliente-id]
    (log-operation "🔢 Contando processos do cliente:" cliente-id)
    (let [count (count (filter #(and (= (:cliente_id %) cliente-id)
                                    (nil? (:deleted_at %)))
                              (vals (:processos @db-atom))))]
      (log-operation "✅ Total de processos:" count)
      count))
  
  ;; ============================================
  ;; DocumentoRepository Implementation
  ;; ============================================
  
  DocumentoRepository
  
  (find-documentos-by-processo [this processo-id]
    (log-operation "📋 Listando documentos do processo:" processo-id)
    (let [documentos (->> (vals (:processo_documentos @db-atom))
                         (filter #(= (:processo_id %) processo-id))
                         (filter #(nil? (:deleted_at %)))
                         (sort-by :created_at #(compare %2 %1)))]
      (log-operation "✅ Retornando" (count documentos) "documentos")
      documentos))
  
  (find-documento-by-id [this documento-id]
    (log-operation "🔍 Buscando documento por ID:" documento-id)
    (let [documento (get (:processo_documentos @db-atom) documento-id)]
      (if (and documento (nil? (:deleted_at documento)))
        (do
          (log-operation "✅ Documento encontrado:" (:nome_arquivo documento))
          documento)
        (do
          (log-operation "❌ Documento não encontrado")
          nil))))
  
  (create-documento! [this documento-data]
    (log-operation "➕ Criando documento:" (:nome_arquivo documento-data))
    (let [documento-id (next-id! db-atom :documento-id)
          new-documento (-> documento-data
                           (assoc :id documento-id)
                           (assoc :deleted_at nil)
                           (assoc :created_at (Instant/now)))]
      
      (swap! db-atom assoc-in [:processo_documentos documento-id] new-documento)
      (log-operation "✅ Documento criado com ID:" documento-id)
      new-documento))
  
  (soft-delete-documento! [this documento-id]
    (log-operation "🗑️ Deletando documento (soft):" documento-id)
    (let [documento (get (:processo_documentos @db-atom) documento-id)]
      (if documento
        (let [updated-documento (assoc documento :deleted_at (Instant/now))]
          (swap! db-atom assoc-in [:processo_documentos documento-id] updated-documento)
          (log-operation "✅ Documento deletado")
          true)
        (do
          (log-operation "❌ Documento não encontrado")
          false))))
  
  (count-documentos-by-processo [this processo-id]
    (log-operation "🔢 Contando documentos do processo:" processo-id)
    (let [count (count (filter #(and (= (:processo_id %) processo-id)
                                    (nil? (:deleted_at %)))
                              (vals (:processo_documentos @db-atom))))]
      (log-operation "✅ Total de documentos:" count)
      count))
  
  ;; ============================================
  ;; HistoricoRepository Implementation
  ;; ============================================
  
  HistoricoRepository
  
  (add-historico! [this historico-data]
    (log-operation "➕ Adicionando entrada no histórico - processo:" (:processo_id historico-data))
    (let [historico-id (next-id! db-atom :historico-id)
          new-historico (assoc historico-data
                              :id historico-id
                              :created_at (Instant/now))]
      
      (swap! db-atom assoc-in [:processo_historico historico-id] new-historico)
      (log-operation "✅ Histórico criado com ID:" historico-id)
      new-historico))
  
  (find-historico-by-processo [this processo-id opts]
    (log-operation "📋 Listando histórico do processo:" processo-id)
    (let [{:keys [limit offset]} opts
          limit (or limit 50)
          offset (or offset 0)
          
          all-historico (->> (vals (:processo_historico @db-atom))
                            (filter #(= (:processo_id %) processo-id))
                            (sort-by :created_at #(compare %2 %1)))
          
          ;; Adiciona informações do usuário
          with-user (map (fn [h]
                          (let [user (get (:users @db-atom) (:user_id h))]
                            (assoc h
                                   :user_email (:email user)
                                   :user_name (:full_name user))))
                        all-historico)
          
          ;; Aplica paginação
          paginated (take limit (drop offset with-user))]
      
      (log-operation "✅ Retornando" (count paginated) "entradas de histórico")
      paginated))
  
  (count-historico-by-processo [this processo-id]
    (log-operation "🔢 Contando histórico do processo:" processo-id)
    (let [count (count (filter #(= (:processo_id %) processo-id)
                              (vals (:processo_historico @db-atom))))]
      (log-operation "✅ Total de entradas:" count)
      count))
  
  ;; ============================================
  ;; ProcessosRepository Implementation (Legacy)
  ;; ============================================
  
  ProcessosRepository
  
  (listar-processos [this]
    (log-operation "📋 Listando processos (legacy)")
    (let [processos (filter-active (:processos @db-atom))]
      (log-operation "✅ Retornando" (count processos) "processos")
      (vec processos)))
  
  (obter-processo-por-id [this id]
    (log-operation "🔍 Buscando processo por ID (legacy):" id)
    (let [processo (get (:processos @db-atom) id)]
      (if (and processo (nil? (:deleted_at processo)))
        (do
          (log-operation "✅ Processo encontrado")
          processo)
        (do
          (log-operation "❌ Processo não encontrado")
          nil))))
  
  (criar-processo [this processo]
    (log-operation "➕ Criando processo (legacy)")
    (let [processo-id (next-id! db-atom :processo-id)
          new-processo (-> processo
                          (assoc :id processo-id)
                          (assoc :deleted_at nil)
                          add-timestamps)]
      
      (swap! db-atom assoc-in [:processos processo-id] new-processo)
      (log-operation "✅ Processo criado com ID:" processo-id)
      new-processo))
  
  (atualizar-processo [this id dados-processo]
    (log-operation "✏️ Atualizando processo (legacy):" id)
    (let [processo (get (:processos @db-atom) id)]
      (if processo
        (let [updated-processo (-> processo
                                  (merge dados-processo)
                                  update-timestamp)]
          (swap! db-atom assoc-in [:processos id] updated-processo)
          (log-operation "✅ Processo atualizado")
          updated-processo)
        (do
          (log-operation "❌ Processo não encontrado")
          nil))))
  
  (deletar-processo [this id]
    (log-operation "🗑️ Deletando processo (legacy):" id)
    (let [processo (get (:processos @db-atom) id)]
      (if processo
        (do
          (swap! db-atom update :processos dissoc id)
          (log-operation "✅ Processo deletado")
          true)
        (do
          (log-operation "❌ Processo não encontrado")
          false)))))

;; ============================================
;; Constructor
;; ============================================

(defn create-mock-repository
  "Cria uma nova instância do MockRepository com dados de seed."
  []
  (let [seed-data (seed/generate-seed-data)
        db (atom seed-data)]
    
    (println "")
    (println "========================================")
    (println "[MOCK] 🚀 Repositório Mock Inicializado")
    (println "========================================")
    (println "[MOCK] 📦 Dados de seed carregados:")
    (println "[MOCK]    - Tenants:" (count (:tenants seed-data)))
    (println "[MOCK]    - Usuários:" (count (:users seed-data)))
    (println "[MOCK]    - Clientes:" (count (:clientes seed-data)))
    (println "[MOCK]    - Processos:" (count (:processos seed-data)))
    (println "[MOCK]    - Documentos:" (count (:processo_documentos seed-data)))
    (println "[MOCK]    - Histórico:" (count (:processo_historico seed-data)))
    (println "")
    (println "[MOCK] 👤 Credenciais de acesso:")
    (println "[MOCK]    Super Admin: admin@demo.com / admin123")
    (println "[MOCK]    Master User: master@demo.com / master123")
    (println "[MOCK]    Operador 1:  operador1@demo.com / operador123")
    (println "[MOCK]    Operador 2:  operador2@demo.com / operador123")
    (println "========================================")
    (println "")
    
    (->MockRepository db)))
