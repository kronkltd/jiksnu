(ns jiksnu.routes.search-routes
  (:require [jiksnu.actions.search-actions :as search]))

(defn routes
  []
  [[[:get  "/main/search"] #'search/perform-search]
   [[:post "/main/search"] #'search/perform-search]
   [[:get  "/opensearch/people"]   #'search/os-people]
   [[:get  "/opensearch/notices"]  #'search/os-notice]
   ])
