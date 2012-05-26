(ns jiksnu.filters.search-filters
  (:use [ciste.debug :only [spy]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.search-actions))

(deffilter #'perform-search :http
  [action request]
  (-> request :params action))
