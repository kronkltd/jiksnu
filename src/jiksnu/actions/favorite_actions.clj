(ns jiksnu.actions.favorite-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [runner :only [require-namespaces]])))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(definitializer
  (require-namespaces
   ["jiksnu.filters.favorite-filters"
    "jiksnu.views.favorite-views"]))
