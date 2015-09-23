(ns jiksnu.modules.web.routes.feed-subscription-routes
  (:require [jiksnu.actions.feed-subscription-actions :as feed-subscription]))


(defn pages
  []
  [
   [{:name "feed-subscriptions"}    {:action #'feed-subscription/index}]
   ])
