(ns jiksnu.xmpp.controller.subscription-controller
  (:use jiksnu.model
        jiksnu.xmpp.view)
  (:require [jiksnu.model.subscription :as model.subscription]))

(defn subscriptions
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (model.subscription/index :from recipient)]
    subscriptions))

(defn subscribers
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (model.subscription/index :to recipient)]
    subscriptions))

(defn subscribe
  [request]
  `())

(defn subscribed
  [request]
  '())
