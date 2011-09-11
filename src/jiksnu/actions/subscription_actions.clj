(ns jiksnu.actions.subscription-actions
  (:use (ciste [core :only (defaction)]
               [debug :only (spy)])
        (jiksnu model
                [session :only (current-user
                                current-user-id)]))
  (:require (jiksnu [namespace :as namespace])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

(defaction delete
;;   "Deletes a subscription.

;; This action is primarily for the admin console.
;; In most cases, use the user-specific versions. (unsubscribe)"
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
  [user]
  user)

(defaction remote-subscribe
  [& _])

(defaction remote-subscribe-confirm
  [& _])

(defaction subscribe
  [user]
  (let [actor (current-user)]
    (model.subscription/create
     {:from (:_id actor)
      :to (:_id user)
      :pending true})))

(defaction subscribed
  [actor user]
  (model.subscription/create
   {:from (:_id actor)
    :to (:_id user)}))

(defaction subscribers
  [user]
  [user (model.subscription/subscribers user)])

(defaction subscriptions
  [user]
  [user (model.subscription/subscriptions user)])


(defaction unsubscribe
  [actor-id user-id]
  (model.subscription/unsubscribe actor-id user-id))

(defaction subscribe-confirm
  [user]
  ;; TODO: unmark pending flag
  true)

(defn find-record
  [args]
  (model.subscription/find-record args))

(defn confirm
  [subscription]
  (model.subscription/confirm subscription))
