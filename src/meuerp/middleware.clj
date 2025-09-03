(ns meuerp.middleware
  (:require [meuerp.db.mock :as db.mock]))

(defn wrap-db-repo
  "Middleware para injetar o repositório de banco de dados na requisição.

  Ele extrai o tenant-id do cabeçalho 'x-tenant-id', cria uma instância
  do repositório mock configurado para aquele tenant e a anexa à requisição
  na chave `:db-repo`.

  Se o cabeçalho não for encontrado, retorna uma resposta 401 Unauthorized."
  [handler]
  (fn [request]
    (if-let [tenant-id (get-in request [:headers "x-tenant-id"])]
      ;; Se o tenant-id foi encontrado, cria o repo e continua o fluxo.
      (let [repo (db.mock/create-repository tenant-id)
            request' (assoc request :db-repo repo)]
        (handler request'))
      ;; Se não, retorna um erro 401.
      {:status 401
       :headers {"Content-Type" "application/json"}
       :body "{\"error\": \"x-tenant-id header is missing.\"}"})))
