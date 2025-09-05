(ns juridico.api.middleware
  (:require [juridico.api.db.mock :as db.mock]
            [buddy.sign.jwt :as jwt]
            [clojure.string :as str]))

;; A mesma chave secreta usada para assinar os tokens no handler.
;; Em um sistema real, esta chave deve vir de uma variável de ambiente.
(def jwt-secret "minha-chave-secreta-super-forte-e-longa")

(defn- extract-token
  "Função auxiliar para extrair o token do header 'Authorization: Bearer <token>'"
  [request]
  (some-> (get-in request [:headers "authorization"])
          (str/split #" ")
          (second)))

(defn wrap-jwt-authentication
  "Middleware que valida o token JWT e injeta o repositório com escopo e a identidade do usuário.
   Substitui o antigo 'wrap-tenant-db-repo'."
  [handler]
  (fn [request]
    (try
      (if-let [token (extract-token request)]
        ;; Valida a assinatura e a expiração do token. Se falhar, lança uma exceção.
        (let [claims (jwt/unsign token jwt-secret)
              tenant-id (:tenant-id claims)]
          (if tenant-id
            (let [repo (db.mock/create-repository tenant-id)
                  request' (-> request
                               (assoc :db-repo repo)
                               (assoc :identity claims))] ; Injeta os dados do token (user-id, tenant-id, role)
              (handler request'))
            ;; Caso de segurança: token válido, mas sem a claim 'tenant-id'
            {:status 401
             :headers {"Content-Type" "application/json"}
             :body "{\"error\": \"Token inválido: tenant-id não encontrado nas claims.\"}"}))
        ;; Caso onde o header 'Authorization' não foi encontrado ou está mal formatado
        {:status 401
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"Token de autorização não fornecido no header 'Authorization'.\"}"})
      ;; Captura exceções do 'jwt/unsign' (token inválido, expirado, etc.)
      (catch Exception _
        {:status 401
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"Token inválido ou expirado.\"}"}))))

(defn wrap-public-db-repo
  "Middleware para injetar um repositório PÚBLICO (não isolado) na requisição.
   Usado para rotas como login e provisionamento."
  [handler]
  (fn [request]
    (let [repo (db.mock/create-repository) ; Chama o construtor sem tenant-id
          request' (assoc request :db-repo repo)]
      (handler request'))))
