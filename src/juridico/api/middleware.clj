(ns juridico.api.middleware
  (:require [juridico.api.db.mock :as db.mock]
            [juridico.api.db.protocols :as p]
            [buddy.sign.jwt :as jwt]
            [clojure.string :as str]))

;; --- CONFIGURAÇÃO DE SEGURANÇA ---
;; A chave secreta agora é lida de uma variável de ambiente.
;; Se a variável não existir, usa um valor padrão (APENAS para ambiente de desenvolvimento).
;; Esta é a única fonte da verdade para a chave secreta.
(def jwt-secret (or (System/getenv "JWT_SECRET") "chave-padrao-para-desenvolvimento-segura"))


;; --- NOVOS MIDDLEWARES DE CONTEXTO E AUTENTICAÇÃO ---

(defn wrap-tenant-context
  "Middleware que identifica o tenant a partir do subdomínio no header Host.
   Ele roda antes da lógica de login e injeta os dados do tenant na requisição."
  [handler]
  (fn [{:keys [db-repo headers] :as request}]
    (if-let [host (get headers "host")]
      ;; Lógica para extrair "modelo1" de "modelo1.localhost:3000" ou "modelo1.meuerp.com"
      (let [subdomain (first (str/split host #"\."))]
        (if-let [tenant (p/encontrar-tenant-por-subdominio db-repo subdomain)]
          ;; SUCESSO: Tenant encontrado. Associa o tenant completo à requisição e continua.
          (handler (assoc request :tenant tenant))
          ;; FALHA: Tenant inválido/não encontrado. Interrompe a requisição com um 404.
          {:status 404
           :headers {"Content-Type" "application/json"}
           :body (str "{\"error\": \"Escritório não encontrado no subdomínio '" subdomain "'.\"}")}))
      ;; FALHA: Header Host ausente, o que é inválido para requisições HTTP/1.1.
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body "{\"error\": \"Header 'Host' ausente na requisição.\"}"})))

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
        ;; Valida a assinatura e a expiração do token usando a chave secreta.
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
