(ns jiksnu.filters.search-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.search-actions))

(deffilter #'perform-search :http
  [action request]
  (-> request :params action))
