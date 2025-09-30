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
  (:gen-class)) ; <--- CORRIGIDO: Agora dentro da declaração 'ns'

;; --- Rotas da API (Estrutura Inalterada) ---
(def api-routes
  [""
   ["/debug"
    ["/secret-check" {:get {:handler h/secret-check-handler}}]]

   ["/admin"
    {:middleware [mw/wrap-jwt-authentication
                  mw/wrap-super-admin-authorization
                  mw/wrap-public-db-repo]}

    ["/provision-tenant"
     {:post {:handler h/provision-tenant-handler
             :name :admin/provision}}]

    ["/tenants"
     {:get {:handler h/listar-tenants-handler
            :name :admin/list-tenants}
      :post {:handler h/criar-tenant-handler
             :name :admin/create-tenant}}]

    ["/tenants/{id}"
     {:get {:handler h/obter-tenant-handler
            :name :admin/get-tenant}
      :put {:handler h/atualizar-tenant-handler
            :name :admin/update-tenant}
      :delete {:handler h/deletar-tenant-handler
               :name :admin/delete-tenant}}]]

   ["/auth" {:middleware [mw/wrap-public-db-repo
                          mw/wrap-tenant-context]}
    ["/login"
     {:post {:handler h/login-handler
             :name :auth/login}}]]

   ["/api"
    {:middleware [mw/wrap-jwt-authentication]}

    ["/processos"
     {:get {:handler h/listar-processos-handler
            :name :processos/list}
      :post {:handler h/criar-processo-handler
             :name :processos/create}}]

    ["/processos/{id}"
     {:get {:handler h/obter-processo-handler
            :name :processos/get-by-id}
      :put {:handler h/atualizar-processo-handler
            :name :processos/update}
      :delete {:handler h/deletar-processo-handler
               :name :processos/delete}}]
    
    ["/operadores"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/listar-operadores-handler
            :name :operadores/list}
      :post {:handler h/criar-operador-handler
             :name :operadores/create}}]

    ["/operadores/{id}"
     {:middleware [mw/wrap-master-role-authorization]
      :get {:handler h/obter-operador-handler
            :name :operadores/get-by-id}
      :put {:handler h/atualizar-operador-handler
            :name :operadores/update}
      :delete {:handler h/deletar-operador-handler
               :name :operadores/delete}}]]])

;; --- Handler que serve o index.html para rotas do frontend ---
(defn spa-handler [_request]
  (-> (resp/resource-response "index.html" {:root "public"})
      (resp/content-type "text/html")))

;; --- Handler principal que decide entre API e Frontend ---
(def main-handler
  (ring/router
   ;; As rotas de API têm prioridade
   api-routes
   {:data {:muuntaja m/instance
           :middleware [muuntaja/format-middleware]}}))

;; --- Aplicação final com todos os middlewares ---
(def app
  (-> (fn [request]
        ;; Se a rota não for encontrada no roteador da API,
        ;; entrega o controle para o handler do frontend.
        (or (main-handler request)
            (spa-handler request)))
      ;; O wrap-resource é crucial. Ele roda ANTES de tudo.
      ;; Se a requisição for para um arquivo estático (ex: /assets/index.js),
      ;; ele o serve e a requisição termina aqui.
      ;; Se não, ele passa a requisição para a função acima.
      (resource/wrap-resource "public")
      ;; Middleware de CORS
      (cors/wrap-cors
       :access-control-allow-origin [#".*"]
       :access-control-allow-methods [:get :post :put :delete]
       :access-control-allow-headers #{"Content-Type" "Authorization" "X-Tenant-Subdomain"})))

;; --- Ponto de Entrada (Inalterado) ---
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println "Iniciando servidor na porta" port "...")
    (jetty/run-jetty app {:port port :join? false})))
