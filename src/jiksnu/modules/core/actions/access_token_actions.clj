(ns jiksnu.modules.core.actions.access-token-actions
  (:require jiksnu.modules.core.model.access-token
            [jiksnu.modules.core.model.client :as model.client]
            [jiksnu.modules.core.model.request-token :as model.request-token]
            [jiksnu.modules.core.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.access-token-transforms :as transforms.access-token]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]))

(def model-ns 'jiksnu.modules.core.model.access-token)

(def create-fn (ns-resolve (the-ns model-ns) 'create))
(def delete-fn (ns-resolve (the-ns model-ns) 'delete))
(def fetch-fn  (ns-resolve (the-ns model-ns) 'fetch-by-id))

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
      transforms/set-updated-time))

(defn prepare-delete
  ([item]
   (prepare-delete item @delete-hooks))
  ([item hooks]
   (if (seq hooks)
     (recur ((first hooks) item) (rest hooks))
     item)))

(defn delete
  [item]
  (let [item (prepare-delete item)]
    (delete-fn item)))

(defn show
  [item]
  item)

(def index*
  (templates.actions/make-indexer model-ns :sort-clause {:created 1}))

(defn index
  [& options]
  (apply index* options))

(defn create
  [params]
  (let [params (prepare-create params)]
    (create-fn params)))

(defn find-or-create
  [params]
  (or (fetch-fn (:_id params)) (create params)))

(defn get-access-token
  [params]
  (timbre/info "getting access token")
  (let [{consumer-key "oauth_consumer_key"
         signature-method "oauth_signature_method"
         token        "oauth_token"
         version      "oauth_version"} params]
    (if (= version "1.0")
      (if (= signature-method "HMAC-SHA1")
        (if-let [client (model.client/fetch-by-id consumer-key)]
          (if-let [request-token (model.request-token/fetch-by-id token)]
            (if (:access-token request-token)
              (throw+ "Request Token has already been consumed")
              (if (= consumer-key (:client request-token))
                (let [params {:client (:_id client)
                              :request-token (:_id request-token)
                              ;; FIXME: generate a random secret
                              :secret "foo"}]
                  (create params))
                (throw+ "Consumer Key does not match request token's consumer.")))
            (throw+ "Request token not found"))
          (throw+ "Consumer not found"))
        (throw+ "Unknown Signature method"))
      (throw+ "Invalid Oauth version"))))
