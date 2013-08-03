(ns jiksnu.modules.admin.filters.key-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.key-actions :only [index]]
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))
