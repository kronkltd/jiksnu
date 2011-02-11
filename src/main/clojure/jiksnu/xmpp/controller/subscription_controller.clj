(ns jiksnu.xmpp.controller.subscription-controller
  (:use jiksnu.model
        jiksnu.xmpp.view)
  (:require [jiksnu.model.follower :as follower.model]
            [jiksnu.model.subscription :as subscription.model]))

(defn subscriptions
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (subscription.model/index :from recipient)]
    subscriptions))

(defn subscribers
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (subscription.model/index :to recipient)]
    subscriptions))

(defn subscribe
  [request]
  `())

(defn subscribed
  [request]
  '())
