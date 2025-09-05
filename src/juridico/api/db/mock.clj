(ns juridico.api.db.mock
  (:require [juridico.api.db.protocols :refer [ProcessosRepository AuthRepository]] ; <-- Corrigido aqui
            [clojure.string :as str]))

;; --- Banco de Dados Mock ---
(def db-atom
  (atom
   {"tenant-1"
    {:dados {:id "tenant-1"
             :company_name "Escritório Modelo 1"
             :subdomain "modelo1"}}
     :users
     {"user-11" {:id "user-11"
                 :email "admin@modelo1.com"
                 :password_hash "$2a$12$..."
                 :role "admin"}}
     :processos
     {"proc-111" {:id "proc-111"
                  :case_number "0001-2023"
                  :jurisdiction "Tribunal de Justiça de São Paulo"}
      "proc-222" {:id "proc-222"
                  :case_number "0002-2023"
                  :jurisdiction "Tribunal Regional Federal"}}}
    "tenant-2"
    {:dados {:id "tenant-2"
             :company_name "Advocacia Teste 2"
             :subdomain "teste2"}}
     :users
     {"user-21" {:id "user-21"
                 :email "admin@teste2.com"
                 :password_hash "$2a$12$..."
                 :role "admin"}}
     :processos
     {"proc-333" {:id "proc-333"
                  :case_number "ABC-2024"
                  :jurisdiction "Superior Tribunal de Justiça"}}}}))

;; --- Implementação dos Repositórios Mock ---
(defrecord MockRepository [db tenant-id]

  ;; --- Implementação do Protocolo de Processos ---
  ProcessosRepository ; <-- E corrigido aqui

  (listar-processos [this]
    (-> @(:db this) (get (:tenant-id this)) :processos vals (or [])))

  (obter-processo-por-id [this id]
    (-> @(:db this) (get-in [(:tenant-id this) :processos id])))

  (criar-processo [this processo]
    (let [new-id (str "proc-" (rand-int 10000))
          novo-processo-com-id (assoc processo :id new-id)]
      (swap! (:db this) assoc-in [(:tenant-id this) :processos new-id] novo-processo-com-id)
      novo-processo-com-id))

  ;; --- Implementação do Protocolo de Autenticação ---
  AuthRepository
  (encontrar-tenant-por-subdominio [this subdominio]
    (->> (vals @(:db this))
         (filter #(= subdominio (get-in % [:dados :subdomain])))
         first))

  (encontrar-usuario-por-email [this tenant-id email]
    (let [usuarios-do-tenant (get-in @(:db this) [tenant-id :users])]
      (->> (vals usuarios-do-tenant)
           (filter #(= email (:email %)))
           first)))

  (criar-tenant-e-usuario-master [this {:keys [company_name email]}]
    (let [new-tenant-id (str "tenant-" (rand-int 10000))
          subdomain (-> company_name str/lower-case (str/replace #" " "-"))
          new-user-id (str "user-" (rand-int 10000))
          temp-password (str "pass" (rand-int 1000))]

      (swap! (:db this) assoc new-tenant-id
             {:dados {:id new-tenant-id
                      :company_name company_name
                      :subdomain subdomain}
              :users {new-user-id {:id new-user-id
                                   :email email
                                   :password_hash temp-password
                                   :role "admin"}}
              :processos {}})

      {:tenant {:id new-tenant-id :subdomain subdomain}
       :user {:email email :temp_password temp-password}})))

;; Função "construtora" para facilitar a criação de instâncias do nosso repositório.
(defn create-repository
  ([] (->MockRepository db-atom nil))
  ([tenant-id] (->MockRepository db-atom tenant-id)))
