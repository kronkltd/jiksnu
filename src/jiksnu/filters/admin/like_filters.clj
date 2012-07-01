(ns jiksnu.filters.admin.like-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.like-actions :only [index]]))

(deffilter #'index :http
  [action request]
  ;; TODO: pass page params
  (action))
