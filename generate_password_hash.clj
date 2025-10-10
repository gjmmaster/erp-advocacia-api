(ns generate-password-hash
  (:require [buddy.hashers :as hashers]))

;; Script para gerar o hash da senha do super admin
;; Execute com: lein run -m generate-password-hash

(defn -main []
  (let [password "DEFINA_SUA_SENHA_AQUI" ; ← MUDE ESTA SENHA!
        hash (hashers/encrypt password)]
    (println "Senha:" password)
    (println "Hash:" hash)
    (println "\nUse este SQL para criar o super admin:")
    (println "INSERT INTO users (tenant_id, email, password_hash, role, full_name, created_at)")
    (println "VALUES (NULL, 'super@admin.com', '" hash "', 'super-admin', 'Super Administrator', NOW())")
    (println "ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;")))
