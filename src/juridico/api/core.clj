(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [juridico.api.handlers :as h]
            [juridico.api.middleware :as mw]
            [ring.middleware.cors :as cors])
  (:gen-class))

;; As rotas da API continuam as mesmas
(def api-routes
  [""
   ["/debug"
    ["/secret-check" {:get {:handler h/secret-check-handler}}]]
   ["/admin"
    {:middleware [mw/wrap-jwt-authentication
                  mw/wrap-super-admin-authorization
                  mw/wrap-public-db-repo]}
    ["/provision-tenant" {:post {:handler h/provision-tenant-handler}}]
    ["/tenants" {:get {:handler h/listar-tenants-handler}
                :post {:handler h/criar-tenant-handler}}]
    ["/tenants/{id}" {:get {:handler h/obter-tenant-handler}
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
    ["/operadores"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/listar-operadores-handler}
      :post {:handler h/criar-operador-handler}}]
    ["/operadores/{id}"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/obter-operador-handler}
      :put {:handler h/atualizar-operador-handler}
      :delete {:handler h/deletar-operador-handler}}]]])

;; Construção da Aplicação (Apenas API)
(def app
  (-> (ring/ring-handler
       (ring/router
        api-routes
        {:data {:muuntaja m/instance
                :middleware [muuntaja/format-middleware]}}))
      ;; CORS é essencial para permitir que o frontend (em outro domínio) acesse a API
      (cors/wrap-cors
       :access-control-allow-origin [#".*"]
       :access-control-allow-methods [:get :post :put :delete]
       :access-control-allow-headers #{"Content-Type" "Authorization" "X-Tenant-Subdomain"})))

;; Ponto de Entrada
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println "Iniciando servidor API na porta" port "...")
    (jetty/run-jetty app {:port port :join? false})))
