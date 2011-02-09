(ns jiksnu.xmpp.controller.following-controller
  (:require [jiksnu.model.following :as following.model]
            [jiksnu.model.subscription :as subscription.model]))

(defn index
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (subscription.model/index :to recipient)]
    subscriptions))

(defn create
  [request]
  '())
