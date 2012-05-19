(ns jiksnu.filters.admin.activity-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.activity-actions :only [index]]))

(deffilter #'index :http
  [action request]
  (action))
