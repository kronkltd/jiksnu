(ns jiksnu.routes.search-routes
  (:require [jiksnu.actions.search-actions :as search]
            [jiksnu.routes.helpers :refer [add-route! named-path]]))

(add-route! "/main/search" {:named "search"})

(defn routes
  []
  [[[:get  (named-path "search")] #'search/perform-search]
   [[:post (named-path "search")] #'search/perform-search]
   [[:get  "/opensearch/people"]   #'search/os-people]
   [[:get  "/opensearch/notices"]  #'search/os-notice]
   ])
