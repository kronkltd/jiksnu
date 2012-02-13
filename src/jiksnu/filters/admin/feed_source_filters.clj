(ns jiksnu.filters.admin.feed-source-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.admin.feed-source-actions))

(deffilter #'index :http
  [action request]
  (action))
