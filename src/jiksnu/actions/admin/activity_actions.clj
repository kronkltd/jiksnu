(ns jiksnu.actions.admin.activity-actions
  "This is the namespace for the admin pages for activities"
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]))

(def index*
  (model/make-indexer 'jiksnu.model.activity))

(defaction index
  [& [params & [options]]]
  (index* params options))

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.activity-filters"
    "jiksnu.views.admin.activity-views"]))
