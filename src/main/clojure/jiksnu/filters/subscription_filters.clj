(ns jiksnu.controller.subscription-controller
  (:use ciste.debug
        ciste.filters
        jiksnu.model
        jiksnu.namespace
        [jiksnu.session :only (current-user-id)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'delete :http
  "Deletes a subscription.

This action is primarily for the admin console.
In most cases, use the user-specific versions. (unsubscribe)"
  [{{id "id"} :params
    :as request}]
  (model.subscription/delete id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'index :http
  [request]
  (model.subscription/index))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatus
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'ostatus :http
  [request]
  true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatussub
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'ostatussub :http
  [request]
  (let [{{profile "profile"} :params} request]
    (if profile
      (let [[username password] (clojure.string/split profile #"@")]
        (model.user/show username password))
      (User.))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatussub-submit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'ostatussub-submit :http
  [request]
  (let [{{profile "profile"} :params} request]
    (if profile
      (let [[username password] (clojure.string/split profile #"@")]
        (model.user/show username password)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-subscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-subscribe :xmpp
  [actor user]
  ["subscribe" {"xmlns" pubsub-uri
                "node" microblog-uri
                "jid" (str (:username user) "@" (:domain user))}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-subscribe-confirm
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-subscribe-confirm :xmpp
  [request]
  (let [subscriber (model.user/fetch-by-jid (:to request))
        subscribee (model.user/fetch-by-jid (:from request))
        subscription (model.subscription/find-record
                      {:to (:_id subscribee) :from (:_id subscriber)})]
    (model.subscription/confirm subscription)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscribe :http
  [request]
  (if-let [actor (current-user-id)]
    (if-let [{{user-id "subscribeto"} :params} request]
      (if-let [user (model.user/fetch-by-id user-id)]
        (model.subscription/subscribe actor (:_id user))))))

(deffilter #'subscribe :xmpp
  [request]
  (let [to (:to request)
        from (:from request)]
    (let [user (model.user/fetch-by-jid to)
          subscriber (model.user/find-or-create-by-jid from)]
      (model.subscription/subscribe (:_id subscriber)
                                    (:_id user)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribed
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscribed :xmpp
  [request]
  '())

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscribers :http
  [request]
  (let [{{id "id"} :params} request
        user (model.user/show id)]
    (model.subscription/subscribers user)))

(deffilter #'subscribers :xmpp
  [request]
  (if-let [recipient (model.user/fetch-by-jid (:to request))]
    (model.subscription/subscribers recipient)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscriptions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscriptions :http
  [request]
  (let [{{id "id"} :params} request
        user (model.user/show id)]
    (model.subscription/subscriptions user)))

(deffilter #'subscriptions :xmpp
  [request]
  (let [recipient (model.user/fetch-by-jid (:to request))
        subscriptions (model.subscription/subscriptions recipient)]
    subscriptions))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; unsubscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'unsubscribe :http
  [request]
  (if-let [actor (current-user-id)]
    (if-let [{{user "unsubscribeto"} :params} request]
      (let [subscription (model.subscription/find-record
                          {:from actor :to (make-id user)})]
        (model.subscription/delete subscription)
        true))))

(deffilter #'unsubscribe :xmpp
  [request]
  (let [to (:to request)
        from (:from request)]
    (let [user (model.user/fetch-by-jid to)
          subscriber (model.user/find-or-create-by-jid from)]
      (model.subscription/unsubscribe (:_id subscriber)
                                      (:_id user))
      true)))
