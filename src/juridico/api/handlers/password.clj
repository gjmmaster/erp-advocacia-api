(ns juridico.api.handlers.password
  "Handlers para gerenciamento de senhas (troca de senha temporária, reset, etc.)"
  (:require [buddy.hashers :as hashers]
            [juridico.api.db.protocols :as p]
            [juridico.api.config :as config]
            [buddy.sign.jwt :as jwt]
            [clojure.string :as str]))

;; --- Validação de Senha ---

(defn validate-password-strength
  "Valida a força de uma senha.
   Requisitos:
   - Mínimo 8 caracteres
   - Pelo menos uma letra maiúscula
   - Pelo menos uma letra minúscula
   - Pelo menos um número
   
   Retorna true se senha é válida, false caso contrário."
  [password]
  (and (string? password)
       (>= (count password) 8)
       (re-find #"[A-Z]" password)
       (re-find #"[a-z]" password)
       (re-find #"[0-9]" password)))

(defn get-password-validation-errors
  "Retorna lista de erros de validação da senha.
   Útil para dar feedback detalhado ao usuário."
  [password]
  (let [errors []]
    (cond-> errors
      (< (count password) 8)
      (conj "Senha deve ter no mínimo 8 caracteres")
      
      (not (re-find #"[A-Z]" password))
      (conj "Senha deve conter pelo menos uma letra maiúscula")
      
      (not (re-find #"[a-z]" password))
      (conj "Senha deve conter pelo menos uma letra minúscula")
      
      (not (re-find #"[0-9]" password))
      (conj "Senha deve conter pelo menos um número"))))

;; --- Handler Principal ---

(defn change-password-handler
  "Handler para trocar senha temporária.
   
   Fluxo:
   1. Valida senha atual
   2. Valida força da nova senha
   3. Valida que senhas coincidem
   4. Valida que nova senha é diferente da atual
   5. Atualiza senha no banco
   6. Gera novo JWT sem flag de senha temporária
   7. Retorna novo token
   
   Request deve conter:
   - :jwt-payload (injetado pelo middleware de autenticação)
   - :body-params {:current_password, :new_password, :confirm_password}
   - :db-repo (injetado pelo middleware)
   - :headers (para audit log)"
  [{:keys [db-repo jwt-payload body-params headers] :as request}]
  (let [{:keys [current_password new_password confirm_password]} body-params
        user-id (:user-id jwt-payload)
        tenant-id (:tenant-id jwt-payload)
        email (:email jwt-payload)]
    
    (println "[CHANGE PASSWORD] Iniciando troca de senha")
    (println "[CHANGE PASSWORD] JWT payload:" jwt-payload)
    (println "[CHANGE PASSWORD] user-id:" user-id)
    (println "[CHANGE PASSWORD] email:" email)
    (println "[CHANGE PASSWORD] tenant-id:" tenant-id)
    
    (cond
      ;; Validar que todos os campos foram fornecidos
      (or (str/blank? current_password)
          (str/blank? new_password)
          (str/blank? confirm_password))
      (do
        (println "[CHANGE PASSWORD] Campos obrigatórios faltando")
        {:status 400
         :body {:success false
                :error "Todos os campos são obrigatórios"}})
      
      ;; Buscar usuário no banco
      :else
      (if-let [user (p/encontrar-usuario-por-email db-repo tenant-id email)]
        (cond
          ;; Validar senha atual
          (not (hashers/check current_password (:users/password_hash user)))
          (do
            (println "[CHANGE PASSWORD] Senha atual incorreta")
            {:status 401
             :body {:success false
                    :error "Senha atual incorreta"}})
          
          ;; Validar que senhas coincidem
          (not= new_password confirm_password)
          (do
            (println "[CHANGE PASSWORD] Senhas não coincidem")
            {:status 400
             :body {:success false
                    :error "As senhas não coincidem"}})
          
          ;; Validar que nova senha é diferente da atual
          (hashers/check new_password (:users/password_hash user))
          (do
            (println "[CHANGE PASSWORD] Nova senha igual à atual")
            {:status 400
             :body {:success false
                    :error "Nova senha deve ser diferente da senha atual"}})
          
          ;; Validar força da nova senha
          (not (validate-password-strength new_password))
          (let [errors (get-password-validation-errors new_password)]
            (println "[CHANGE PASSWORD] Senha fraca:" errors)
            {:status 400
             :body {:success false
                    :error "Senha não atende aos requisitos de segurança"
                    :details errors}})
          
          ;; Tudo válido - atualizar senha
          :else
          (try
            (let [;; Fazer hash da nova senha (bcrypt cost 12)
                  new-hash (hashers/derive new_password {:alg :bcrypt+sha512})
                  
                  ;; Atualizar no banco (implementaremos a função no postgres.clj)
                  _ (p/update-user-password! db-repo user-id new-hash false)
                  
                  ;; Gerar novo JWT sem flag de senha temporária
                  new-claims {:user-id user-id
                              :email (:users/email user)
                              :role (:users/role user)
                              :tenant-id tenant-id
                              :temporary-password false
                              :requires-password-change false
                              :exp (-> (java.time.Instant/now)
                                       (.plusSeconds 604800)  ; 7 dias
                                       (.getEpochSecond))}
                  new-token (jwt/sign new-claims config/jwt-secret)]
              
              (println "[CHANGE PASSWORD] Senha alterada com sucesso para user-id:" user-id)
              
              ;; TODO: Enviar email de confirmação (Task 7)
              ;; TODO: Criar audit log entry (Task 14)
              
              {:status 200
               :body {:success true
                      :message "Senha alterada com sucesso"
                      :token new-token}})
            
            (catch Exception e
              (println "[CHANGE PASSWORD ERROR]" (.getMessage e))
              (.printStackTrace e)
              {:status 500
               :body {:success false
                      :error "Erro ao alterar senha. Tente novamente."}})))
        
        ;; Usuário não encontrado (não deveria acontecer se JWT é válido)
        (do
          (println "[CHANGE PASSWORD] Usuário não encontrado")
          {:status 404
           :body {:success false
                  :error "Usuário não encontrado"}})))))
