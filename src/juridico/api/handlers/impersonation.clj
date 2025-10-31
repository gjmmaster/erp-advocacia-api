(ns juridico.api.handlers.impersonation
  (:require [ring.util.response :as response]
            [juridico.api.db.protocols :as p]
            [buddy.sign.jwt :as jwt]
            [juridico.api.config :as config]
            [clojure.tools.logging :as log]))

(defn- log-impersonation-event
  "Registra evento de impersonation no log"
  [event-type impersonator-id target-user-id tenant-id ip-address]
  (log/info (str "IMPERSONATION_EVENT: " event-type)
            {:impersonator_id impersonator-id
             :target_user_id target-user-id
             :tenant_id tenant-id
             :ip_address ip-address
             :timestamp (java.time.Instant/now)}))

(defn start-impersonation-handler
  "Handler para iniciar impersonation de um tenant"
  [{:keys [identity path-params db-repo] :as request}]
  (println "=== START IMPERSONATION HANDLER CHAMADO ===")
  (println "Path params:" path-params)
  (println "Identity:" identity)
  (println "DB-repo presente:" (boolean db-repo))
  
  (let [target-user-id (parse-long (:user-id path-params))
        impersonator-id (:user-id identity)
        impersonator-email (:email identity)
        impersonator-role (:role identity)
        ip-address (or (get-in request [:headers "x-forwarded-for"])
                       (get-in request [:headers "x-real-ip"])
                       (:remote-addr request))]
    
    (println "Target user ID:" target-user-id)
    (println "Impersonator ID:" impersonator-id)
    (println "Impersonator role:" impersonator-role)
    
    ;; Validar que é super-admin
    (if (not= impersonator-role "super-admin")
      (response/status (response/response {:error "Unauthorized"}) 403)
      
      ;; Buscar tenant target
      (do
        (println "Buscando usuário target no banco...")
        (if-let [target-user (p/find-by-id db-repo target-user-id)]
          (do
            (println "Usuário encontrado:" target-user)
            (if (not= (:role target-user) "tenant")
              (response/status (response/response {:error "Can only impersonate tenants"}) 400)
              
              ;; Gerar JWT especial
              (let [jwt-claims {:user-id (:id target-user)
                               :email (:email target-user)
                               :role "tenant"
                               :tenant-id (:tenant_id target-user)
                               :impersonating true
                               :impersonator-id impersonator-id
                               :impersonator-email impersonator-email
                               :exp (-> (java.time.Instant/now)
                                        (.plusSeconds 3600)
                                        (.getEpochSecond))}
                    token (jwt/sign jwt-claims config/jwt-secret)]
                
                ;; Log do evento
                (log-impersonation-event "IMPERSONATE_START"
                                        impersonator-id
                                        target-user-id
                                        (:tenant_id target-user)
                                        ip-address)
                
                (response/response {:token token
                                   :user {:id (:id target-user)
                                         :email (:email target-user)
                                         :role "tenant"
                                         :tenant-id (:tenant_id target-user)
                                         :impersonating true
                                         :impersonator-email impersonator-email}}))))
          
          ;; Usuário não encontrado
          (do
            (println "ERRO: Usuário não encontrado no banco!")
            (response/status (response/response {:error "User not found"}) 404)))))))

(defn stop-impersonation-handler
  "Handler para parar impersonation e voltar para super admin"
  [{:keys [identity db-repo] :as request}]
  (let [impersonating? (:impersonating identity)
        impersonator-id (:impersonator-id identity)
        target-user-id (:user-id identity)
        tenant-id (:tenant-id identity)
        ip-address (or (get-in request [:headers "x-forwarded-for"])
                       (get-in request [:headers "x-real-ip"])
                       (:remote-addr request))]
    
    ;; Validar que está em modo impersonation
    (if (not impersonating?)
      (response/status (response/response {:error "Not in impersonation mode"}) 400)
      
      ;; Buscar dados do super admin
      (if-let [admin-user (p/find-by-id db-repo impersonator-id)]
        (let [jwt-claims {:user-id (:id admin-user)
                         :email (:email admin-user)
                         :role "super-admin"
                         :exp (-> (java.time.Instant/now)
                                  (.plusSeconds 3600)
                                  (.getEpochSecond))}
              token (jwt/sign jwt-claims config/jwt-secret)]
          
          ;; Log do evento
          (log-impersonation-event "IMPERSONATE_STOP"
                                  impersonator-id
                                  target-user-id
                                  tenant-id
                                  ip-address)
          
          (response/response {:token token
                             :user {:id (:id admin-user)
                                   :email (:email admin-user)
                                   :role "super-admin"}}))
        
        ;; Admin não encontrado (não deveria acontecer)
        (response/status (response/response {:error "Admin user not found"}) 404)))))
