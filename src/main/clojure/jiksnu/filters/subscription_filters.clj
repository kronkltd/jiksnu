(ns jiksnu.filters.subscription-filters
  (:use ciste.debug
        ciste.filters
        jiksnu.actions.subscription-actions
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
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'index :http
  [action request]
  (action))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatus
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'ostatus :http
  [action request]
  (action))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatussub
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'ostatussub :http
  [action request]
  (let [{{profile :profile} :params} request]
    (action profile)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatussub-submit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'ostatussub-submit :http
  [action request]
  (let [{{profile :profile} :params} request]
    (if profile
      (let [[username password] (clojure.string/split profile #"@")
            user (model.user/find-or-create username password)]
        (action user)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-subscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (deffilter #'remote-subscribe :xmpp
;;   [actor user]
;;   ["subscribe" {"xmlns" pubsub-uri
;;                 "node" microblog-uri
;;                 "jid" (str (:username user) "@" (:domain user))}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-subscribe-confirm
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-subscribe-confirm :xmpp
  [action request]
  (let [subscriber (model.user/fetch-by-jid (:to request))
        subscribee (model.user/fetch-by-jid (:from request))
        subscription (model.subscription/find-record
                      {:to (:_id subscribee) :from (:_id subscriber)})]
    (model.subscription/confirm subscription)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscribe :http
  [action request]
  (if-let [{{user-id :subscribeto} :params} request]
    (if-let [user (model.user/fetch-by-id user-id)]
      (action user))))

(deffilter #'subscribe :xmpp
  [action request]
  (let [user (model.user/fetch-by-jid (:to request))]
    (action user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribed
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscribed :xmpp
  [action request]
  (let [subscriber (model.user/fetch-by-jid (:from request))
        subscribee (model.user/fetch-by-jid (:to request))]
    (action subscriber subscribee)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscribers :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/show id)]
    (action user)))

(deffilter #'subscribers :xmpp
  [action request]
  (if-let [user (model.user/fetch-by-jid (:to request))]
    (action user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscriptions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'subscriptions :http
  [action request]
  (let [{{id :id} :params} request
        user (model.user/show id)]
    (action user)))

(deffilter #'subscriptions :xmpp
  [action request]
  (let [user (model.user/fetch-by-jid (:to request))]
    (action user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; unsubscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'unsubscribe :http
  [action request]
  (if-let [actor (current-user-id)]
    (if-let [{{user :unsubscribeto} :params} request]
      (action actor (make-id user)))))

(deffilter #'unsubscribe :xmpp
  [action request]
  (let [{:keys [to from]} request
        user (model.user/fetch-by-jid to)
        subscriber (model.user/find-or-create-by-jid from)]
    (action (:_id subscriber) (:_id user))))
