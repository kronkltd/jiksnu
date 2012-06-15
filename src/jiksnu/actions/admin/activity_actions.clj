(ns jiksnu.actions.admin.activity-actions
  "This is the namespace for the admin pages for activities"
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]))

(def index*
  (model/make-indexer 'jiksnu.model.activity))

(defaction index
  [& options]
  (apply index* options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.activity-filters"
    "jiksnu.views.admin.activity-views"]))
