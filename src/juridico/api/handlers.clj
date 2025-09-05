(ns juridico.api.handlers
  (:require [juridico.api.db.protocols :as p]
            [clojure.spec.alpha :as s]
            [juridico.api.specs]))

;; --- Handlers de Processos (Já existentes) ---

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
        ;; ATENÇÃO: Verificação de senha SIMPLIFICADA apenas para a PoC.
        ;; Em um sistema real, aqui se compararia o hash da senha.
        (if (= password (:password_hash user))
          {:status 200
           :body {:message (str "Usuário " email " autenticado com sucesso.")
                  :token (str "jwt-simulado-para-" (:id user))}}
          {:status 401 :body {:error "Credenciais inválidas."}})
        {:status 401 :body {:error "Credenciais inválidas."}}))
    {:status 400
     :body {:error "Dados de login inválidos."
            :details (s/explain-data :juridico.api.specs/login-payload body-params)}}))
