(ns jiksnu.actions.favorite-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]]))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(definitializer
  (require-namespaces
   ["jiksnu.filters.favorite-filters"
    "jiksnu.views.favorite-views"]))
