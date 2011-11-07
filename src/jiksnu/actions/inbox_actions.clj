(ns jiksnu.actions.inbox-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]])
        jiksnu.model)
  (:require (jiksnu.model [item :as model.item])))

(definitializer
  (doseq [namespace ['jiksnu.filters.inbox-filters
                     'jiksnu.views.inbox-views]]
    (require namespace)))

(defaction index
  [user]
  (model.item/fetch-activities user))
