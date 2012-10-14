(ns jiksnu.actions.favorite-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]))

(defaction user-list
  [user]
  ;; TODO: implement
  [])

(definitializer
  (require-namespaces
   ["jiksnu.filters.favorite-filters"
    "jiksnu.views.favorite-views"]))
