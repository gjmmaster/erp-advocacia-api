(ns juridico.api.db.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [juridico.api.db.protocols :refer [ProcessosRepository AuthRepository]]
            [environ.core :refer [env]]
            [buddy.hashers :as hashers]
            [clojure.string :as str]))

;; --- Configuração da Conexão ---
;; A URL de conexão será lida da variável de ambiente DATABASE_URL
(def db-spec {:dbtype "postgresql" :dbname (env :database-url)})
(def ds (jdbc/get-datasource db-spec))

;; --- Implementação Concreta para PostgreSQL ---
(defrecord PostgresRepository [db-conn tenant-id]

  ;; --- Implementação do Protocolo de Processos ---
  ProcessosRepository
  (listar-processos [this]
    (sql/query db-conn ["SELECT * FROM legal_cases WHERE tenant_id = ?" tenant-id]))

  (obter-processo-por-id [this id]
    (first (sql/query db-conn ["SELECT * FROM legal_cases WHERE id = ? AND tenant_id = ?" id tenant-id])))

  (criar-processo [this processo]
    ;; A função `insert!` do next.jdbc já retorna o registro criado
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

            ;; Cria o tenant e recupera o ID gerado
            new-tenant (sql/insert! tx :tenants {:company_name company_name :subdomain subdomain} {:return-keys true})
            new-tenant-id (:id new-tenant)

            ;; Cria o usuário master associado ao novo tenant
            new-user (sql/insert! tx :users {:tenant_id new-tenant-id
                                             :email email
                                             :password_hash (hashers/encrypt temp-password)
                                             :role "master"} {:return-keys true})]

        ;; Retorna os dados para o handler
        {:tenant {:id new-tenant-id :subdomain subdomain :company_name company_name}
         :user {:email email :temp_password temp-password}}))))

;; --- Função Construtora ---
;; Esta função será chamada pelo middleware para criar o repositório
(defn create-repository
  ([] (->PostgresRepository ds nil))
  ([tenant-id] (->PostgresRepository ds tenant-id)))
