(ns juridico.api.config)

(def jwt-secret
  (or (System/getenv "JWT_SECRET")
      "chave-padrao-para-desenvolvimento-segura"))
