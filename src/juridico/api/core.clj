(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.multipart :as multipart]
            [muuntaja.core :as m]
            [ring.util.response :as response]
            [juridico.api.handlers :as h]
            [juridico.api.handlers.password :as pwd]
            [juridico.api.handlers.processos :as processos]  ;; ⭐ NOVO
            [juridico.api.handlers.users :as users]  ;; ⭐ NOVO
            [juridico.api.middleware :as mw]
            [juridico.api.rate-limit :as rl]
            [ring.middleware.cors :as cors])
  (:gen-class))

;; Deploy: 2025-10-29 - Fix backend connectivity

;; As rotas da API continuam as mesmas
(def api-routes
  [""
   ["/debug"
    ["/secret-check" {:get {:handler h/secret-check-handler}}]]
   ["/api"
    {:middleware [mw/wrap-public-db-repo]}
    ["/tenants"
     ["/by-subdomain/:subdomain" {:get {:handler h/get-tenant-by-subdomain-handler}}]]
    ["/auth"
     ["/login" {:post {:handler h/login-auto-discover-handler}}]
     ["/change-password" {:middleware [mw/wrap-public-db-repo  ;; ⭐ Adicionar db-repo
                                       mw/wrap-jwt-authentication]  ;; ⭐ Requer autenticação
                          :post {:handler pwd/change-password-handler}}]]
    ["/super-admin"
     ["/login" {:post {:handler h/super-admin-login-handler}}]]]
   ["/admin"
    {:middleware [mw/wrap-public-db-repo]}
    ["/login" {:post {:handler h/super-admin-login-handler}}]
    ["/provision-tenant" {:middleware [mw/wrap-jwt-authentication
                                       mw/wrap-super-admin-authorization]
                          :post {:handler h/provision-tenant-handler}}]
    ["/tenants" {:middleware [mw/wrap-public-db-repo
                              mw/wrap-jwt-authentication
                              mw/wrap-super-admin-authorization]
                 :get {:handler h/listar-tenants-handler}
                 :post {:handler h/provision-tenant-handler}}]
    ;; Rota mais específica deve vir ANTES da rota genérica
    ["/tenants/:id/master-user" {:middleware [mw/wrap-public-db-repo
                                              mw/wrap-jwt-authentication
                                              mw/wrap-super-admin-authorization]
                                 :get {:handler h/get-tenant-master-user-handler}}]
    ["/tenants/:id" {:middleware [mw/wrap-public-db-repo
                                  mw/wrap-jwt-authentication
                                  mw/wrap-super-admin-authorization]
                     :get {:handler h/obter-tenant-handler}
                     :put {:handler h/atualizar-tenant-handler}
                     :delete {:handler h/deletar-tenant-handler}}]
    ;; Rotas de Impersonation
    ["/impersonate/:user-id" {:middleware [mw/wrap-public-db-repo
                                           mw/wrap-jwt-authentication
                                           mw/wrap-super-admin-authorization]
                              :post {:handler h/start-impersonation-handler}}]
    ["/stop-impersonate" {:middleware [mw/wrap-public-db-repo
                                       mw/wrap-jwt-authentication]
                          :post {:handler h/stop-impersonation-handler}}]]
   ["/auth" {:middleware [mw/wrap-public-db-repo mw/wrap-tenant-context]}
    ["/login" {:post {:handler h/login-handler}}]]
   ["/api"
    {:middleware [multipart/multipart-middleware  ;; ⭐ Multipart ANTES de JWT
                  mw/wrap-jwt-authentication]}
    
    ;; Rotas antigas de processos (manter compatibilidade)
    ["/processos" {:get {:handler h/listar-processos-handler}
                  :post {:handler h/criar-processo-handler}}]
    ["/processos/:id" {:get {:handler h/obter-processo-handler}
                       :put {:handler h/atualizar-processo-handler}
                       :delete {:handler h/deletar-processo-handler}}]
    
    ;; Novas rotas de gestão de processos
    ["/tenant"
     {:middleware [mw/wrap-public-db-repo]}
     
     ;; Processos
     ["/processos"
      ["" {:get {:handler processos/list-processos-handler}
           :post {:handler processos/create-processo-handler}}]
      ["/:id" {:get {:handler processos/get-processo-handler}
               :put {:handler processos/update-processo-handler}
               :delete {:handler processos/delete-processo-handler}}]
      
      ;; Documentos de um processo
      ["/:processo-id/documentos"
       ["" {:get {:handler processos/list-documentos-handler}
            :post {:handler processos/upload-documento-handler}}]
       ["/:documento-id" {:get {:handler processos/download-documento-handler}
                          :delete {:handler processos/delete-documento-handler}}]]
      
      ;; Histórico de um processo
      ["/:processo-id/historico"
       {:get {:handler processos/get-historico-handler}}]]
     
     ;; Clientes
     ["/clientes"
      ["" {:get {:handler processos/list-clientes-handler}
           :post {:handler processos/create-cliente-handler}}]
      ["/:id" {:get {:handler processos/get-cliente-handler}
               :put {:handler processos/update-cliente-handler}
               :delete {:handler processos/delete-cliente-handler}}]]

     ;; Usuários
     ["/users"
      ["" {:get {:handler users/list-users-handler}
           :post {:handler users/create-user-handler}}]
      ["/:id" {:get {:handler users/get-user-handler}
               :put {:handler users/update-user-handler}
               :delete {:handler users/delete-user-handler}}]
      ["/:id/reset-password" {:post {:handler users/reset-user-password-handler}}]]]]
    
    ["/dashboard"
     ["/stats/:tenant-id" {:get {:handler h/get-dashboard-stats-handler}}]]
    
    ["/operadores"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/listar-operadores-handler}
      :post {:handler h/criar-operador-handler}}]
    ["/operadores/:id"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/obter-operador-handler}
      :put {:handler h/atualizar-operador-handler}
      :delete {:handler h/deletar-operador-handler}}]]])

