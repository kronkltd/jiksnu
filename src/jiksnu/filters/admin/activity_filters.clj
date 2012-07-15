(ns jiksnu.filters.admin.activity-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.activity-actions :only [index]]
        [jiksnu.filters :only [parse-page parse-sorting]]))

(deffilter #'index :http
  [action request]
  (action {}
          (merge {}
                 (parse-page request)
                 (parse-sorting request))))
