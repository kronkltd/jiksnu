(ns jiksnu.controller.subscription-controller
  (:use ciste.core
        ciste.debug
        jiksnu.model
        jiksnu.namespace
        [jiksnu.session :only (current-user-id)])
  (:require [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(defaction delete
  "Deletes a subscription.

This action is primarily for the admin console.
In most cases, use the user-specific versions. (unsubscribe)"
  [id]
  (model.subscription/delete id))

(defaction index
  [& _]
  (model.subscription/index))

(defaction ostatus
  [& _]
  true)

(defaction ostatussub
  [profile]
  (if profile
    (let [[username password] (clojure.string/split profile #"@")]
      (model.user/show username password))
    (User.)))

(defaction ostatussub-submit
  [& _])

(defaction remote-subscribe
  [& _])

(defaction remote-subscribe-confirm
  [& _])

(defaction subscribe
  [& _])

(defaction subscribed
  [& _])

(defaction subscribers
  [user]
  (model.subscription/subscribers user))

(defaction subscriptions
  [user]
  (model.subscription/subscriptions user))

(defaction unsubscribe
  [actor-id user-id]
  (model.subscription/unsubscribe actor-id user-id))

