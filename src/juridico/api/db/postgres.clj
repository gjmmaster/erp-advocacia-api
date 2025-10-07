(ns juridico.api.db.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [juridico.api.db.protocols :refer [ProcessosRepository AuthRepository]]
            [environ.core :refer [env]]
            [buddy.hashers :as hashers]
            [clojure.string :as str]))

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

  (encontrar-super-admin-por-email [this email]
    (first (sql/query db-conn ["SELECT * FROM users WHERE email = ? AND role = 'super-admin'" email])))

  (criar-tenant-e-usuario-master [this {:keys [company_name email operator_limit]}]
    (jdbc/with-transaction [tx db-conn]
      (let [subdomain (-> company_name str/lower-case (str/replace #"[^a-z0-9-]" "-"))
            temp-password (str "pass" (rand-int 10000))
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
                                       :role "master"} 
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
      (mapv #(hash-map :id (:tenants/id %)
                       :company_name (:tenants/company_name %)
                       :subdomain (:tenants/subdomain %)
                       :created_at (:tenants/created_at %)
                       :operator_limit (:tenants/operator_limit %))
            results)))

  (obter-tenant-por-id [this tenant-id]
    (when-let [result (first (sql/query db-conn ["SELECT id, company_name, subdomain, created_at, operator_limit FROM tenants WHERE id = ?" tenant-id]))]
      {:id (:tenants/id result)
       :company_name (:tenants/company_name result)
       :subdomain (:tenants/subdomain result)
       :created_at (:tenants/created_at result)
       :operator_limit (:tenants/operator_limit result)}))

  (atualizar-tenant [this tenant-id dados-tenant]
    ;; Apenas company_name e operator_limit podem ser alterados
    (sql/update! db-conn :tenants
                 (select-keys dados-tenant [:company_name :operator_limit])
                 {:id tenant-id}))

  (criar-tenant [this {:keys [company_name subdomain operator_limit]}]
    (let [subdomain-to-use (or subdomain (-> company_name str/lower-case (str/replace #"[^a-z0-9-]" "-")))]
      (sql/insert! db-conn :tenants
                   {:company_name company_name
                    :subdomain subdomain-to-use
                    :operator_limit (or operator_limit 4)}
                   {:return-keys true})))

  (deletar-tenant [this tenant-id]
    (sql/delete! db-conn :tenants {:id tenant-id})))

;; --- FUNÇÃO CONSTRUTora ---
(defn create-repository
  ([] (->PostgresRepository @datasource nil))
  ([tenant-id] (->PostgresRepository @datasource tenant-id)))
