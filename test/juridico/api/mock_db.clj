(ns juridico.api.mock-db
  (:require [juridico.api.db.protocols :as p]
            [buddy.hashers :as hashers]))

(defn- find-first [pred coll]
  (first (filter pred coll)))

(deftype MockDb [state]
  p/ProcessosRepository
  (listar-processos [_] @state)
  (obter-processo-por-id [_ id] (find-first #(= (:id %) id) @state))
  (criar-processo [_ processo]
    (let [new-p (assoc processo :id (rand-int 1000))]
      (swap! state conj new-p)
      new-p))
  (atualizar-processo [this id dados-processo]
    (let [processo-existente (p/obter-processo-por-id this id)]
      (when processo-existente
        (swap! state (fn [current-state]
                       (map (fn [p]
                              (if (= (:id p) id)
                                (merge p dados-processo)
                                p))
                            current-state)))
        {:next.jdbc/update-count 1})))
  (deletar-processo [this id]
    (let [processo-existente (p/obter-processo-por-id this id)]
      (when processo-existente
        (swap! state (fn [current-state]
                       (filterv #(not= (:id %) id) current-state)))
        {:next.jdbc/update-count 1})))

  p/AuthRepository
  (encontrar-tenant-por-subdominio [_ subdominio] (find-first #(= (:tenants/subdomain %) subdominio) (:tenants @state)))
  (encontrar-usuario-por-email [_ tenant-id email] (find-first #(and (= (:users/tenant_id %) tenant-id) (= (:users/email %) email)) (:users @state)))
  (criar-tenant-e-usuario-master [_ {:keys [subdomain plan user]}]
    (let [new-tenant {:tenants/id (rand-int 1000)
                      :tenants/subdomain subdomain
                      :tenants/plan plan}
          new-user {:users/id (rand-int 1000)
                    :users/tenant_id (:tenants/id new-tenant)
                    :users/email (:email user)
                    :users/name (:name user)
                    :users/password_hash (hashers/encrypt (:password user))
                    :users/role "master"}]
      (swap! state (fn [current-state]
                     (-> current-state
                         (update :tenants conj new-tenant)
                         (update :users conj new-user))))
      {:tenant new-tenant :user new-user}))

  (listar-usuarios-do-tenant [_ tenant-id] (filter #(= (:users/tenant_id %) tenant-id) (:users @state)))
  (criar-usuario-operador [_ tenant-id dados-usuario]
    (let [new-user (assoc dados-usuario
                          :users/id (rand-int 1000)
                          :users/tenant_id tenant-id
                          :users/role "operador"
                          :users/password_hash (hashers/encrypt (:password dados-usuario)))]
      (swap! state update :users conj new-user)
      new-user))
  (atualizar-operador [_ tenant-id user-id dados-usuario]
    (swap! state update :users
           (fn [users]
             (map (fn [u]
                    (if (and (= (:users/tenant_id u) tenant-id) (= (:users/id u) user-id))
                      (merge u dados-usuario)
                      u))
                  users)))
    {:next.jdbc/update-count 1})
  (deletar-operador [_ tenant-id user-id]
    (swap! state update :users
           (fn [users]
             (filterv #(not (and (= (:users/tenant_id %) tenant-id) (= (:users/id %) user-id))) users)))
    {:next.jdbc/update-count 1})
  (obter-operador-por-id [_ tenant-id user-id] (find-first #(and (= (:users/tenant_id %) tenant-id) (= (:users/id %) user-id)) (:users @state)))

  (listar-tenants [_] (:tenants @state))
  (obter-tenant-por-id [_ tenant-id] (find-first #(= (:tenants/id %) tenant-id) (:tenants @state)))
  (atualizar-tenant [_ tenant-id dados-tenant]
    (swap! state update :tenants
           (fn [tenants]
             (map (fn [t]
                    (if (= (:tenants/id t) tenant-id)
                      (merge t dados-tenant)
                      t))
                  tenants)))
    {:next.jdbc/update-count 1})
  (criar-tenant [_ dados-tenant]
    (let [new-tenant (assoc dados-tenant :tenants/id (rand-int 1000))]
      (swap! state update :tenants conj new-tenant)
      new-tenant))
  (deletar-tenant [_ tenant-id]
    (swap! state update :tenants
           (fn [tenants]
             (filterv #(not= (:tenants/id %) tenant-id) tenants)))
    {:next.jdbc/update-count 1}))

(defn- -create-mock-db
  "Creates a new mock database with an initial state."
  ([] (-create-mock-db {}))
  ([initial-state] (->MockDb (atom initial-state))))
