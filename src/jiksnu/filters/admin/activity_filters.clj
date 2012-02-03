(ns jiksnu.filters.admin.activity-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.admin.activity-actions))

(deffilter #'index :http
  [action request]
  (action))
