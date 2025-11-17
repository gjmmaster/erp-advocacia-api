(ns juridico.api.handlers.users
  (:require [ring.util.response :as response]
            [juridico.api.db.core :as db]
            [buddy.hashers :as hashers]
            [clojure.string :as str]))

(defn list-users-handler
  "Lista todos os usuários do tenant"
  [{:keys [db-repo tenant-id]}]
  (try
    (let [users (.list-users-by-tenant db-repo tenant-id)]
      (response/response
        {:users (map #(dissoc % :password_hash) users)}))
    (catch Exception e
      (println "[ERROR] Erro ao listar usuários:" (.getMessage e))
      (-> (response/response {:error "Erro ao listar usuários"})
          (response/status 500)))))

(defn get-user-handler
  "Obtém detalhes de um usuário específico"
  [{:keys [db-repo tenant-id path-params]}]
  (try
    (let [user-id (parse-long (:id path-params))
          user (.get-user-by-id db-repo user-id)]
      (if (and user (= (:tenant_id user) tenant-id))
        (response/response
          {:user (dissoc user :password_hash)})
        (-> (response/response {:error "Usuário não encontrado"})
            (response/status 404))))
    (catch Exception e
      (println "[ERROR] Erro ao obter usuário:" (.getMessage e))
      (-> (response/response {:error "Erro ao obter usuário"})
          (response/status 500)))))

(defn create-user-handler
  "Cria um novo usuário no tenant"
  [{:keys [db-repo tenant-id body-params]}]
  (try
    (let [{:keys [email full_name role]} body-params
          temp-password (.generate-temp-password db-repo)
          password-hash (hashers/derive temp-password)
          new-user (.create-user db-repo {
                                            :tenant_id tenant-id
                                            :email (str/lower-case (str/trim email))
                                            :full_name (str/trim full_name)
                                            :role role
                                            :password_hash password-hash
                                            :temporary_password true
                                            :requires_password_change true
                                            :active true})]
      (response/response
        {:user (dissoc new-user :password_hash)
         :temporary_password temp-password
         :message "Usuário criado com sucesso. Senha temporária gerada."}))
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)]
        (cond
          (= (:type data) :duplicate-email)
          (-> (response/response {:error "Email já cadastrado"})
              (response/status 409))
          :else
          (-> (response/response {:error (.getMessage e)})
              (response/status 400)))))
    (catch Exception e
      (println "[ERROR] Erro ao criar usuário:" (.getMessage e))
      (-> (response/response {:error "Erro ao criar usuário"})
          (response/status 500)))))

(defn update-user-handler
  "Atualiza um usuário existente"
  [{:keys [db-repo tenant-id path-params body-params]}]
  (try
    (let [user-id (parse-long (:id path-params))
          existing-user (.get-user-by-id db-repo user-id)]
      (if (and existing-user (= (:tenant_id existing-user) tenant-id))
        (let [updated-user (.update-user db-repo user-id
                                          (select-keys body-params [:full_name :role :active]))]
          (response/response
            {:user (dissoc updated-user :password_hash)
             :message "Usuário atualizado com sucesso"}))
        (-> (response/response {:error "Usuário não encontrado"})
            (response/status 404))))
    (catch Exception e
      (println "[ERROR] Erro ao atualizar usuário:" (.getMessage e))
      (-> (response/response {:error "Erro ao atualizar usuário"})
          (response/status 500)))))

(defn delete-user-handler
  "Desativa um usuário (soft delete)"
  [{:keys [db-repo tenant-id path-params]}]
  (try
    (let [user-id (parse-long (:id path-params))
          existing-user (.get-user-by-id db-repo user-id)]
      (if (and existing-user (= (:tenant_id existing-user) tenant-id))
        (do
          (.soft-delete-user db-repo user-id)
          (response/response
            {:message "Usuário desativado com sucesso"}))
        (-> (response/response {:error "Usuário não encontrado"})
            (response/status 404))))
    (catch Exception e
      (println "[ERROR] Erro ao deletar usuário:" (.getMessage e))
      (-> (response/response {:error "Erro ao deletar usuário"})
          (response/status 500)))))

(defn reset-user-password-handler
  "Reseta a senha de um usuário para uma senha temporária"
  [{:keys [db-repo tenant-id path-params]}]
  (try
    (let [user-id (parse-long (:id path-params))
          existing-user (.get-user-by-id db-repo user-id)]
      (if (and existing-user (= (:tenant_id existing-user) tenant-id))
        (let [temp-password (.generate-temp-password db-repo)
              password-hash (hashers/derive temp-password)]
          (.update-user db-repo user-id {
                                          :password_hash password-hash
                                          :temporary_password true
                                          :requires_password_change true})
          (response/response
            {:temporary_password temp-password
             :message "Senha resetada com sucesso"}))
        (-> (response/response {:error "Usuário não encontrado"})
            (response/status 404))))
    (catch Exception e
      (println "[ERROR] Erro ao resetar senha:" (.getMessage e))
      (-> (response/response {:error "Erro ao resetar senha"})
          (response/status 500)))))
