(ns jiksnu.actions.access-token-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [jiksnu.model.client :as model.client]
            [jiksnu.model.access-token :as model.access-token]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.access-token-transforms :as transforms.access-token]
            [slingshot.slingshot :refer [throw+ try+]]))

(def model-sym 'jiksnu.model.access-token)

(def create-fn (ns-resolve (the-ns model-sym) 'create))
(def delete-fn (ns-resolve (the-ns model-sym) 'delete))
(def fetch-fn  (ns-resolve (the-ns model-sym) 'fetch-by-id))

(defonce delete-hooks (ref []))

(defn prepare-create
  [params]
  (-> params
      transforms.access-token/set-_id
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
  (log/info "getting access token")
  (let [version (get params "oauth_version")]
    (if (= version "1.0")
      (let [signature-method (get params "oauth_signature_method")]
        (if (= signature-method "HMAC-SHA1")
          (let [nonce (get params "oauth_nonce")
                consumer-key (get params "oauth_consumer_key")
                timestamp (get params "oauth_timestamp")
                signature (get params "oauth_signature")
                verifier (get params "oauth_verifier")
                token (get params "oauth_token")]
            (if-let [client (model.client/fetch-by-id consumer-key)]
              (if-let [request-token (model.request-token/fetch-by-id token)]
                (if (:access-token request-token)
                  (throw+ "Request Token has already been consumed")
                  (if (= consumer-key (:client request-token))
                    (let [params {:client (:_id client)
                                  :request-token (:_id request-token)
                                  :secret "foo"
                                  }]
                      (create (log/spy :info params)))
                    (throw+ "Consumer Key does not match request token's consumer.")))
                (throw+ "Request token not found"))
              (throw+ "Consumer not found")))
          (throw+ "Unknown Signature method")))
      (throw+ "Invalid Oauth version"))))
