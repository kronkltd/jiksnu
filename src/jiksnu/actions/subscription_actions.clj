(ns jiksnu.actions.subscription-actions
  (:use (ciste [core :only [defaction]]
               [debug :only [spy]])
        (jiksnu model))
  (:require (jiksnu [session :as session])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import javax.security.sasl.AuthenticationException
           jiksnu.model.Subscription
           jiksnu.model.User))

(defaction delete
;;   "Deletes a subscription.

;; This action is primarily for the admin console.
;; In most cases, use the user-specific versions. (unsubscribe)"
  [id]
  (model.subscription/delete id))

(defaction index
  [& _]
  (if (session/is-admin?)
    (model.subscription/index)
    (throw (AuthenticationException. "Must be admin"))))

(defaction ostatus
  [& _]
  true)

(defaction ostatussub
  [profile]
  (if profile
    (let [[username password] (clojure.string/split profile #"@")]
      (model.user/show username password))
    (User.)))

(defaction remote-subscribe
  [& _])

(defaction remote-subscribe-confirm
  [& _])

(defaction subscribe
  [actor user]
  (model.subscription/create
   {:from (:_id actor)
    :to (:_id user)
    :pending true}))

(defaction ostatussub-submit
  [user]
  (subscribe user))

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
