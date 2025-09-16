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

  ;; --- Implementação do Protocolo de Autenticação ---
  AuthRepository
  (encontrar-tenant-por-subdominio [this subdominio]
    (first (sql/query db-conn ["SELECT * FROM tenants WHERE subdomain = ?" subdominio])))

  (encontrar-usuario-por-email [this tenant-id email]
    (first (sql/query db-conn ["SELECT * FROM users WHERE tenant_id = ? AND email = ?" tenant-id email])))

  (criar-tenant-e-usuario-master [this {:keys [company_name email]}]
    (jdbc/with-transaction [tx db-conn]
      (let [subdomain (-> company_name str/lower-case (str/replace #"[^a-z0-9-]" "-"))
            temp-password (str "pass" (rand-int 10000))
            new-tenant (sql/insert! tx :tenants 
                                    {:company_name company_name :subdomain subdomain} 
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
             :user {:email email :temp_password temp-password}}))))))

;; --- FUNÇÃO CONSTRUTORA ---
(defn create-repository
  ([] (->PostgresRepository @datasource nil))
  ([tenant-id] (->PostgresRepository @datasource tenant-id)))
