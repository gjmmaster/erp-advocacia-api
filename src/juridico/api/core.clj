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
   ;; --- INÍCIO DA CORREÇÃO ---

   ;; Rotas públicas para provisionamento e autenticação.
   ;; Usam o middleware público que não exige 'x-tenant-id'.
   ["" {:middleware [mw/wrap-public-db-repo]}

    ["/admin"
     ["/provision-tenant"
      {:post {:handler h/provision-tenant-handler
              :name :admin/provision}}]]

    ["/auth"
     ["/login"
      {:post {:handler h/login-handler
              :name :auth/login}}]]]


   ;; Rotas protegidas que exigem o X-Tenant-ID.
   ;; Usam o middleware que isola por tenant.
   ["/api"
    {:middleware [mw/wrap-tenant-db-repo]} ;<-- Nome do middleware corrigido

    ["/processos"
     {:get {:handler h/listar-processos-handler
            :name :processos/list}
      :post {:handler h/criar-processo-handler
             :name :processos/create}}]

    ["/processos/{id}"
     {:get {:handler h/obter-processo-handler
            :name :processos/get-by-id}}]]
   ;; --- FIM DA CORREÇÃO ---
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
