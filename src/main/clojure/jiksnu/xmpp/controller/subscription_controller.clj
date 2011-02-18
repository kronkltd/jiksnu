(ns jiksnu.xmpp.controller.subscription-controller
  (:use jiksnu.model
        jiksnu.xmpp.view)
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(defn subscriptions
  [request]
  (let [recipient (model.user/fetch-by-jid (:to request))
        subscriptions (model.subscription/subscriptions recipient)]
    subscriptions))

(defn subscribers
  [request]
  (let [recipient (model.user/fetch-by-jid (:to request))
        subscriptions (model.subscription/subscribers recipient)]
    subscriptions))

(defn subscribe
  [request]
  `())

(defn subscribed
  [request]
  '())
