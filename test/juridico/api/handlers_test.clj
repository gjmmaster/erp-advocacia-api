(ns juridico.api.handlers-test
  (:require [clojure.test :refer :all]
            [juridico.api.handlers :as h]
            [juridico.api.mock-db :as mock-db]
            [juridico.api.test-utils :as tu]
            [buddy.hashers :as hashers]
            [clojure.spec.alpha :as s]
            [juridico.api.services.email :as email-service]))

(deftest login-handler-test
  (let [tenant {:tenants/id 1, :tenants/subdomain "acme"}
        user {:users/id 10,
              :users/tenant_id 1,
              :users/email "test@acme.com",
              :users/password_hash (hashers/encrypt "secret"),
              :users/role "master"}
        db-repo (mock-db/->MockDb (atom {:tenants [tenant] :users [user]}))]

    (testing "Login com sucesso"
      (let [request {:db-repo db-repo
                     :tenant tenant
                     :body-params {:email "test@acme.com" :password "secret"}}
            response (h/login-handler request)]
        (is (= 200 (:status response)))
        (is (contains? (:body response) :token))
        (is (= "Usuário test@acme.com autenticado com sucesso." (:message (:body response))))))

    (testing "Login com senha incorreta"
      (let [request {:db-repo db-repo
                     :tenant tenant
                     :body-params {:email "test@acme.com" :password "wrong"}}
            response (h/login-handler request)]
        (is (= 401 (:status response)))
        (is (= {:error "Credenciais inválidas."} (:body response)))))

    (testing "Login com email incorreto"
      (let [request {:db-repo db-repo
                     :tenant tenant
                     :body-params {:email "wrong@acme.com" :password "secret"}}
            response (h/login-handler request)]
        (is (= 401 (:status response)))
        (is (= {:error "Credenciais inválidas."} (:body response)))))

    (testing "Login com payload inválido"
      (let [request {:db-repo db-repo
                     :tenant tenant
                     :body-params {:email "test@acme.com"}}
            response (h/login-handler request)]
        (is (= 400 (:status response)))
        (is (= "Dados de login inválidos." (:error (:body response))))))))

(deftest processos-handlers-test
  (let [processo1 {:id 1, :numero "001", :descricao "Processo 1"}
        processo2 {:id 2, :numero "002", :descricao "Processo 2"}
        db-repo (mock-db/->MockDb (atom [processo1 processo2]))
        identity {:user-id 10 :tenant-id 1 :role "master"}]

    (testing "Listar processos"
      (let [request {:db-repo db-repo}
            response (h/listar-processos-handler request)]
        (is (= 200 (:status response)))
        (is (= [processo1 processo2] (:body response)))))

    (testing "Obter processo por ID"
      (let [request {:db-repo db-repo :path-params {:id "1"}}
            response (h/obter-processo-handler request)]
        (is (= 200 (:status response)))
        (is (= processo1 (:body response)))))

    (testing "Obter processo por ID inexistente"
      (let [request {:db-repo db-repo :path-params {:id "999"}}
            response (h/obter-processo-handler request)]
        (is (= 404 (:status response)))))

    (testing "Criar processo com sucesso"
      (with-redefs [s/valid? (constantly true)]
        (let [novo-processo {:numero "003" :descricao "Novo Processo"}
              request {:db-repo db-repo :body-params novo-processo}
              response (h/criar-processo-handler request)]
          (is (= 201 (:status response)))
          (is (= "003" (:numero (:body response)))))))

    (testing "Criar processo com dados inválidos"
      (with-redefs [s/valid? (constantly false)
                    s/explain-data (constantly "Explicação do erro")]
        (let [novo-processo {:numero "003"}
              request {:db-repo db-repo :body-params novo-processo}
              response (h/criar-processo-handler request)]
          (is (= 400 (:status response)))
          (is (contains? (:body response) :error)))))

    (testing "Atualizar processo com sucesso"
      (with-redefs [s/valid? (constantly true)]
        (let [dados-atualizados {:descricao "Processo 1 Atualizado"}
              request {:db-repo db-repo :path-params {:id "1"} :body-params dados-atualizados}
              response (h/atualizar-processo-handler request)]
          (is (= 200 (:status response)))
          (is (= {:message "Processo atualizado com sucesso."} (:body response)))
          (let [processo-atualizado (:body (h/obter-processo-handler {:db-repo db-repo :path-params {:id "1"}}))]
            (is (= "Processo 1 Atualizado" (:descricao processo-atualizado)))))))

    (testing "Deletar processo com sucesso"
      (let [request {:db-repo db-repo :path-params {:id "2"}}
            response (h/deletar-processo-handler request)]
        (is (= 204 (:status response)))
        (let [processo-deletado (h/obter-processo-handler {:db-repo db-repo :path-params {:id "2"}})]
          (is (= 404 (:status processo-deletado)))))))))

(deftest operadores-handlers-test
  (let [operador1 {:users/id 20, :users/tenant_id 1, :users/email "op1@acme.com"}
        operador2 {:users/id 21, :users/tenant_id 1, :users/email "op2@acme.com"}
        db-repo (mock-db/->MockDb (atom {:users [operador1 operador2]}))
        identity {:user-id 10 :tenant-id 1 :role "master"}]

    (testing "Listar operadores"
      (let [request {:db-repo db-repo :identity identity}
            response (h/listar-operadores-handler request)]
        (is (= 200 (:status response)))
        (is (= 2 (count (:body response))))))

    (testing "Criar operador"
      (with-redefs [s/valid? (constantly true)]
        (let [novo-operador {:email "op3@acme.com" :password "pass"}
              request {:db-repo db-repo :identity identity :body-params novo-operador}
              response (h/criar-operador-handler request)]
          (is (= 201 (:status response)))
          (is (= "op3@acme.com" (:email (:body response)))))))))

(deftest tenants-handlers-test
  (testing "Listar tenants"
    (let [tenant1 {:tenants/id 1, :tenants/subdomain "acme"}
          tenant2 {:tenants/id 2, :tenants/subdomain "globex"}
          db-repo (mock-db/->MockDb (atom {:tenants [tenant1 tenant2] :users []}))
          identity {:user-id 1 :role "super-admin"}
          request {:db-repo db-repo :identity identity}
          response (h/listar-tenants-handler request)]
      (is (= 200 (:status response)))
      (is (= 2 (count (:body response))))))

  (testing "Provisionar tenant"
    (let [tenant1 {:tenants/id 1, :tenants/subdomain "acme"}
          tenant2 {:tenants/id 2, :tenants/subdomain "globex"}
          db-repo (mock-db/->MockDb (atom {:tenants [tenant1 tenant2] :users []}))
          identity {:user-id 1 :role "super-admin"}]
      (with-redefs [s/valid? (constantly true)
                    juridico.api.services.email/send-welcome-email (constantly nil)]
        (let [dados-provisionamento {:subdomain "newcorp" :plan "basic" :user {:email "admin@newcorp.com" :name "Admin" :password "pass"}}
              request {:db-repo db-repo :identity identity :body-params dados-provisionamento}
              response (h/provision-tenant-handler request)]
          (is (= 201 (:status response)))
          (let [tenants-after @(:state db-repo)
                new-tenant-exists? (some #(= "newcorp" (:tenants/subdomain %)) (:tenants tenants-after))]
            (is (true? new-tenant-exists?))))))))
