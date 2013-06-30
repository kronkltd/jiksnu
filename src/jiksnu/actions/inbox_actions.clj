(ns jiksnu.actions.inbox-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.model.item :as model.item]))

(defaction index
  [user]
  #_(model.item/fetch-activities user))

(definitializer
  (require-namespaces
   ["jiksnu.filters.inbox-filters"
    "jiksnu.views.inbox-views"]))
