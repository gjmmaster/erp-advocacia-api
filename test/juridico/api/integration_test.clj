(ns juridico.api.integration-test
  (:require [clojure.test :refer :all]
            [juridico.api.core :as core]
            [juridico.api.mock-db :as mock-db]
            [juridico.api.test-utils :as tu]
            [juridico.api.config :as config]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [buddy.hashers :as hashers]))

(def test-tenant {:tenants/id 1, :tenants/subdomain "acme"})
(def test-user {:users/id 10,
                :users/tenant_id 1,
                :users/email "test@acme.com",
                :users/password_hash (hashers/encrypt "secret"),
                :users/role "master"})

(defn test-db-fixture [f]
  (let [mock-db-instance (mock-db/->MockDb (atom {:tenants [test-tenant] :users [test-user]}))]
    (with-redefs [juridico.api.db.postgres/create-repository (constantly mock-db-instance)
                  config/jwt-secret tu/test-jwt-secret]
      (f))))

(use-fixtures :each test-db-fixture)

(deftest auth-routes-integration-test
  (testing "Login com sucesso"
    (let [request (-> (mock/request :post "/auth/login")
                      (mock/header "X-Tenant-Subdomain" "acme")
                      (mock/json-body {:email "test@acme.com" :password "secret"}))
          response (core/app request)]
      (is (= 200 (:status response)))
      (let [body (json/parse-string (slurp (:body response)) true)]
        (is (contains? body :token))))))

(deftest protected-routes-integration-test
  (let [master-token (tu/generate-token 10 1 "master")
        operador-token (tu/generate-token 11 1 "operador")]

    (testing "Acesso a rota de processos com token válido"
      (let [request (-> (mock/request :get "/api/processos")
                        (mock/header "Authorization" (str "Bearer " master-token)))
            response (core/app request)]
        (is (= 200 (:status response)))))

    (testing "Acesso a rota de processos sem token"
      (let [request (mock/request :get "/api/processos")
            response (core/app request)]
        (is (= 401 (:status response)))))

    (testing "Acesso a rota de operadores com role 'master'"
      (let [request (-> (mock/request :get "/api/operadores")
                        (mock/header "Authorization" (str "Bearer " master-token)))
            response (core/app request)]
        (is (= 200 (:status response)))))

    (testing "Acesso a rota de operadores com role 'operador'"
      (let [request (-> (mock/request :get "/api/operadores")
                        (mock/header "Authorization" (str "Bearer " operador-token)))
            response (core/app request)]
        (is (= 403 (:status response)))))))
