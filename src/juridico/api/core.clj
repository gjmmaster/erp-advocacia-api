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
   ;; Usam uma cadeia de middlewares:
   ;; 1. `wrap-public-db-repo`: Injeta um repositório de BD sem escopo de tenant.
   ;; 2. `wrap-tenant-context`: Identifica o tenant pelo subdomínio do Host e o injeta na requisição.
   ["" {:middleware [mw/wrap-public-db-repo
                     mw/wrap-tenant-context]} 

    ["/admin"
     ["/provision-tenant"
      {:post {:handler h/provision-tenant-handler
              :name :admin/provision}}]]

    ["/auth"
     ["/login"
      {:post {:handler h/login-handler
              :name :auth/login}}]]]


   ;; Rotas protegidas que exigem um JWT válido.
   ;; Usam o middleware que valida o token e isola o acesso
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
    {:not-found (constantly {:status 404, :body "{\"error\": \"Rota não encontrada.\"}"})})))

;; --- Ponto de Entrada ---
(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println "Iniciando servidor na porta" port "...")
    (jetty/run-jetty app {:port port :join? false})))
