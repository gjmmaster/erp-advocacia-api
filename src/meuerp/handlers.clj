(ns meuerp.handlers
  (:require [meuerp.db.protocols :as p]))

(defn listar-processos-handler
  "Handler para listar todos os processos do tenant."
  [{:keys [db-repo]}]
  {:status 200
   :body (p/listar-processos db-repo)})

(defn obter-processo-handler
  "Handler para obter um processo específico por ID."
  [{{:keys [id]} :path-params
    :keys [db-repo]}]
  (if-let [processo (p/obter-processo-por-id db-repo id)]
    {:status 200 :body processo}
    {:status 404 :body {:error "Processo não encontrado."}}))

(defn criar-processo-handler
  "Handler para criar um novo processo."
  [{:keys [db-repo body-params]}]
  (let [novo-processo (p/criar-processo db-repo body-params)]
    {:status 201
     :body novo-processo}))
