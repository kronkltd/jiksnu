(ns jiksnu.routes.feed-subscription-routes
  (:require [jiksnu.actions.feed-subscription-actions :as feed-subscription]))

(defn routes
  []
  [
   [[:get "/main/feed-subscriptions"] #'feed-subscription/index]
   [[:get "/main/feed-subscriptions.:format"] #'feed-subscription/index]
   [[:get "/main/feed-subscriptions/:id"]   #'feed-subscription/show]
   [[:get "/main/feed-subscriptions/:id.:format"]   #'feed-subscription/show]
   [[:get "/model/feedSubscriptions/:id"]  #'feed-subscription/show]
   ])

(defn pages
  []
  [
   [{:name "feed-subscriptions"}    {:action #'feed-subscription/index}]
   ])
