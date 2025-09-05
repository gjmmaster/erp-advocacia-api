(ns juridico.api.middleware
  (:require [juridico.api.db.mock :as db.mock]))

(defn wrap-tenant-db-repo
  "Middleware para injetar o repositório ISOLADO POR TENANT na requisição.
   Lê o 'x-tenant-id' e retorna 401 se não encontrar."
  [handler]
  (fn [request]
    (if-let [tenant-id (get-in request [:headers "x-tenant-id"])]
      (let [repo (db.mock/create-repository tenant-id)
            request' (assoc request :db-repo repo)]
        (handler request'))
      {:status 401
       :headers {"Content-Type" "application/json"}
       :body "{\"error\": \"x-tenant-id header is missing.\"}"})))

;; --- INÍCIO DA MODIFICAÇÃO ---

(defn wrap-public-db-repo
  "Middleware para injetar um repositório PÚBLICO (não isolado) na requisição.
   Usado para rotas como login e provisionamento."
  [handler]
  (fn [request]
    (let [repo (db.mock/create-repository) ; Chama o construtor sem tenant-id
          request' (assoc request :db-repo repo)]
      (handler request'))))

;; --- FIM DA MODIFICAÇÃO ---
