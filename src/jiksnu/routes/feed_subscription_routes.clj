(ns jiksnu.routes.feed-subscription-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.feed-subscription-actions :as feed-subscription]))

(add-route! "/main/feed-subscriptions"      {:named "index feed-subscriptions"})
(add-route! "/main/feed-subscriptions/:id"  {:named "show feed-subscription"})
(add-route! "/model/feedSubscriptions/:id" {:named "feed-subscription model"})

(defn routes
  []
  [
   [[:get (named-path     "index feed-subscriptions")] #'feed-subscription/index]
   [[:get (formatted-path "index feed-subscriptions")] #'feed-subscription/index]
   [[:get (named-path     "show feed-subscription")]   #'feed-subscription/show]
   [[:get (formatted-path "show feed-subscription")]   #'feed-subscription/show]
   [[:get (formatted-path "feed-subscription model")]  #'feed-subscription/show]
   ])

(defn pages
  []
  [
   [{:name "feed-subscriptions"}    {:action #'feed-subscription/index}]
   ])
