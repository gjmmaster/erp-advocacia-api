(ns juridico.api.config
  (:require [clojure.tools.logging :as log]))

(def jwt-secret
  (let [env-secret (System/getenv "JWT_SECRET")
        app-env (System/getenv "APP_ENV")
        is-production? (= "production" app-env)]
    (cond
      ;; Produção: JWT_SECRET é obrigatório
      (and is-production? (nil? env-secret))
      (throw (Exception. "ERRO CRÍTICO: A variável de ambiente JWT_SECRET não foi definida."))
      
      ;; Desenvolvimento: permite chave padrão com aviso
      (nil? env-secret)
      (do
        (log/warn "⚠️  Usando chave JWT padrão. NÃO USE EM PRODUÇÃO!")
        "chave-padrao-para-desenvolvimento-segura")
      
      ;; JWT_SECRET definido
      :else
      (do
        (log/info "✓ JWT_SECRET carregado com sucesso")
        env-secret))))
