(ns jiksnu.actions.request-token-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [jiksnu.model.request-token :as model.request-token]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [slingshot.slingshot :refer [throw+ try+]]))

(def model-sym 'jiksnu.model.request-token)

(def create-fn (ns-resolve (the-ns model-sym) 'create))
(def delete-fn (ns-resolve (the-ns model-sym) 'delete))
(def fetch-fn  (ns-resolve (the-ns model-sym) 'fetch-by-id))

(defonce delete-hooks (ref []))

(defn prepare-create
  [domain]
  (-> domain
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
  (let [item (prepare-create params)]
    (create-fn item)))

(defn find-or-create
  [params]
  (or (fetch-fn (:_id params)) (create params)))

