(ns jiksnu.filters.tag-filters
  (:use (ciste [filters :only [deffilter]])
        jiksnu.actions.tag-actions))

(deffilter #'show :http
  [action request]
  (-> request :params :name action))
