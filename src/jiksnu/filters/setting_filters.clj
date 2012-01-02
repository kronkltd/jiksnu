(ns jiksnu.filters.setting-filters
  (:use (ciste [debug :only (spy)]
               filters)
        jiksnu.actions.setting-actions))

(deffilter #'admin-edit-page :http
  [action request]
  (action))
