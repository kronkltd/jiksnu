(ns jiksnu.filters.admin.key-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.key-actions :only [index]]
        [jiksnu.filters :only [parse-page parse-sorting]]))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))
