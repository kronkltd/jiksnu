(ns jiksnu.xmpp.controller.subscription-controller
  (:use ciste.debug
        jiksnu.model
        jiksnu.namespace
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
  (if-let [recipient (model.user/fetch-by-jid (:to request))]
    (model.subscription/subscribers recipient)))

(defn subscribe
  [request]
  (let [to (:to request)
        from (:from request)]
    (let [user (model.user/fetch-by-jid to)
          subscriber (model.user/find-or-create-by-jid from)]
      (model.subscription/subscribe (:_id subscriber)
                                    (:_id user)))))

(defn remote-subscribe
  [actor user]
  ["subscribe" {"xmlns" pubsub-uri
                "node" microblog-uri
                "jid" (str (:username user) "@" (:domain user))}])

(defn remote-subscribe-confirm
  [request]
  (let [subscriber (model.user/fetch-by-jid (:to request))
        subscribee (model.user/fetch-by-jid (:from request))
        subscription (model.subscription/find-record
                      {:to (:_id subscribee) :from (:_id subscriber)})]
    (model.subscription/confirm subscription)))

(defn subscribed
  [request]
  '())

(defn unsubscribe
  [request]
  (let [to (:to request)
        from (:from request)]
    (let [user (model.user/fetch-by-jid to)
          subscriber (model.user/find-or-create-by-jid from)]
      (model.subscription/unsubscribe (:_id subscriber)
                                      (:_id user))
      true)))
