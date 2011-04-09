(ns jiksnu.filters.settings-filters
  (:use ciste.debug
        ciste.filters
        jiksnu.actions.settings-actions)
  )

(deffilter #'edit :http
  [action request]
  (action)
  )
