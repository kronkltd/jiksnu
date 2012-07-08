(ns jiksnu.routes.search-routes
  (:require [jiksnu.actions.search-actions :as search]))

(defn routes
  []
  [[[:get    "/main/search"]                                 #'search/perform-search]
   [[:post   "/main/search"]                                 #'search/perform-search]])
