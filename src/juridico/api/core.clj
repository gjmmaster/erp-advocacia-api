(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [juridico.api.handlers :as h]
            [juridico.api.middleware :as mw]
            [juridico.api.rate-limit :as rl]
            [ring.middleware.cors :as cors])
  (:gen-class))

;; As rotas da API continuam as mesmas
(def api-routes
  [""
   ["/debug"
    ["/secret-check" {:get {:handler h/secret-check-handler}}]]
   ["/api"
    {:middleware [mw/wrap-public-db-repo]}
    ["/tenants"
     ["/by-subdomain/{subdomain}" {:get {:handler h/get-tenant-by-subdomain-handler}}]]]
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
                 :post {:handler h/criar-tenant-handler}}]
    ["/tenants/{id}" {:middleware [mw/wrap-public-db-repo
                                   mw/wrap-jwt-authentication
                                   mw/wrap-super-admin-authorization]
                      :get {:handler h/obter-tenant-handler}
                      :put {:handler h/atualizar-tenant-handler}
                      :delete {:handler h/deletar-tenant-handler}}]]
   ["/auth" {:middleware [mw/wrap-public-db-repo mw/wrap-tenant-context]}
    ["/login" {:post {:handler h/login-handler}}]]
   ["/api"
    {:middleware [mw/wrap-jwt-authentication]}
    ["/processos" {:get {:handler h/listar-processos-handler}
                  :post {:handler h/criar-processo-handler}}]
    ["/processos/{id}" {:get {:handler h/obter-processo-handler}
                        :put {:handler h/atualizar-processo-handler}
                        :delete {:handler h/deletar-processo-handler}}]
    ["/dashboard"
     ["/stats/{tenant-id}" {:get {:handler h/get-dashboard-stats-handler}}]]
    ["/operadores"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/listar-operadores-handler}
      :post {:handler h/criar-operador-handler}}]
    ["/operadores/{id}"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/obter-operador-handler}
      :put {:handler h/atualizar-operador-handler}
      :delete {:handler h/deletar-operador-handler}}]]])

;; Handler de fallback para rotas não encontradas
(defn not-found-handler [request]
  (println "=== ROTA NÃO ENCONTRADA ===")
  (println "URI:" (:uri request))
  (println "Method:" (:request-method request))
  {:status 404
   :body {:error "Rota não encontrada"
          :uri (:uri request)
          :method (:request-method request)}})

;; Construção da Aplicação (Apenas API)
(def app
  (-> (ring/ring-handler
       (ring/router
        api-routes
        {:data {:muuntaja m/instance
                :middleware [muuntaja/format-middleware]}})
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
    (println "Iniciando servidor API na porta" port "...")
    (jetty/run-jetty app {:port port :join? false})))
