(ns jiksnu.filters.admin.subscription-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.admin.subscription-actions))

(deffilter #'index :http
  [action request]
  (action))
