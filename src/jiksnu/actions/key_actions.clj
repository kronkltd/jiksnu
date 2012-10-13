(ns jiksnu.actions.key-actions
  (:use [ciste.config :only [config]]
        [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.key :as model.key])
  (:import jiksnu.model.User))

(defaction create
  [params]
  (model.key/create params))

(defaction delete
  [record]
  (model.key/delete record))

(defaction show
  [record] record)

(def index*
  (model/make-indexer 'jiksnu.model.key
                      :sort-clause [{:_id 1}]))

(defaction index
  [& options]
  (apply index* options))

(defn generate-key-for-user
  "Generate key for the user and store the result."
  [^User user]
  (let [params (assoc (model.key/pair-hash (model.key/generate-key))
                 :userid (:_id user))]
    (create params)))

(definitializer
  (require-namespaces
   ["jiksnu.filters.key-filters"
    "jiksnu.sections.key-sections"
    "jiksnu.triggers.key-triggers"
    "jiksnu.views.key-views"]))

