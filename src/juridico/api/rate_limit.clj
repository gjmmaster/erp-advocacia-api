(ns juridico.api.rate-limit
  (:require [clojure.tools.logging :as log]))

;; Armazena tentativas de login por IP
;; Estrutura: {ip {:attempts [timestamp1 timestamp2 ...] :blocked-until timestamp}}
(def login-attempts (atom {}))

;; Configuração
(def max-attempts 100) ; Aumentado para 100 para facilitar testes
(def window-ms (* 5 60 1000)) ; 5 minutos
(def block-duration-ms (* 2 60 1000)) ; Reduzido para 2 minutos de bloqueio

(defn get-client-ip
  "Extrai o IP do cliente da requisição, considerando proxies."
  [request]
  (or (get-in request [:headers "x-forwarded-for"])
      (get-in request [:headers "x-real-ip"])
      (:remote-addr request)
      "unknown"))

(defn clean-old-attempts
  "Remove tentativas antigas fora da janela de tempo."
  [attempts now]
  (filter #(> % (- now window-ms)) attempts))

(defn is-blocked?
  "Verifica se o IP está bloqueado."
  [ip-data now]
  (when-let [blocked-until (:blocked-until ip-data)]
    (> blocked-until now)))

(defn should-block?
  "Verifica se deve bloquear o IP baseado no número de tentativas."
  [attempts]
  (>= (count attempts) max-attempts))

(defn record-attempt
  "Registra uma tentativa de login para o IP."
  [ip]
  (let [now (System/currentTimeMillis)]
    (swap! login-attempts
           (fn [state]
             (let [ip-data (get state ip {:attempts []})
                   cleaned-attempts (clean-old-attempts (:attempts ip-data) now)
                   new-attempts (conj cleaned-attempts now)]
               (if (should-block? new-attempts)
                 (assoc state ip {:attempts new-attempts
                                 :blocked-until (+ now block-duration-ms)})
                 (assoc state ip {:attempts new-attempts})))))))

(defn check-rate-limit
  "Verifica se o IP pode fazer login. Retorna nil se permitido, ou mensagem de erro se bloqueado."
  [ip]
  (let [now (System/currentTimeMillis)
        ip-data (get @login-attempts ip)]
    (when (and ip-data (is-blocked? ip-data now))
      (let [remaining-seconds (quot (- (:blocked-until ip-data) now) 1000)]
        {:blocked true
         :retry-after remaining-seconds
         :message (str "Muitas tentativas de login. Tente novamente em " 
                      (quot remaining-seconds 60) " minutos.")}))))

(defn wrap-rate-limit-login
  "Middleware que aplica rate limiting em endpoints de login.
   TEMPORARIAMENTE DESABILITADO para testes."
  [handler]
  (fn [request]
    ;; TEMPORARIAMENTE DESABILITADO - apenas loga mas não bloqueia
    (let [uri (:uri request)
          method (:request-method request)
          is-login? (and (= method :post)
                        (or (= uri "/login")
                            (= uri "/admin/login")))]
      (when is-login?
        (let [client-ip (get-client-ip request)]
          (log/info "Login attempt from IP:" client-ip "(rate limiting disabled)")))
      (handler request))))

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
