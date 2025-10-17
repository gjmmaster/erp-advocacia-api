(ns juridico.api.handlers
  (:require [juridico.api.db.protocols :as p]
            [clojure.spec.alpha :as s]
            [juridico.api.specs]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers :as hashers]
            [juridico.api.services.email :as email-service]
            [juridico.api.config :as config]))


;; --- HANDLERS DE PROCESSOS (Protegidos por JWT) ---

(defn listar-processos-handler
  "Handler para listar todos os processos do tenant."
  [{:keys [db-repo]}]
  {:status 200
   :body (p/listar-processos db-repo)})

(defn obter-processo-handler
  "Handler para obter um processo específico por ID."
  [{{:keys [id]} :path-params
    :keys [db-repo]}]
  (let [process-id (Long/parseLong id)]
    (if-let [processo (p/obter-processo-por-id db-repo process-id)]
      {:status 200 :body processo}
      {:status 404 :body {:error "Processo não encontrado."}})))

(defn criar-processo-handler
  "Handler para criar um novo processo."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/create-process-payload body-params)
    (let [novo-processo (p/criar-processo db-repo body-params)]
      {:status 201
       :body novo-processo})
    {:status 400
     :body {:error "Dados de entrada inválidos."
            :details (s/explain-data :juridico.api.specs/create-process-payload body-params)}}))

(defn atualizar-processo-handler
  "Handler para atualizar um processo existente."
  [{:keys [db-repo body-params path-params]}]
  (if (s/valid? :juridico.api.specs/update-process-payload body-params)
    (let [process-id (Long/parseLong (:id path-params))
          linhas-afetadas (p/atualizar-processo db-repo process-id body-params)]
      (if (= 1 (:next.jdbc/update-count linhas-afetadas))
        {:status 200 :body {:message "Processo atualizado com sucesso."}}
        {:status 404 :body {:error "Processo não encontrado ou não pertence a este escritório."}}))
    {:status 400
     :body {:error "Dados de entrada inválidos."
            :details (s/explain-data :juridico.api.specs/update-process-payload body-params)}}))

(defn deletar-processo-handler
  "Handler para deletar um processo."
  [{:keys [db-repo path-params]}]
  (let [process-id (Long/parseLong (:id path-params))
        linhas-afetadas (p/deletar-processo db-repo process-id)]
    (if (= 1 (:next.jdbc/update-count linhas-afetadas))
      {:status 204 :body nil}
      {:status 404 :body {:error "Processo não encontrado ou não pertence a este escritório."}})))


;; --- HANDLERS DE PROVISIONAMENTO E AUTENTICAÇÃO (Públicos) ---

(defn provision-tenant-handler
  "Handler para o Super Admin criar um novo tenant e seu usuário Admin."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/provision-payload body-params)
    (let [resultado (p/criar-tenant-e-usuario-master db-repo body-params)]
      (email-service/send-welcome-email (:user resultado) (:tenant resultado))
      {:status 201
       :body (assoc resultado
                      :message "Tenant criado com sucesso. E-mail de boas-vindas enviado.")})
    {:status 400
     :body {:error "Dados para provisionamento inválidos."
            :details (s/explain-data :juridico.api.specs/provision-payload body-params)}}))

(defn login-handler
  "Handler para autenticar um usuário (Admin ou Operador)."
  [{:keys [db-repo body-params tenant]}]
  (if (s/valid? :juridico.api.specs/login-payload body-params)
    (let [{:keys [email password]} body-params
          tenant-id (:tenants/id tenant)]
      (if-let [user (and tenant-id (p/encontrar-usuario-por-email db-repo tenant-id email))]
        (if (hashers/check password (:users/password_hash user))
          (let [claims {:user-id (:users/id user)
                        :tenant-id tenant-id
                        :role (:users/role user)
                        :exp (-> (java.time.Instant/now)
                                 (.plusSeconds 3600)
                                 (.getEpochSecond))}
                token (jwt/sign claims config/jwt-secret)]
            {:status 200
             :body {:message (str "Usuário " email " autenticado com sucesso.")
                    :token token}})
          {:status 401 :body {:error "Credenciais inválidas."}})
        {:status 401 :body {:error "Credenciais inválidas."}}))
    {:status 400
     :body {:error "Dados de login inválidos."
            :details (s/explain-data :juridico.api.specs/login-payload body-params)}}))

