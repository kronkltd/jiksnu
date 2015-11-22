(ns jiksnu.actions.request-token-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.request-token-transforms :as transforms.request-token]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as timbre]))

(def model-sym 'jiksnu.model.request-token)

(def create-fn (ns-resolve (the-ns model-sym) 'create))
(def delete-fn (ns-resolve (the-ns model-sym) 'delete))
(def fetch-fn  (ns-resolve (the-ns model-sym) 'fetch-by-id))

(defonce delete-hooks (ref []))

(defn prepare-create
  [params]
  (-> params
      transforms.request-token/set-_id
      transforms.request-token/set-secret
      transforms.request-token/set-verifier
      transforms.request-token/set-used
      transforms.request-token/set-authenticated
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

(defn get-request-token
  [params]
  (create params))

(defn show-authorization-form
  [token]
  (let [user (session/current-user)]
    [user token]))

(defn authorize
  [params]
  (timbre/info "authorizing")
  (let [id (:oauth_token params)
        token (model.request-token/fetch-by-id id)]
    (if (= (:verifier params) (:verifier token))
      (let [user (session/current-user)]
        (model.request-token/set-field! token :user (:_id user))
        token)
      (throw+ "Verifier does not match"))))
