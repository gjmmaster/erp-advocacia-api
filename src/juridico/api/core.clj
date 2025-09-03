(ns juridico.api.core
  (:require [ring.adapter.jetty :as jetty]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [juridico.api.handlers :as h]
            [juridico.api.middleware :as mw])
  (:gen-class))

;; --- Rotas da API ---
;; Definimos as rotas da nossa aplicação usando Reitit.
;; Cada rota mapeia um método HTTP e um caminho para um handler.
(def routes
  ["/api"
   ;; Nosso middleware customizado é aplicado a todas as rotas aninhadas sob "/api".
   {:middleware [mw/wrap-db-repo]}

   ["/processos"
    {:get {:handler h/listar-processos-handler
           :name :processos/list}
     :post {:handler h/criar-processo-handler
            :name :processos/create}}]

   ["/processos/{id}"
    {:get {:handler h/obter-processo-handler
           :name :processos/get-by-id}}]])

;; --- Handler Principal da Aplicação ---
;; Criamos o handler principal do Ring com as rotas e middlewares globais.
(def app
  (ring/ring-handler
   (ring/router
    routes
    ;; Configuração para o Reitit, incluindo o middleware do Muuntaja.
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})
   ;; Handler padrão para rotas não encontradas.
   (ring/create-default-handler
    {:not-found (constantly {:status 404, :body "Rota não encontrada."})})))

;; --- Ponto de Entrada ---
;; A função -main é o ponto de entrada para rodar a aplicação.
(defn -main []
  (println "Iniciando servidor na porta 3000...")
  (jetty/run-jetty app {:port 3000 :join? false}))
