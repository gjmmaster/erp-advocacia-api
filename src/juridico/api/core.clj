(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [juridico.api.handlers :as h]
            [juridico.api.middleware :as mw])
  (:gen-class))

;; --- Rotas da API ---
(def routes
  [""
   ;; Rota pública para o Super Admin provisionar um novo tenant.
   ;; Não usa o middleware de tenant, pois opera antes de um tenant existir.
   ["/admin"
    ["/provision-tenant"
     {:post {:handler h/provision-tenant-handler
             :name :admin/provision}}]]

   ;; Rota pública para autenticação de usuários.
   ["/auth"
    ["/login"
     {:post {:handler h/login-handler
             :name :auth/login}}]]

   ;; Rotas protegidas que exigem o X-Tenant-ID
   ["/api"
    {:middleware [mw/wrap-db-repo]}

    ["/processos"
     {:get {:handler h/listar-processos-handler
            :name :processos/list}
      :post {:handler h/criar-processo-handler
             :name :processos/create}}]

    ["/processos/{id}"
     {:get {:handler h/obter-processo-handler
            :name :processos/get-by-id}}]]
   ])

;; --- Handler Principal da Aplicação ---
(def app
  (ring/ring-handler
   (ring/router
    routes
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})
   (ring/create-default-handler
    {:not-found (constantly {:status 404, :body "Rota não encontrada."})})))

;; --- Ponto de Entrada ---
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println "Iniciando servidor na porta" port "...")
    (jetty/run-jetty app {:port port :join? false})))
