(ns juridico.api.test-utils
  (:require [buddy.sign.jwt :as jwt]))

(def test-jwt-secret "test-secret-key-that-is-long-enough-to-be-valid")

(defn generate-token
  "Generates a JWT token for testing purposes."
  [user-id tenant-id role]
  (let [claims {:user-id user-id
                :tenant-id tenant-id
                :role role
                :exp (-> (java.time.Instant/now)
                         (.plusSeconds 3600)
                         (.getEpochSecond))}]
    (jwt/sign claims test-jwt-secret)))
