(ns jiksnu.modules.core.filters.search-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.search-actions :as actions.search]))

(deffilter #'actions.search/perform-search :http
  [action request]
  (-> request :params action))
