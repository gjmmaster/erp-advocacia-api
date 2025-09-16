(ns juridico.api.middleware
  (:require [juridico.api.db.postgres :as db]
            [juridico.api.db.protocols :as p]
            [buddy.sign.jwt :as jwt]
            [clojure.string :as str]))

(def jwt-secret (or (System/getenv "JWT_SECRET") "chave-padrao-para-desenvolvimento-segura"))

(defn wrap-tenant-context
  "Middleware que identifica o tenant a partir de um cabeçalho customizado (para teste) ou do subdomínio."
  [handler]
  (fn [{:keys [db-repo headers] :as request}]
    ;; --- LÓGICA DE TESTE ADICIONADA AQUI ---
    ;; 1. Tenta pegar o subdomínio de um cabeçalho customizado 'X-Tenant-Subdomain'.
    ;; 2. Se não existir, pega do cabeçalho 'Host' como antes.
    (let [host (get headers "host")
          subdomain-from-header (get headers "x-tenant-subdomain")
          subdomain (or subdomain-from-header (first (str/split host #"\.")))]
      
      (if (and host subdomain)
        (if-let [tenant (p/encontrar-tenant-por-subdominio db-repo subdomain)]
          (handler (assoc request :tenant tenant))
          {:status 404
           :headers {"Content-Type" "application/json"}
           :body (str "{\"error\": \"Escritório não encontrado no subdomínio '" subdomain "'.\"}")})
        {:status 400
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"Header 'Host' ausente na requisição.\"}"}))))

(defn- extract-token
  "Função auxiliar para extrair o token do header 'Authorization: Bearer <token>'"
  [request]
  (some-> (get-in request [:headers "authorization"])
          (str/split #" ")
          (second)))

(defn wrap-jwt-authentication
  "Middleware que valida o token JWT e injeta o repositório com escopo e a identidade do usuário."
  [handler]
  (fn [request]
    (try
      (if-let [token (extract-token request)]
        (let [claims (jwt/unsign token jwt-secret)
              tenant-id (:tenant-id claims)]
          (if tenant-id
            (let [repo (db/create-repository tenant-id)
                  request' (-> request
                               (assoc :db-repo repo)
                               (assoc :identity claims))]
              (handler request'))
            {:status 401
             :headers {"Content-Type" "application/json"}
             :body "{\"error\": \"Token inválido: tenant-id não encontrado nas claims.\"}"}))
        {:status 401
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"Token de autorização não fornecido no header 'Authorization'.\"}"})
      (catch Exception _
        {:status 401
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"Token inválido ou expirado.\"}"}))))

(defn wrap-public-db-repo
  "Middleware para injetar um repositório PÚBLICO (não isolado) na requisição."
  [handler]
  (fn [request]
    (let [repo (db/create-repository)
          request' (assoc request :db-repo repo)]
      (handler request'))))

(defn wrap-master-role-authorization
  "Middleware que verifica se o usuário autenticado tem a role 'master'."
  [handler]
  (fn [request]
    (let [role (get-in request [:identity :role])]
      (if (= role "master")
        (handler request)
        {:status 403
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"Acesso negado. Requer permissão de administrador.\"}"}))))
