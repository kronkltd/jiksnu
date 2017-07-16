(ns jiksnu.modules.core.actions.client-actions
  (:require [ciste.config :refer [config]]
            [clj-time.coerce :as coerce]
            [clojure.string :as string]
            jiksnu.model.client
            [jiksnu.modules.core.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.client-transforms :as transforms.client]
            [slingshot.slingshot :refer [throw+]]))

(def model-ns 'jiksnu.model.client)

(def create-fn (ns-resolve (the-ns model-ns) 'create))
(def delete-fn (ns-resolve (the-ns model-ns) 'delete))
(def fetch-fn  (ns-resolve (the-ns model-ns) 'fetch-by-id))

(defonce delete-hooks (ref []))

(defn prepare-create
  [domain]
  (-> domain
      transforms.client/set-_id
      transforms.client/set-type
      transforms.client/set-secret
      transforms.client/set-expiry
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
  (taoensso.timbre/info "indexing clients")
  (apply index* options))

(defn create
  [params]
  (let [item (prepare-create params)]
    (create-fn item)))

(defn find-or-create
  [params]
  (or (fetch-fn (:_id params)) (create params)))

(defn handle-client-update
  [_]
  nil)

(defn handle-client-associate
  [params]
  (let [contacts (when-let [contacts (:contacts params)]
                   (string/split contacts #" "))
        type (:application_type params)
        title (:application_name params)
        redirect_uris (when-let [redirect-uris (:redirect_uris params)]
                        (string/split redirect-uris #" "))]
    ;; TODO: validate contacts
    (if-not (:access_token params)
      (if-not (:client_secret params)
        (let [host (:remoteHost params)
              webfinger (:remoteUser params)
              client-uri (format "https://%s/oauth/request_token" (config :domain))
              params (merge params
                            {:contacts contacts
                             :_id (:registration_access_token params)
                             :type type
                             :title title
                             :redirect_uris redirect_uris}
                            (when host {:host host})
                            (when webfinger {:webfinger webfinger}))
              {client-id :_id
               expires :secret-expires
               :keys [token secret created]} (create params)
              created (int (/ (coerce/to-long created) 1000))]
          (merge {:client_id client-id
                  :client_id_issued_at created
                  :registration_access_token token
                  :registration_client_uri client-uri}
                 (when secret {:client_secret secret})
                 (when expires {:expires_at expires})))
        (throw+ "Only set client_secret for update"))
      (throw+ "access_token not needed for registration"))))

(defn register
  [params]
  (let [type (:type params)]
    (condp = type
      "client_update" (handle-client-update params)
      "client_associate" (handle-client-associate params)
      ;; NB: I'm copying pump.io's response here. Doubt it matters
      (throw+ "No registration type provided"))))
