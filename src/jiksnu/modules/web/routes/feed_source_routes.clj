(ns jiksnu.modules.web.routes.feed-source-routes
  (:require [ciste.initializer :refer [definitializer]]
            [jiksnu.actions.feed-source-actions :as feed-source]))

(defn routes
  []
  [
   [[:get "/main/feed-sources"]  #'feed-source/index]
   [[:get "/main/feed-sources.:format"]  #'feed-source/index]
   [[:get "/main/feed-sources/:id.:format"]  #'feed-source/show]
   [[:get "/main/feed-sources/:id"]  #'feed-source/show]
   [[:get "/main/push/callback"]     #'feed-source/process-updates]
   [[:get "/model/feed-sources/:id"] #'feed-source/show]
   ])

(defn pages
  []
  [
   [{:name "feed-sources"}    {:action #'feed-source/index}]
   ])
