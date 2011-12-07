(ns jiksnu.filters.subscription-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        (clojure.core [incubator :only [-?> -?>>]])
        jiksnu.actions.subscription-actions
        (jiksnu [session :only [current-user current-user-id]]))
  (:require (jiksnu [model :as model]
                    [namespace :as namespace])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [user :as model.user])))

(deffilter #'delete :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'ostatus :http
  [action request]
  (action))

(deffilter #'ostatussub :http
  [action request]
  (let [{{profile :profile} :params} request]
    (action profile)))

(deffilter #'ostatussub-submit :http
  [action request]
  (let [{{profile :profile} :params} request]
    (if profile
      (let [[username password] (clojure.string/split profile #"@")
            user (actions.user/find-or-create username password)]
        (action (current-user) user)))))

(deffilter #'subscribe :http
  [action request]
  (-?>> request :params :subscribeto
        model.user/fetch-by-id (action (current-user))))

(deffilter #'get-subscribers :http
  [action request]
  (let [{{username :username} :params} request
        user (model.user/show username)]
    (action user)))

(deffilter #'get-subscriptions :http
  [action request]
  (let [{{username :username} :params} request
        user (model.user/show username)]
    (action user)))

(deffilter #'unsubscribe :http
  [action request]
  (if-let [actor (current-user-id)]
    (if-let [{{user :unsubscribeto} :params} request]
      (action actor (model/make-id user)))))






(deffilter #'remote-subscribe-confirm :xmpp
  [action request]
  (let [subscriber (actions.user/fetch-by-jid (:to request))
        subscribee (actions.user/fetch-by-jid (:from request))
        subscription (find-record
                      {:to (:_id subscribee) :from (:_id subscriber)})]
    (confirm subscription)))

(deffilter #'subscribe :xmpp
  [action request]
  (let [user (actions.user/fetch-by-jid (:to request))]
    (action user)))

(deffilter #'subscribed :xmpp
  [action request]
  (let [subscriber (actions.user/fetch-by-jid (:from request))
        subscribee (actions.user/fetch-by-jid (:to request))]
    (action subscriber subscribee)))

(deffilter #'get-subscribers :xmpp
  [action request]
  (if-let [user (actions.user/fetch-by-jid (:to request))]
    (action user)))

(deffilter #'get-subscriptions :xmpp
  [action request]
  (let [user (actions.user/fetch-by-jid (:to request))]
    (action user)))

(deffilter #'unsubscribe :xmpp
  [action request]
  (let [{:keys [to from]} request
        user (actions.user/fetch-by-jid to)
        subscriber (actions.user/find-or-create-by-jid from)]
    (action (:_id subscriber) (:_id user))))
