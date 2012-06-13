(ns jiksnu.filters.admin.key-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.key-actions :only [index]]))

(deffilter #'index :http
  [action request]
  (-> request :params action))
