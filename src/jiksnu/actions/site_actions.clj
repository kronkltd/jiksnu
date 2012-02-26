(ns jiksnu.actions.site-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]])))

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
  (doseq [namespace ['jiksnu.filters.site-filters
                     'jiksnu.views.site-views]]
    (require namespace)))
