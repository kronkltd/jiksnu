(ns jiksnu.actions.access-token-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.request-token-transforms :as transforms.request-token]
            [slingshot.slingshot :refer [throw+ try+]]))

(def model-sym 'jiksnu.model.request-token)

(def create-fn (ns-resolve (the-ns model-sym) 'create))
(def delete-fn (ns-resolve (the-ns model-sym) 'delete))
(def fetch-fn  (ns-resolve (the-ns model-sym) 'fetch-by-id))

(defonce delete-hooks (ref []))

(defn prepare-create
  [params]
  (-> params
      ;; transforms.request-token/set-_id
      ;; transforms.request-token/set-secret
      ;; transforms.request-token/set-verifier
      ;; transforms.request-token/set-used
      ;; transforms.request-token/set-authenticated
      transforms/set-created-time
      transforms/set-updated-time
      ))

(defn prepare-delete
  ([item]
     (prepare-delete item @delete-hooks))
  ([item hooks]
     (if (seq hooks)
       (recur ((first hooks) item) (rest hooks))
       item)))

(defaction delete
  [item]
  (let [item (prepare-delete item)]
    (delete-fn item)))

(defaction show
  [item]
  item)

(def index*
  (templates.actions/make-indexer model-sym :sort-clause {:created 1}))

(defaction index
  [& options]
  (apply index* options))

(defaction create
  [params]
  (let [params (prepare-create params)]
    (create-fn params)))

(defn find-or-create
  [params]
  (or (fetch-fn (:_id params)) (create params)))

(defn get-access-token
  [params]
  (let [version (:oauth_version params)]
    (if (= version "1.0")
      (let [signature-method (:oauth_signature_method params)]
        (if (= signature-method "HMAC-SHA1")
          (let [nonce (:oauth_nonce params)
                consumer-key (:oauth_consumer_key params)
                timestamp (:oauth_timestamp params)
                signature (:oauth_signature params)
                verifier (:oauth_verifier params)
                token (:oauth_token params)]
            (if-let [client (model.client/fetch-by-id consumer-key)]
              (if-let [request-token (model.request-token/fetch-by-id token)]
                (if (:access-token request-token)
                  (throw+ "Request Token has already been consumed")
                  (if (= consumer-key (:client request-token))
                    (create params)
                    (throw+ "Consumer Key does not match request token's consumer.")))
                (throw+ "Request token not found"))
              (throw+ "Consumer not found")))
          (throw+ "Unknown Signature method")))
      (throw+ "Invalid Oauth version"))))
