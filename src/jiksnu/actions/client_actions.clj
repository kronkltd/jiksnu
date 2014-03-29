(ns jiksnu.actions.client-actions
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.model.client :as model.client]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.client-transforms :as transforms.client]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+ try+]]))

(def model-sym 'jiksnu.model.client)

(def create-fn (ns-resolve (the-ns model-sym) 'create))
(def delete-fn (ns-resolve (the-ns model-sym) 'delete))
(def fetch-fn  (ns-resolve (the-ns model-sym) 'fetch-by-id))

(defonce delete-hooks (ref []))

(defn prepare-create
  [domain]
  (-> domain
      transforms.client/set-_id
      transforms.client/set-type
      transforms.client/set-secret
      transforms.client/set-expiry
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
  (let [item (prepare-create params)]
    (create-fn item)))

(defn find-or-create
  [params]
  (or (fetch-fn (:_id params)) (create params)))

(defn handle-client-update
  [params]


  )

(defn handle-client-associate
  [params]
  (let [contacts (when-let [contacts (:contacts params)]
                   (string/split contacts #" "))
        type (:application_type params)
        title (:application_name params)
        logo_url (:logo_url params)
        redirect_uris (when-let [redirect-uris (:redirect_uris params)]
                        (string/split redirect-uris #" "))]
    ;; TODO: validate contacts
    (if-not (:access_token params)
      (if-not (:client_secret params)
        (let [host (:remoteHost params)
              webfinger (:remoteUser params)
              params (merge params
                            {:contacts contacts
                             :_id (:registration_access_token params)
                             :type type
                             :title title
                             :redirect_uris redirect_uris}
                            (when host {:host host})
                            (when webfinger {:webfinger webfinger}))
              client (create params)]
          client)
        (throw+ "Only set client_secret for update"))
      (throw+ "access_token not needed for registration"))))

(defaction register
  [params]
  (let [type (:type params)]
    (condp = type
      "client_update" (handle-client-update params)
      "client_associate" (handle-client-associate params)
      ;; NB: I'm copying pump.io's response here. Doubt it matters
      (throw+ "No registration type provided"))))
