(ns juridico.api.handlers
  (:require [juridico.api.db.protocols :as p]
            [clojure.spec.alpha :as s]
            [juridico.api.specs]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers :as hashers]))

;; --- Handlers de Processos (Já existentes) ---
(def jwt-secret "minha-chave-secreta-super-forte-e-longa")

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

;; --- Handlers de Provisionamento e Autenticação ---

(defn provision-tenant-handler
  "Handler para o Super Admin criar um novo tenant e seu usuário Admin."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/provision-payload body-params)
    (let [resultado (p/criar-tenant-e-usuario-master db-repo body-params)]
      {:status 201
       :body (assoc resultado
                      :message "Tenant criado com sucesso. Simulação de e-mail de boas-vindas enviado.")})
    {:status 400
     :body {:error "Dados para provisionamento inválidos."
            :details (s/explain-data :juridico.api.specs/provision-payload body-params)}}))

(defn login-handler
  "Handler para autenticar um usuário (Admin ou Operador)."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/login-payload body-params)
    (let [{:keys [subdomain email password]} body-params
          tenant-dados (p/encontrar-tenant-por-subdominio db-repo subdomain)
          tenant-id (get-in tenant-dados [:dados :id])]
      (if-let [user (and tenant-id (p/encontrar-usuario-por-email db-repo tenant-id email))]
        ;; Verificação de senha SEGURA usando buddy-hashers
        ;; Em um sistema real, o :password_hash seria gerado com (hashers/encrypt password)
        (if (hashers/check password (:password_hash user))
          (let [claims {:user-id (:id user)
                        :tenant-id tenant-id
                        :role (:role user)
                        ;; Adiciona uma data de expiração (ex: 1 hora)
                        :exp (-> (java.time.Instant/now)
                                 (.plusSeconds 3600)
                                 (.getEpochSecond))}
                token (jwt/sign claims jwt-secret)]
            {:status 200
             :body {:message (str "Usuário " email " autenticado com sucesso.")
                    :token token}})
          {:status 401 :body {:error "Credenciais inválidas."}})
        {:status 401 :body {:error "Credenciais inválidas."}}))
    {:status 400
     :body {:error "Dados de login inválidos."
            :details (s/explain-data :juridico.api.specs/login-payload body-params)}}))
