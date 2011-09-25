(ns jiksnu.filters.like-filters
  (:use (ciste [filters :only (deffilter)])
        jiksnu.actions.like-actions))

(deffilter #'like-activity :http
  [action request]
  (-> request :params :id show action))

