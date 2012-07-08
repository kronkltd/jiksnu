(ns jiksnu.routes.search-routes
  (:require )
  )

(defn routes
  []
  [
     [[:get    "/opensearch/people"]                           #'search/os-people]
     [[:get    "/opensearch/notices"]                          #'search/os-notice]

   ]
  )
