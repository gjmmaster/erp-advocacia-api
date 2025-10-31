(ns juridico.api.handlers
  (:require [juridico.api.db.protocols :as p]
            [clojure.spec.alpha :as s]
            [juridico.api.specs]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers :as hashers]
            [juridico.api.services.email :as email-service]
            [juridico.api.config :as config]
            [juridico.api.handlers.impersonation :as impersonation]))


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
  "Handler para o Super Admin criar um novo tenant e seu usuário Admin.
   MODIFICADO: Retorna senha temporária na resposta e indica se email foi enviado."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/provision-payload body-params)
    (let [resultado (p/criar-tenant-e-usuario-master db-repo body-params)
          _ (println "[PROVISION] Resultado do DB:" resultado)
          _ (println "[PROVISION] User:" (:user resultado))
          _ (println "[PROVISION] Temp password:" (:temp_password (:user resultado)))
          ;; Tentar enviar email (mas não falhar se não conseguir)
          email-sent (try
                       (email-service/send-welcome-email (:user resultado) (:tenant resultado))
                       true
                       (catch Exception e
                         (println "AVISO: Falha ao enviar email:" (.getMessage e))
                         false))
          response-body (assoc resultado
                          :message "Tenant criado com sucesso."
                          :temp_password (:temp_password (:user resultado))  ; Retornar senha temporária
                          :email_sent email-sent)
          _ (println "[PROVISION] Response body:" response-body)]
      {:status 201
       :body response-body})  ; Indicar se email foi enviado
    {:status 400
     :body {:error "Dados para provisionamento inválidos."
            :details (s/explain-data :juridico.api.specs/provision-payload body-params)}}))

(defn login-handler
  "Handler para autenticar um usuário (Admin ou Operador) de um tenant específico.
   Agora aceita 'subdomain' no body para identificar o tenant."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/login-payload body-params)
    (let [{:keys [email password subdomain]} body-params]
      
      ;; 1. Buscar tenant pelo subdomain
      (if-let [tenant (p/encontrar-tenant-por-subdominio db-repo subdomain)]
        (let [tenant-id (:tenants/id tenant)]
          
          ;; 2. Buscar usuário pelo email no tenant
          (if-let [user (p/encontrar-usuario-por-email db-repo tenant-id email)]
            
            ;; 3. Validar que usuário pertence ao tenant correto (redundante mas seguro)
            (if (= (:users/tenant_id user) tenant-id)
              
              ;; 4. Validar senha
              (if (hashers/check password (:users/password_hash user))
                
                ;; 5. Gerar JWT com tenant-id e flag de senha temporária
                (let [temporary-password (boolean (:users/temporary_password user))
                      claims {:user-id (:users/id user)
                              :email (:users/email user)
                              :tenant-id tenant-id
                              :role (:users/role user)
                              :temporary-password temporary-password  ;; ⭐ NOVO
                              :requires-password-change temporary-password  ;; ⭐ NOVO
                              :exp (-> (java.time.Instant/now)
                                       (.plusSeconds 900))} ; 15 minutos
                      token (jwt/sign claims config/jwt-secret)]
                  ;; Log quando usuário faz login com senha temporária
                  (when temporary-password
                    (println "[LOGIN] Usuário" email "fez login com senha temporária"))
                  {:status 200
                   :body {:message (str "Usuário " email " autenticado com sucesso.")
                          :token token}})
                
                ;; Senha incorreta
                {:status 401 :body {:error "Credenciais inválidas."}})
              
              ;; Usuário não pertence ao tenant
              {:status 403 :body {:error "Usuário não pertence a este escritório."}})
            
            ;; Usuário não encontrado
            {:status 401 :body {:error "Credenciais inválidas."}})
          )
        
        ;; Tenant não encontrado
        {:status 404 :body {:error "Escritório não encontrado."}}))
    
    ;; Validação de payload falhou
    {:status 400
     :body {:error "Dados de login inválidos."
            :details (s/explain-data :juridico.api.specs/login-payload body-params)}}))

(defn super-admin-login-handler
  "Handler para autenticar o Super Admin (sem tenant context)."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/super-admin-login-payload body-params)
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
            :details (s/explain-data :juridico.api.specs/super-admin-login-payload body-params)}}))

