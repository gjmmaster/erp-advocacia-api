(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [juridico.api.handlers :as h]
            [juridico.api.middleware :as mw]
            [ring.middleware.cors :as cors]
            [ring.middleware.resource :as resource]
            [ring.util.response :as resp]
            [clojure.string :as str])
  (:gen-class))

;; --- Rotas da API (Definidas separadamente) ---
(def api-routes
  [""
   {:middleware [muuntaja/format-middleware]} ; Aplica middleware de formato a todas as rotas da API

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

;; --- Handler APENAS para a API ---
(def api-handler
  (ring/ring-handler
   (ring/router api-routes {:data {:muuntaja m/instance}})
   (ring/create-default-handler))) ; Handler para rotas de API não encontradas (404)

;; --- Handler que serve o index.html para QUALQUER rota do frontend ---
(defn spa-handler [_request]
  (-> (resp/resource-response "index.html" {:root "public"})
      (resp/content-type "text/html")))

;; --- Aplicação final com todos os middlewares ---
(def app
  (-> (fn [request]
        ;; Se a URI começa com /api, /admin, ou /auth, usa o handler da API.
        (if (re-find #"^/(api|admin|auth|debug)" (:uri request))
          (api-handler request)
          ;; Senão, é uma rota do frontend, então serve o SPA.
          (spa-handler request)))
      ;; 1. O wrap-resource roda PRIMEIRO. Se for um arquivo estático (JS/CSS), ele é servido.
      (resource/wrap-resource "public")
      ;; 2. O wrap-cors roda em seguida para todas as requisições.
      (cors/wrap-cors
       :access-control-allow-origin [#".*"]
       :access-control-allow-methods [:get :post :put :delete]
       :access-control-allow-headers #{"Content-Type" "Authorization" "X-Tenant-Subdomain"})))

;; --- Ponto de Entrada (Inalterado) ---
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println "Iniciando servidor na porta" port "...")
    (jetty/run-jetty app {:port port :join? false})))
