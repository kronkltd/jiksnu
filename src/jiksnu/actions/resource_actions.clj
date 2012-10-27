(ns jiksnu.actions.resource-actions
  (:use [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions :only [invoke-action]]
        [jiksnu.transforms :only [set-_id set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.namespace :as ns]))

(defonce delete-hooks (ref []))

(defn prepare-delete
  ([item]
     (prepare-delete item @delete-hooks))
  ([item hooks]
     (if (seq hooks)
       (recur ((first hooks) item) (rest hooks))
       item)))

(defn prepare-create
  [params]
  (-> params
      set-_id
      set-updated-time
      set-created-time))

(defaction create
  [params]
  (let [item (prepare-create params)]
    (s/increment "resources created")
    (model.resource/create item)))

(defaction find-or-create
  [params]
  (or (model.resource/fetch-by-url (:url params))
      (create params)))

(defaction delete
  "Delete the resource"
  [item]
  (if-let [item (prepare-delete item)]
    (do (model.resource/delete item)
        item)
    (throw+ "Could not delete record")))

(def index*
  (model/make-indexer 'jiksnu.model.resource
                      :sort-clause {:updated -1}))

(defaction index
  [& args]
  (apply index* args))

(defaction discover
  [item]
  item)

(defaction update
  [item]
  item)

(defaction show
  [item]
  item)

(definitializer
  (l/receive-all
   model/pending-resources
   (fn [[url ch]]
     (l/enqueue ch (find-or-create {:url url}))))

  (require-namespaces
   ["jiksnu.filters.resource-filters"
    ;; "jiksnu.sections.resource-sections"
    ;; "jiksnu.triggers.resource-triggers"
    "jiksnu.views.resource-views"]))
