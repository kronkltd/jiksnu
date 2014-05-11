(ns jiksnu.routes.feed-subscription-routes
  (:require [jiksnu.actions.feed-subscription-actions :as feed-subscription]
            [jiksnu.routes.helpers :refer [add-route! named-path formatted-path]]))

(add-route! "/main/feed-subscriptions"      {:named "index feed-subscriptions"})
(add-route! "/main/feed-subscriptions/:id"  {:named "show feed-subscription"})
(add-route! "/model/feedSubscriptions/:id" {:named "feed-subscription model"})

(defn routes
  []
  [
   [[:get "/main/feed-subscriptions"] #'feed-subscription/index]
   [[:get "/main/feed-subscriptions.:format"] #'feed-subscription/index]
   [[:get (named-path     "show feed-subscription")]   #'feed-subscription/show]
   [[:get (formatted-path "show feed-subscription")]   #'feed-subscription/show]
   [[:get (formatted-path "feed-subscription model")]  #'feed-subscription/show]
   ])

(defn pages
  []
  [
   [{:name "feed-subscriptions"}    {:action #'feed-subscription/index}]
   ])
