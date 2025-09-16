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
  (if-let [processo (p/obter-processo-por-id db-repo id)]
    {:status 200 :body processo}
    {:status 404 :body {:error "Processo não encontrado."}}))

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

;; --- ADICIONAR ESTES HANDLERS ---
(defn atualizar-processo-handler
  "Handler para atualizar um processo existente."
  [{:keys [db-repo body-params path-params]}]
  (if (s/valid? :juridico.api.specs/update-process-payload body-params)
    (let [id (:id path-params)
          linhas-afetadas (p/atualizar-processo db-repo id body-params)]
      (if (= 1 (:next.jdbc/update-count linhas-afetadas))
        {:status 200 :body {:message "Processo atualizado com sucesso."}}
        {:status 404 :body {:error "Processo não encontrado ou não pertence a este escritório."}}))
    {:status 400
     :body {:error "Dados de entrada inválidos."
            :details (s/explain-data :juridico.api.specs/update-process-payload body-params)}}))

(defn deletar-processo-handler
  "Handler para deletar um processo."
  [{:keys [db-repo path-params]}]
  (let [id (:id path-params)
        linhas-afetadas (p/deletar-processo db-repo id)]
    (if (= 1 (:next.jdbc/update-count linhas-afetadas))
      {:status 204 :body nil} ; 204 No Content é a resposta padrão para delete com sucesso
      {:status 404 :body {:error "Processo não encontrado ou não pertence a este escritório."}})))


;; --- HANDLERS DE PROVISIONAMENTO E AUTENTICAÇÃO (Públicos) ---

(defn provision-tenant-handler
  "Handler para o Super Admin criar um novo tenant e seu usuário Admin."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/provision-payload body-params)
    (let [resultado (p/criar-tenant-e-usuario-master db-repo body-params)]
      ;; --- MODIFICAÇÃO: Dispara o e-mail de boas-vindas ---
      (email-service/send-welcome-email (:user resultado) (:tenant resultado))
      ;; ----------------------------------------------------
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
          tenant-id (:tenants/id tenant)] ; CORREÇÃO: Usa a chave qualificada
      (if-let [user (and tenant-id (p/encontrar-usuario-por-email db-repo tenant-id email))]
        ;; Verificação de senha SEGURA usando buddy-hashers
        ;; CORREÇÃO: Acessa o hash da senha usando a chave correta :users/password_hash
        (if (hashers/check password (:users/password_hash user))
          (let [claims {:user-id (:users/id user)       ; CORREÇÃO
                        :tenant-id tenant-id
                        :role (:users/role user)         ; CORREÇÃO
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

;; --- HANDLERS DE GESTÃO DE OPERADORES (Protegidos por Role) ---

(defn listar-operadores-handler
  "Handler para o 'master' listar todos os usuários do seu tenant."
  [{:keys [db-repo identity]}]
  (let [tenant-id (:tenant-id identity)]
    {:status 200
     :body (p/listar-usuarios-do-tenant db-repo tenant-id)}))

(defn criar-operador-handler
  "Handler para o 'master' criar um novo usuário 'operador' no seu tenant."
  [{:keys [db-repo body-params identity]}]
  (if (s/valid? :juridico.api.specs/create-operator-payload body-params)
    (let [tenant-id (:tenant-id identity)
          novo-operador (p/criar-usuario-operador db-repo tenant-id body-params)]
      {:status 201
       :body (dissoc novo-operador :password_hash)}) ; Remove o hash da senha da resposta
    {:status 400
     :body {:error "Dados de entrada para criar operador são inválidos."
            :details (s/explain-data :juridico.api.specs/create-operator-payload body-params)}}))

(defn atualizar-operador-handler
  "Handler para o 'master' atualizar um operador."
  [{:keys [db-repo body-params path-params identity]}]
  (if (s/valid? :juridico.api.specs/update-operador-payload body-params)
    (let [tenant-id (:tenant-id identity)
          user-id (:id path-params)
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
        user-id (:id path-params)
        linhas-afetadas (p/deletar-operador db-repo tenant-id user-id)]
    (if (= 1 (:next.jdbc/update-count linhas-afetadas))
      {:status 204 :body nil}
      {:status 404 :body {:error "Operador não encontrado ou não pertence a este escritório."}})))

;; --- HANDLER DE DEPURAÇÃO (TEMPORÁRIO) ---
(defn secret-check-handler
  "Endpoint temporário para verificar os primeiros caracteres da JWT_SECRET."
  [request]
  {:status 200
   :body {:secret_start (subs config/jwt-secret 0 4)}})
