(ns jiksnu.routes.feed-source-routes
  (:require [ciste.initializer :refer [definitializer]]
            [jiksnu.actions.feed-source-actions :as feed-source]
            [jiksnu.routes.helpers :refer [add-route! named-path formatted-path]]))

(add-route! "/main/feed-sources"      {:named "index feed-sources"})
(add-route! "/main/feed-sources/:id"  {:named "show feed-source"})
(add-route! "/model/feed-sources/:id" {:named "feed-source model"})
(add-route! "/main/push/callback"    {:named "push callback"})

(defn routes
  []
  [
   [[:get "/main/feed-sources"]  #'feed-source/index]
   [[:get "/main/feed-sources.:format"]  #'feed-source/index]
   [[:get (named-path     "show feed-source")]  #'feed-source/show]
   [[:get (formatted-path "show feed-source")]  #'feed-source/show]
   [[:get (named-path     "push callback")]     #'feed-source/process-updates]
   [[:get (formatted-path "feed-source model")] #'feed-source/show]
   ])

(defn pages
  []
  [
   [{:name "feed-sources"}    {:action #'feed-source/index}]
   ])
