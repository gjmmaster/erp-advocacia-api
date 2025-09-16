(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [juridico.api.handlers :as h]
            [juridico.api.middleware :as mw])
  (:gen-class))

;; --- Rotas da API (Estrutura Corrigida) ---
(def routes
  [""
   ;; --- ROTAS DE ADMINISTRAÇÃO (sem contexto de tenant) ---
   ;; Esta rota só precisa do repositório público para criar um novo tenant.
   ["/admin" {:middleware [mw/wrap-public-db-repo]}
    ["/provision-tenant"
     {:post {:handler h/provision-tenant-handler
             :name :admin/provision}}]]

   ;; --- ROTAS DE AUTENTICAÇÃO (com contexto de tenant) ---
   ;; O Login precisa saber qual tenant o usuário está tentando acessar,
   ;; por isso mantemos os dois middlewares aqui.
   ["/auth" {:middleware [mw/wrap-public-db-repo
                          mw/wrap-tenant-context]}
    ["/login"
     {:post {:handler h/login-handler
             :name :auth/login}}]]


   ;; --- ROTAS PROTEGIDAS DA API (com autenticação JWT) ---
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
            :name :processos/get-by-id}
      ;; --- ADICIONAR ESTAS ROTAS ---
      :put {:handler h/atualizar-processo-handler
            :name :processos/update}
      :delete {:handler h/deletar-processo-handler
               :name :processos/delete}}]

    ;; --- ROTAS DE GESTÃO DE OPERADORES (acessíveis apenas pelo 'master') ---
    ["/operadores"
     {:middleware [mw/wrap-master-role-authorization] ; <--- APLICA A PROTEÇÃO DE PAPEL AQUI
      :get {:handler h/listar-operadores-handler
            :name :operadores/list}
      :post {:handler h/criar-operador-handler
             :name :operadores/create}}]

    ["/operadores/{id}"
     {:middleware [mw/wrap-master-role-authorization]
      :put {:handler h/atualizar-operador-handler
            :name :operadores/update}
      :delete {:handler h/deletar-operador-handler
               :name :operadores/delete}}]]])

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
