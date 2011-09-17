(ns jiksnu.filters.settings-filters
  (:use (ciste [debug :only (spy)]
               filters)
        jiksnu.actions.settings-actions))

(deffilter #'edit :http
  [action request]
  (action))
