(ns jiksnu.actions.client-actions
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
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
      ;; transforms/set-_id
      transforms/set-created-time))

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
  (let [item (prepare-create (log/spy :info params))]
    (create-fn (log/spy :info item))))

(defn find-or-create
  [params]
  (or (fetch-fn (:_id params)) (create params)))

(defaction register
  [params]
  (let [client (create params)]
    (let [request-token (actions.request-token/create {:client (:_id client)})]
      (-> client
          (assoc :token (:token request-token))
          (assoc :secret (util/generate-token 32))
          (assoc :secret-expires 0)
          ))))
