(ns juridico.api.rate-limit
  (:require [throttler.core :as throttler]
            [clojure.tools.logging :as log]))

;; Configuração do rate limiter
;; Permite 5 tentativas de login por IP a cada 15 minutos
(def login-limiter
  (throttler/make-throttler :login 5 (* 15 60 1000)))

(defn get-client-ip
  "Extrai o IP do cliente da requisição, considerando proxies."
  [request]
  (or (get-in request [:headers "x-forwarded-for"])
      (get-in request [:headers "x-real-ip"])
      (:remote-addr request)
      "unknown"))

(defn wrap-rate-limit-login
  "Middleware que aplica rate limiting em endpoints de login.
   Bloqueia IPs que excedem 5 tentativas em 15 minutos."
  [handler]
  (fn [request]
    (let [uri (:uri request)
          method (:request-method request)
          is-login? (and (= method :post)
                        (or (= uri "/login")
                            (= uri "/admin/login")))]
      (if is-login?
        (let [client-ip (get-client-ip request)
              throttle-key (str "login:" client-ip)]
          (if (throttler/allow? login-limiter throttle-key)
            (do
              (log/info "Login attempt from IP:" client-ip)
              (handler request))
            (do
              (log/warn "Rate limit exceeded for IP:" client-ip)
              {:status 429
               :headers {"Content-Type" "application/json"
                        "Retry-After" "900"}
               :body {:error "Muitas tentativas de login. Tente novamente em 15 minutos."}})))
        (handler request)))))

(defn wrap-global-error-handler
  "Middleware global que captura todas as exceções não tratadas.
   Registra o erro detalhado e retorna resposta genérica ao cliente."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error e "Erro não tratado na requisição:"
                   {:method (:request-method request)
                    :uri (:uri request)
                    :params (:params request)})
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body {:error "Erro interno do servidor. Por favor, tente novamente mais tarde."}}))))
