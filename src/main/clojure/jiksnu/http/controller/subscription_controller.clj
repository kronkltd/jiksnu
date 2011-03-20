(ns jiksnu.http.controller.subscription-controller
  (:use jiksnu.model
        [jiksnu.session :only (current-user-id)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.xmpp.view.subscription-view :as xmpp.view.subscription])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

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