(defn super-admin-login-handler
  "Handler para autenticar o Super Admin (sem tenant context)."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/login-payload body-params)
    (let [{:keys [email password]} body-params]
      (if-let [user (p/encontrar-super-admin-por-email db-repo email)]
        (if (hashers/check password (:users/password_hash user))
          (let [claims {:user-id (:users/id user)
                        :role (:users/role user)
                        :exp (-> (java.time.Instant/now)
                                 (.plusSeconds 3600)
                                 (.getEpochSecond))}
                token (jwt/sign claims config/jwt-secret)]
            {:status 200
             :body {:message (str "Super Admin " email " autenticado com sucesso.")
                    :token token}})
          {:status 401 :body {:error "Credenciais inválidas."}})
        {:status 401 :body {:error "Credenciais inválidas."}}))
    {:status 400
     :body {:error "Dados de login inválidos."
            :details (s/explain-data :juridico.api.specs/login-payload body-params)}}))


;; --- HANDLERS DE GESTÃO DE OPERADORES (Protegidos por Role 'master') ---

(defn listar-operadores-handler
  "Handler para o 'master' listar todos os usuários do seu tenant."
  [{:keys [db-repo identity]}]
  (let [tenant-id (:tenant-id identity)]
    {:status 200
     :body (p/listar-usuarios-do-tenant db-repo tenant-id)}))

(defn obter-operador-handler
  "Handler para obter um operador específico por ID."
  [{:keys [db-repo path-params identity]}]
  (let [tenant-id (:tenant-id identity)
        user-id (Long/parseLong (:id path-params))]
    (if-let [operador (p/obter-operador-por-id db-repo tenant-id user-id)]
      {:status 200 :body operador}
      {:status 404 :body {:error "Operador não encontrado."}})))

(defn criar-operador-handler
  "Handler para o 'master' criar um novo usuário 'operador' no seu tenant."
  [{:keys [db-repo body-params identity]}]
  (if (s/valid? :juridico.api.specs/create-operator-payload body-params)
    (try
      (let [tenant-id (:tenant-id identity)
            novo-operador (p/criar-usuario-operador db-repo tenant-id body-params)]
        {:status 201
         :body (dissoc novo-operador :password_hash)})
      (catch clojure.lang.ExceptionInfo e
        (let [data (ex-data e)]
          (if (= (:type data) :limite-excedido)
            {:status 409 ; Conflict
             :body {:error "Limite de operadores atingido."
                    :details (str "O limite de " (:limit data) " operadores para este escritório foi atingido.")}}
            (throw e)))))
    {:status 400
     :body {:error "Dados de entrada para criar operador são inválidos."
            :details (s/explain-data :juridico.api.specs/create-operator-payload body-params)}}))

(defn atualizar-operador-handler
  "Handler para o 'master' atualizar um operador."
  [{:keys [db-repo body-params path-params identity]}]
  (if (s/valid? :juridico.api.specs/update-operador-payload body-params)
    (let [tenant-id (:tenant-id identity)
          user-id (Long/parseLong (:id path-params))
          linhas-afetadas (p/atualizar-operador db-repo tenant-id user-id body-params)]
      (if (= 1 (:next.jdbc/update-count linhas-afetadas))
        {:status 200 :body {:message "Operador atualizado com sucesso."}}
        {:status 404 :body {:error "Operador não encontrado ou não pertence a este escritório."}}))
    {:status 400
     :body {:error "Dados de entrada inválidos."
            :details (s/explain-data :juridico.api.specs/update-operador-payload body-params)}}))

(defn deletar-operador-handler
  "Handler para o 'master' deletar um operador."
  [{:keys [db-repo path-params identity]}]
  (let [tenant-id (:tenant-id identity)
        user-id (Long/parseLong (:id path-params))
        linhas-afetadas (p/deletar-operador db-repo tenant-id user-id)]
    (if (= 1 (:next.jdbc/update-count linhas-afetadas))
      {:status 204 :body nil}
      {:status 404 :body {:error "Operador não encontrado ou não pertence a este escritório."}})))


