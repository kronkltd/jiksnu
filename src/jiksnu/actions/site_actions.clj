(ns jiksnu.actions.site-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [runner :only [require-namespaces]])))

(defaction service
  [id]
  ;; get user
  true
  )

(defaction rsd
  []
  true
  )

(definitializer
  (require-namespaces
   ["jiksnu.filters.site-filters"
    "jiksnu.views.site-views"]))
