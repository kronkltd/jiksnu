(ns jiksnu.routes.search-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.search-actions :as search]))

(add-route! "/main/search" {:named "search"})

(defn routes
  []
  [[[:get  (named-path "search")] #'search/perform-search]
   [[:post (named-path "search")] #'search/perform-search]
   [[:get  "/opensearch/people"]   #'search/os-people]
   [[:get  "/opensearch/notices"]  #'search/os-notice]
   ])
