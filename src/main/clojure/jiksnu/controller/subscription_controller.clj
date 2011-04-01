(ns jiksnu.controller.subscription-controller
  (:use ciste.debug
        jiksnu.model
        jiksnu.namespace
        [jiksnu.session :only (current-user-id)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

;; xmpp


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


;; http

(defn index
  [request]
  (model.subscription/index))

(defn subscribe
  [request]
  (if-let [actor (current-user-id)]
    (if-let [{{user-id "subscribeto"} :params} request]
      (if-let [user (model.user/fetch-by-id user-id)]
        (let [subscription (model.subscription/subscribe actor (:_id user))]
          (xmpp.view.subscription/notify-subscribe request subscription)
          subscription)))))

(defn unsubscribe
  [request]
  (if-let [actor (current-user-id)]
    (if-let [{{user "unsubscribeto"} :params} request]
      (let [subscription (model.subscription/find-record
                          {:from actor :to (make-id user)})]
        (model.subscription/delete subscription)
        (xmpp.view.subscription/notify-unsubscribe request subscription)
        true))))

(defn delete
  "Deletes a subscription.

This action is primarily for the admin console.
In most cases, use the user-specific versions. (unsubscribe)"
  [{{id "id"} :params
    :as request}]
  (model.subscription/delete id))

(defn ostatus
  [request]
  true)

(defn ostatussub
  [request]
  (let [{{profile "profile"} :params} request]
    (if profile
      (let [[username password] (clojure.string/split profile #"@")]
        (model.user/show username password))
      (User.))))

(defn ostatussub-submit
  [request]
  (let [{{profile "profile"} :params} request]
    (if profile
      (let [[username password] (clojure.string/split profile #"@")]
        (model.user/show username password)))))

(defn subscriptions
  [request]
  (let [{{id "id"} :params} request
        user (model.user/show id)]
    (model.subscription/subscriptions user)))

(defn subscribers
  [request]
  (let [{{id "id"} :params} request
        user (model.user/show id)]
    (model.subscription/subscribers user)))
