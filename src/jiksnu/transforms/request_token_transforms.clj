(ns jiksnu.transforms.request-token-transforms
  (:require [jiksnu.util :as util])
  )

(defn set-token
  [params]
  (if (:token params)
    params
    (let [token (util/generate-token 16)]
      (assoc params :token token))))

(defn set-token-secret
  [params]
  (if (:token-secret params)
    params
    (let [token (util/generate-token 32)]
      (assoc params :token-secret token))))

(defn set-verifier
  [params]
  (if (:verifier params)
    params
    (let [token (util/generate-token 16)]
      (assoc params :verifier token))))

(defn set-authenticated
  [params]
  ;; TODO: needs to test for key presence, but this is okay
  (if (:authenticated params)
    params
    (assoc params :authenticated false)))

(defn set-used
  [params]
  ;; TODO: needs to test for key presence, but this is okay
  (if (:used params)
    params
    (assoc params :used false)))
