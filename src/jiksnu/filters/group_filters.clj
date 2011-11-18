(ns jiksnu.filters.group-filters
  (:use (ciste [filters :only [deffilter]])))

(deffilter #'user-list :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))
