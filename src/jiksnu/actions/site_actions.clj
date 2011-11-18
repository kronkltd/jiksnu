(ns jiksnu.actions.site-actions)

(defaction service
  [id]
  ;; get user
  )

(defaction rsd
  []
  true
  )

(definitializer
  (doseq [namespace ['jiksnu.filters.site-filters
                     'jiksnu.views.site-views]]
    (require namespace)))
