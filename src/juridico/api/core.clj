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
   ;; Rotas públicas para provisionamento e autenticação.
   ;; Usam o middleware público que não exige token, apenas injeta
   ;; um repositório de banco de dados sem escopo de tenant.
   ["" {:middleware [mw/wrap-public-db-repo]}

    ["/admin"
     ["/provision-tenant"
      {:post {:handler h/provision-tenant-handler
              :name :admin/provision}}]]

    ["/auth"
     ["/login"
      {:post {:handler h/login-handler
              :name :auth/login}}]]]


   ;; Rotas protegidas que exigem um JWT válido.
   ;; Usam o novo middleware que valida o token e isola o acesso
   ;; aos dados com base no tenant-id contido no token.
   ["/api"
    {:middleware [mw/wrap-jwt-authentication]}

    ["/processos"
     {:get {:handler h/listar-processos-handler
            :name :processos/list}
      :post {:handler h/criar-processo-handler
             :name :processos/create}}]

    ["/processos/{id}"
     {:get {:handler h/obter-processo-handler
            :name :processos/get-by-id}}]]])

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
