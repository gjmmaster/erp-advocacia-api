(ns juridico.api.specs
  (:require [clojure.spec.alpha :as s]))

;; --- Specs para Processos (Já existentes) ---
(s/def ::case_number (s/and string? not-empty))
(s/def ::jurisdiction (s/and string? not-empty))
(s/def ::create-process-payload (s/keys :req-un [::case_number ::jurisdiction]))

;; --- ADICIONAR ESTA SPEC ---
(s/def ::update-process-payload (s/keys :opt-un [::case_number ::jurisdiction]))

;; --- Specs para Provisionamento e Login ---

;; Spec para validar um e-mail. Usamos uma expressão regular simples.
(s/def ::email (s/and string? #(re-matches #".+@.+\..+" %)))

;; Spec para o nome do escritório (tenant)
(s/def ::company_name (s/and string? not-empty))

;; Spec para a senha (mínimo de 4 caracteres para a PoC)
(s/def ::password (s/and string? #(< 3 (count %))))

;; Spec para o subdomínio (ainda usada no retorno do provisionamento, etc.)
(s/def ::subdomain (s/and string? not-empty))

;; Define a estrutura do payload que o Super Admin envia para criar um novo escritório.
;; Espera o nome da empresa e o e-mail do futuro Admin do escritório.
(s/def ::provision-payload (s/keys :req-un [::company_name ::email]))

;; Define a estrutura do payload para a tela de login.
;; AGORA SÓ ESPERA e-mail e senha. O subdomínio é identificado pelo Host da requisição.
(s/def ::login-payload (s/keys :req-un [::email ::password]))

;; --- Specs para Gestão de Usuários ---
(s/def ::full_name (s/and string? not-empty))

(s/def ::create-operator-payload (s/keys :req-un [::email ::password ::full_name]))

(s/def ::update-operador-payload (s/keys :opt-un [::full_name]))

;; --- Specs para Gestão de Tenants (Super Admin) ---
(s/def ::operator_limit (s/and int? #(> % 0)))
(s/def ::update-tenant-payload (s/keys :opt-un [::company_name ::operator_limit]))
(s/def ::create-tenant-payload (s/keys :req-un [::company_name] :opt-un [::subdomain ::operator_limit]))
