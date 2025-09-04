(ns juridico.api.specs
  (:require [clojure.spec.alpha :as s]))

;; Spec para garantir que o número do processo é uma string não vazia
(s/def ::case_number (s/and string? not-empty?))

;; Spec para garantir que a jurisdição é uma string não vazia
(s/def ::jurisdiction (s/and string? not-empty?))

;; Spec que define a estrutura do corpo da requisição para criar um processo
(s/def ::create-process-payload (s/keys :req-un [::case_number ::jurisdiction]))
