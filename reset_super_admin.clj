(ns reset-super-admin
  (:require [buddy.hashers :as hashers]))

;; Gera hash bcrypt para a senha "Admin@123"
(defn -main []
  (let [password "Admin@123"
        hash (hashers/encrypt password)]
    (println "=== RESET SENHA SUPER ADMIN ===")
    (println "Senha em texto claro:" password)
    (println "Hash bcrypt gerado:" hash)
    (println "")
    (println "Execute este SQL no CockroachDB:")
    (println "")
    (println "UPDATE users")
    (println "SET password_hash = '" hash "'")
    (println "WHERE role = 'super-admin';")
    (println "")
    (println "Depois tente fazer login com:")
    (println "Email: super@admin.com")
    (println "Senha: Admin@123")))

(-main)
