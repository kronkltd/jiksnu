(ns jiksnu.filters.subscription-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        (clojure.core [incubator :only [-?> -?>>]])
        jiksnu.actions.subscription-actions
        (jiksnu [session :only [current-user current-user-id]]))
  (:require (jiksnu [model :as model]
                    [namespace :as namespace])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user])))

(deffilter #'delete :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [subscription (model.subscription/fetch-by-id id)]
      (action subscription))))

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
  (if-let [actor (current-user)]
    (-?>> request :params :subscribeto
          model.user/fetch-by-id (action actor))))

(deffilter #'get-subscribers :http
  [action request]
  (let [{{username :username
          id :id} :params} request
          user (or (when username (model.user/get-user username))
                   (when id (model.user/fetch-by-id id)))]
    (action user)))

(deffilter #'get-subscriptions :http
  [action request]
  (let [{{username :username
          id :id} :params} request
          user (or (when username (model.user/get-user username))
                   (when id (model.user/fetch-by-id id)))]
    (action user)))

(deffilter #'unsubscribe :http
  [action request]
  (if-let [actor (current-user)]
    (let [params {:params request}]
      (if-let [id (or (:unsubscribeto params)
                      (:id params))]
        (if-let [user (model.user/fetch-by-id id)]
          (action actor (model/make-id id))
          (throw (RuntimeException. "User not found")))))))

(deffilter #'remote-subscribe-confirm :xmpp
  [action request]
  (let [subscriber (actions.user/fetch-by-jid (:to request))
        subscribee (actions.user/fetch-by-jid (:from request))
        subscription (model.subscription/fetch-all
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
  (if-let [user (actions.user/fetch-by-jid (:to request))]
    (action user)))

(deffilter #'unsubscribe :xmpp
  [action request]
  (let [{:keys [to from]} request
        user (actions.user/fetch-by-jid to)
        subscriber (actions.user/find-or-create-by-jid from)]
    (action (:_id subscriber) (:_id user))))
