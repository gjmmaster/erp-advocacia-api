(ns juridico.api.services.email
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [environ.core :refer [env]]))

(defn- build-welcome-subject [company-name]
  (str "Bem-vindo ao ERP Jurídico! Seu acesso para " company-name " foi criado."))

(defn- build-welcome-body [user-email subdomain temp-password]
  (str "<h1>Seu acesso foi criado com sucesso!</h1>"
       "<p>Olá,</p>"
       "<p>Sua conta para acessar nosso sistema foi provisionada.</p>"
       "<ul>"
       "<li><b>Link de Acesso:</b> <a href=\"https://erp-advocacia-ui.onrender.com\">https://" subdomain ".erp-advocacia-ui.onrender.com</a></li>"
       "<li><b>Usuário:</b> " user-email "</li>"
       "<li><b>Sua Senha Temporária:</b> " temp-password "</li>"
       "</ul>"
       "<p>Recomendamos que você altere sua senha no primeiro acesso.</p>"
       "<p>Atenciosamente,<br>Equipe ERP Jurídico.</p>"))

(defn- send-email-via-api [to-email subject body]
  (if-let [email-api-url (env :email-api-url)]
    (if-let [email-api-token (env :email-api-token)]
      (let [payload {:user       (env :email-api-user)
                     :from_email "noreplay@jmmaster.com"
                     :from_name  "ERP Jurídico"
                     :contact    [{:to_email to-email
                                   :to_name  "Novo Usuário"
                                   :subject  subject}]
                     :body       body}
            response (try
                       (client/post (str email-api-url "?token=" email-api-token)
                                    {:body             (json/generate-string payload)
                                     :content-type     :json
                                     :throw-exceptions false})
                       (catch Exception e
                         {:status 500 :body (str "Erro de conexão com API de Email: " (.getMessage e))}))]
        response)
      (do
        (println "ERRO: Variável de ambiente EMAIL_API_TOKEN não configurada.")
        {:status 500 :body "EMAIL_API_TOKEN não configurada."}))
    (do
      (println "ERRO: Variável de ambiente EMAIL_API_URL não configurada.")
      {:status 500 :body "EMAIL_API_URL não configurada."})))

(defn send-welcome-email
  "Função principal para orquestrar o envio do e-mail de boas-vindas."
  [{:keys [email temp_password]} {:keys [company_name subdomain]}]
  (println (str "[Serviço de Email] Disparando e-mail de boas-vindas para: " email))
  (let [subject (build-welcome-subject company_name)
        body    (build-welcome-body email subdomain temp_password)]
    (send-email-via-api email subject body)))
