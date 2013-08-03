(ns jiksnu.modules.admin.filters.auth-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.admin.auth-actions))

(deffilter #'index :http
  [action request]
  (action))
