(ns jiksnu.modules.admin.filters.auth-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.modules.admin.actions.auth-actions))

(deffilter #'index :http
  [action request]
  (action))