;; Handler de fallback para rotas não encontradas
(defn not-found-handler [request]
  (println "=== ROTA NÃO ENCONTRADA ===")
  (println "URI:" (:uri request))
  (println "Method:" (:request-method request))
  (println "Request completo:" request)
  {:status 404
   :headers {"Content-Type" "application/json"}
   :body (m/encode "application/json" {:error "Rota não encontrada"
                                        :uri (:uri request)
                                        :method (name (:request-method request))})})

;; Construção da Aplicação (Apenas API)
(def app
  (-> (ring/ring-handler
       (ring/router
        api-routes
        {:data {:muuntaja m/instance
                :middleware [multipart/multipart-middleware  ;; ⭐ Multipart PRIMEIRO
                            muuntaja/format-middleware]}})
       (ring/create-default-handler
        {:not-found not-found-handler}))
      ;; CORS é essencial para permitir que o frontend (em outro domínio) acesse a API
      (cors/wrap-cors
       :access-control-allow-origin [#".*"]
       :access-control-allow-methods [:get :post :put :delete]
       :access-control-allow-headers #{"Content-Type" "Authorization" "X-Tenant-Subdomain"})
      ;; Rate limiting para proteger endpoints de login
      rl/wrap-rate-limit-login
      ;; Tratamento global de erros (deve ser o último middleware)
      rl/wrap-global-error-handler))

;; Ponto de Entrada
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println "=== INICIANDO SERVIDOR API ===")
    (println "Porta:" port)
    (println "Rotas registradas:")
    (println "  GET  /admin/tenants")
    (println "  GET  /admin/tenants/:id/master-user")
    (println "  GET  /admin/tenants/:id")
    (println "  POST /admin/impersonate/:user-id")
    (println "  POST /admin/stop-impersonate")
    (println "==============================")
    (jetty/run-jetty app {:port port :join? false})))
