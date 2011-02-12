(ns jiksnu.xmpp.controller.subscription-controller
  (:use jiksnu.model
        jiksnu.xmpp.view)
  (:require [jiksnu.model.subscription :as model.subscription]))

(defn subscriptions
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (model.subscription/subscriptions recipient)]
    subscriptions))

(defn subscribers
  [request]
  (let [recipient (.getLocalpart (:to request))
        subscriptions (model.subscription/subscribers recipient)]
    subscriptions))

(defn subscribe
  [request]
  `())

(defn subscribed
  [request]
  '())
