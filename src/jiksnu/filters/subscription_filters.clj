(ns jiksnu.filters.subscription-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions.subscription-actions :only [delete ostatus confirm
                                                    ostatussub ostatussub-submit
                                                    get-subscribers get-subscriptions
                                                    subscribe unsubscribe
                                                    remote-subscribe-confirm
                                                    subscribed]]
        [jiksnu.session :only [current-user current-user-id]]
        [slingshot.slingshot :only [throw+]])
  (:require [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(deffilter #'delete :http
  [action request]
  (-?> request :params :id model.subscription/fetch-by-id action))

(deffilter #'ostatus :http
  [action request]
  (action))

(deffilter #'ostatussub :http
  [action request]
  (let [{{profile :profile} :params} request]
    (action profile)))

(deffilter #'ostatussub-submit :http
  [action request]
  (-> request :params :profile action))

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

(deffilter #'subscribe :http
  [action request]
  (if-let [actor (current-user)]
    (let [params (:params request)]
      (if-let [target (-?> (or (:id params) (:subscribeto params))
                           model.user/fetch-by-id)]
        (action actor target)
        (throw+ "User not found")))
    (throw+ "Must be logged in")))

(deffilter #'unsubscribe :http
  [action request]
  (if-let [actor (current-user)]
    (let [params (:params request)]
      (if-let [target (-?> (or (:id params) (:unsubscribeto params))
                           model.user/fetch-by-id)]
        (action actor target)
        (throw+ "User not found")))
    (throw+ "Must be logged in")))

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
