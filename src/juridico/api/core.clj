(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [juridico.api.handlers :as h]
            [juridico.api.middleware :as mw]
            [ring.middleware.cors :as cors]
            [ring.util.response :as resp]
            [ring.middleware.resource :as resource]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.not-modified :as not-modified])
  (:gen-class))

;; --- As rotas da sua API continuam exatamente as mesmas ---
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

;; --- Handler que serve o index.html para qualquer rota não encontrada na API ---
(defn spa-handler [_]
  (-> (resp/resource-response "index.html" {:root "public"})
      (resp/content-type "text/html")))

;; --- Construção da Aplicação (Lógica Reescrevida e Mais Robusta) ---
(def app
  (->
   (ring/ring-handler
    (ring/router
     api-routes
     {:data {:muuntaja m/instance
             :middleware [muuntaja/format-middleware]}})
    {:default spa-handler})
   (resource/wrap-resource "public")
   (content-type/wrap-content-type)
   (not-modified/wrap-not-modified)
   (cors/wrap-cors
    :access-control-allow-origin [#".*"]
    :access-control-allow-methods [:get :post :put :delete]
    :access-control-allow-headers #{"Content-Type" "Authorization" "X-Tenant-Subdomain"})))

;; --- Ponto de Entrada (Inalterado) ---
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println "Iniciando servidor na porta" port "...")
    (jetty/run-jetty app {:port port :join? false})))