;; --- HANDLERS DE GESTÃO DE TENANTS (Super Admin) ---

(defn listar-tenants-handler
  "Handler para o Super Admin listar todos os tenants."
  [{:keys [db-repo] :as request}]
  (println "=== LISTAR TENANTS HANDLER ===")
  (println "Request keys:" (keys request))
  (println "db-repo presente?" (some? db-repo))
  (println "db-repo valor:" db-repo)
  (if db-repo
    (let [tenants (p/listar-tenants db-repo)]
      (println "Tenants encontrados:" (count tenants))
      {:status 200
       :body tenants})
    (do
      (println "ERRO: db-repo é nil!")
      {:status 500
       :body {:error "Erro interno: db-repo não disponível"}})))

(defn obter-tenant-handler
  "Handler para o Super Admin obter um tenant por ID."
  [{:keys [db-repo path-params]}]
  (println "=== OBTER TENANT HANDLER ===")
  (println "Path params:" path-params)
  (println "ID do path:" (:id path-params))
  (let [tenant-id (:id path-params)] ; UUID como string
    (println "Tenant ID (UUID string):" tenant-id)
    (if-let [tenant (p/obter-tenant-por-id db-repo tenant-id)]
      (do
        (println "Tenant encontrado:" tenant)
        {:status 200 :body tenant})
      (do
        (println "Tenant NÃO encontrado!")
        {:status 404 :body {:error "Tenant não encontrado."}}))))

(defn criar-tenant-handler
  "Handler para o Super Admin criar um novo tenant."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/create-tenant-payload body-params)
    (let [novo-tenant (p/criar-tenant db-repo body-params)]
      {:status 201
       :body novo-tenant})
    {:status 400
     :body {:error "Dados de entrada inválidos."
            :details (s/explain-data :juridico.api.specs/create-tenant-payload body-params)}}))

(defn atualizar-tenant-handler
  "Handler para o Super Admin atualizar um tenant."
  [{:keys [db-repo body-params path-params]}]
  (println "=== ATUALIZAR TENANT HANDLER ===")
  (println "Path params:" path-params)
  (println "Body params:" body-params)
  (println "ID do path:" (:id path-params))
  (if (s/valid? :juridico.api.specs/update-tenant-payload body-params)
    (let [tenant-id (:id path-params)] ; UUID como string
      (println "Tenant ID (UUID string):" tenant-id)
      (println "Chamando p/atualizar-tenant com:" tenant-id body-params)
      (let [linhas-afetadas (p/atualizar-tenant db-repo tenant-id body-params)]
        (println "Linhas afetadas:" linhas-afetadas)
        (if (= 1 (:next.jdbc/update-count linhas-afetadas))
          {:status 200 :body {:message "Tenant atualizado com sucesso."}}
          {:status 404 :body {:error "Tenant não encontrado."}})))
    (do
      (println "Validação falhou!")
      (println "Detalhes:" (s/explain-data :juridico.api.specs/update-tenant-payload body-params))
      {:status 400
       :body {:error "Dados de entrada inválidos."
              :details (s/explain-data :juridico.api.specs/update-tenant-payload body-params)}})))

(defn deletar-tenant-handler
  "Handler para o Super Admin deletar um tenant."
  [{:keys [db-repo path-params]}]
  (println "=== DELETAR TENANT HANDLER ===")
  (println "Path params:" path-params)
  (println "ID do path:" (:id path-params))
  (let [tenant-id (:id path-params)] ; UUID como string
    (println "Tenant ID (UUID string):" tenant-id)
    (println "Chamando p/deletar-tenant com:" tenant-id)
    (let [linhas-afetadas (p/deletar-tenant db-repo tenant-id)]
      (println "Linhas afetadas:" linhas-afetadas)
      (if (= 1 (:next.jdbc/update-count linhas-afetadas))
        {:status 204 :body nil}
        {:status 404 :body {:error "Tenant não encontrado."}}))))


;; --- HANDLER DE DEPURAÇÃO (TEMPORÁRIO) ---
(defn secret-check-handler
  "Endpoint temporário para verificar os primeiros caracteres da JWT_SECRET."
  [request]
  {:status 200
   :body {:secret_start (subs config/jwt-secret 0 4)}})
