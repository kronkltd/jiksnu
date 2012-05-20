(ns jiksnu.actions.inbox-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [jiksnu.model.item :as model.item]))

(defaction index
  [user]
  (model.item/fetch-activities user))

(definitializer
  (require-namespaces
   ["jiksnu.filters.inbox-filters"
    "jiksnu.views.inbox-views"]))
