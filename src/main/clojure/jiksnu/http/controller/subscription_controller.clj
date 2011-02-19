(ns jiksnu.http.controller.subscription-controller
  (:use [jiksnu.session :only (current-user-id)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription))

(defn index
  [request]
  (model.subscription/index))

(defn subscribe
  [request]
  (if-let [actor (current-user-id)]
    (if-let [{{user-id "subscribeto"} :params} request]
      (if-let [user (model.user/show username domain)]
        (model.subscription/subscribe actor (:_id user))))))

(defn unsubscribe
  [request]
  (if-let [actor (current-user-id)]
    (if-let [{{user "unsubscribeto"} :params} request]
      (model.subscription/unsubscribe actor user))))

(defn delete
  "Deletes a subscription.

This action is primarily for the admin console.
In most cases, use the user-specific versions. (unsubscribe)"
  [{{id "id"} :params
    :as request}]
  (model.subscription/delete id))
