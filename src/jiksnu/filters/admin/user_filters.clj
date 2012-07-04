(ns jiksnu.filters.admin.user-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.admin.user-actions))

(deffilter #'index :http
  [action request]
  (action))