(defn login-auto-discover-handler
  "Handler para login com auto-descoberta de tenant por email.
   Não requer subdomain - mais seguro e simples.
   O sistema descobre automaticamente qual tenant o usuário pertence."
  [{:keys [db-repo body-params]}]
  (if (s/valid? :juridico.api.specs/simple-login-payload body-params)
    (let [{:keys [email password]} body-params]
      ;; 1. Buscar usuário por email em TODOS os tenants
      (if-let [user (p/encontrar-usuario-por-email-global db-repo email)]
        ;; 2. Validar que tenant está ativo
        (if (:tenant_active user)
          ;; 3. Validar senha
          (if (hashers/check password (:users/password_hash user))
            ;; 4. Gerar token com tenant-id e flag de senha temporária
            (let [temporary-password (boolean (:users/temporary_password user))
                  claims {:user-id (:users/id user)
                          :email (:users/email user)
                          :role (:users/role user)
                          :tenant-id (:users/tenant_id user)
                          :temporary-password temporary-password  ;; ⭐ NOVO
                          :requires-password-change temporary-password  ;; ⭐ NOVO
                          :exp (-> (java.time.Instant/now)
                                   (.plusSeconds 3600)
                                   (.getEpochSecond))}
                  token (jwt/sign claims config/jwt-secret)]
              ;; Log quando usuário faz login com senha temporária
              (when temporary-password
                (println "[LOGIN AUTO-DISCOVER] Usuário" email "fez login com senha temporária"))
              {:status 200
               :body {:message (str "Usuário " email " autenticado com sucesso.")
                      :token token
                      :user {:email email
                             :role (:users/role user)
                             :tenant-id (:users/tenant_id user)
                             :tenant-name (:tenant_name user)}}})
            ;; Senha incorreta
            {:status 401 :body {:error "Credenciais inválidas."}})
          ;; Tenant inativo
          {:status 403 :body {:error "Escritório inativo."}})
        ;; Usuário não encontrado
        {:status 401 :body {:error "Credenciais inválidas."}}))
    ;; Validação de payload falhou
    {:status 400
     :body {:error "Dados de login inválidos."
            :details (s/explain-data :juridico.api.specs/simple-login-payload body-params)}}))


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
  [request]
  (try
    (println "=== LISTAR TENANTS HANDLER CHAMADO ===")
    (println "Request recebido!")
    (let [db-repo (:db-repo request)]
      (println "db-repo:" db-repo)
      (if db-repo
        (do
          (println "Buscando tenants...")
          (let [tenants (p/listar-tenants db-repo)]
            (println "Tenants encontrados:" (count tenants))
            {:status 200
             :body tenants}))
        (do
          (println "ERRO: db-repo é nil!")
          {:status 500
           :body {:error "db-repo não disponível"}})))
    (catch Exception e
      (println "EXCEÇÃO em listar-tenants-handler:" (.getMessage e))
      (.printStackTrace e)
      {:status 500
       :body {:error (str "Erro: " (.getMessage e))}})))

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

(defn get-tenant-master-user-handler
  "Handler para buscar o usuário master de um tenant."
  [{:keys [db-repo path-params]}]
  (println "=== GET TENANT MASTER USER HANDLER CHAMADO ===")
  (println "Path params:" path-params)
  (println "DB-repo presente:" (boolean db-repo))
  (let [tenant-id (:tenant-id path-params)]
    (println "Tenant ID extraído:" tenant-id)
    (if-let [master-user (p/get-tenant-master-user db-repo tenant-id)]
      (do
        (println "Master user encontrado:" master-user)
        {:status 200
         :body (update master-user :id str)})  ;; Converte ID para string para evitar perda de precisão no JavaScript
      (do
        (println "Master user NÃO encontrado!")
        {:status 404
         :body {:error "Master user não encontrado para este tenant"}}))))


;; --- HANDLER DE BUSCA DE TENANT POR SUBDOMÍNIO ---
(defn get-tenant-by-subdomain-handler
  "Handler público para buscar tenant por subdomínio.
   Usado pelo frontend Next.js para validar subdomínios."
  [{:keys [db-repo path-params]}]
  (let [subdomain (:subdomain path-params)]
    (if-let [tenant (p/encontrar-tenant-por-subdominio db-repo subdomain)]
      {:status 200
       :body {:id (str (:tenants/id tenant))
              :name (:tenants/company_name tenant)
              :subdomain (:tenants/subdomain tenant)
              :active true}}
      {:status 404
       :body {:error "Tenant não encontrado"}})))

;; --- HANDLER DE ESTATÍSTICAS DO DASHBOARD ---
(defn get-dashboard-stats-handler
  "Handler para obter estatísticas do dashboard de um tenant.
   Requer autenticação e valida que usuário pertence ao tenant solicitado."
  [{:keys [db-repo path-params identity]}]
  (let [tenant-id (:tenant-id path-params)
        user-tenant-id (:tenant-id identity)]
    
    ;; Validar que usuário pertence ao tenant solicitado
    (if (= (str user-tenant-id) tenant-id)
      (let [stats {:total-processos (p/count-processos db-repo tenant-id)
                   :total-clientes (p/count-clientes db-repo tenant-id)
                   :total-operadores (p/count-operadores db-repo tenant-id)
                   :processos-ativos (p/count-processos-ativos db-repo tenant-id)}]
        {:status 200
         :body stats})
      
      {:status 403
       :body {:error "Acesso negado"}})))

;; --- HANDLER DE DEPURAÇÃO (TEMPORÁRIO) ---
(defn secret-check-handler
  "Endpoint temporário para verificar os primeiros caracteres da JWT_SECRET."
  [request]
  {:status 200
   :body {:secret_start (subs config/jwt-secret 0 4)}})


;; --- HANDLERS DE IMPERSONATION (Super Admin) ---
(def start-impersonation-handler impersonation/start-impersonation-handler)
(def stop-impersonation-handler impersonation/stop-impersonation-handler)
