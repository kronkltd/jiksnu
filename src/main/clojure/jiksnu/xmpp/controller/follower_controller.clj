(ns jiksnu.xmpp.controller.follower-controller
    (:use jiksnu.model
          jiksnu.xmpp.view)
    (:require [jiksnu.model.follower :as follower.model]
              [jiksnu.model.subscription :as subscription.model]))

(defn index
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (subscription.model/index :from recipient)]
    subscriptions))

(defn create
  [request]
  `())

(defn process-subscriber-query
  [items-node]
  (let [recipient nil
        subscribers (follower.model/index :to recipient)]
    subscribers))
