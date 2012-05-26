(ns jiksnu.actions.subscription-actions
  (:use [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.debug :only [spy]]
        [ciste.runner :only [require-namespaces]]
        [ciste.model :only [implement]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.pubsub-actions :as actions.pubsub]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session])
  (:import javax.security.sasl.AuthenticationException
           jiksnu.model.Subscription
           jiksnu.model.User))

(defaction delete
  "Deletes a subscription.

   This action is primarily for the admin console.
   In most cases, use the user-specific versions. (unsubscribe)"
  [id]
  (model.subscription/delete id))

(defaction ostatus
  [& _]
  (implement))

(defaction ostatussub
  [profile]
  ;; TODO: Allow for http uri's
  (if profile
    (let [[username domain] (clojure.string/split profile #"@")]
      (model.user/get-user username domain))
    (User.)))

(defaction remote-subscribe
  [& _]
  (implement))

(defaction remote-subscribe-confirm
  [& _]
  (implement))

(defaction create
  [params & options]
  (model.subscription/create params))

(defaction update
  [subscription]
  (implement))

(defaction subscribe
  [actor user]
  ;; Set up a feed source to that user's public feed
  (actions.feed-source/subscribe user)
  (-> {:from (:_id actor)
       :to (:_id user)
       :pending true}
      create))

(defaction unsubscribed
  [actor user]
  (let [subscription (model.subscription/find-record
                      {:from (:_id actor)
                       :to (:_id user)})]
    (model.subscription/delete subscription)
    subscription))

(defaction ostatussub-submit
  [actor user]
  (subscribe actor user))

(defaction subscribed
  
  [actor user]
  (model.subscription/create
   {:from (:_id actor)
    :to (:_id user)}))

(defaction get-subscribers
  [user]
  [user (model.subscription/subscribers user)])

(defaction get-subscriptions
  [user]
  [user (model.subscription/subscriptions user)])


(defaction unsubscribe
  "User unsubscribes from another user"
  [actor target]
  (if-let [subscription (model.subscription/find-by-users actor target)]
    (model.subscription/unsubscribe actor target)
    (throw (RuntimeException. "Subscription not found"))))

(defaction subscribe-confirm
  [user]
  ;; TODO: unmark pending flag
  (implement))

(defaction confirm
  [subscription]
  (model.subscription/confirm subscription))

(definitializer
  (require-namespaces
   ["jiksnu.filters.subscription-filters"
    "jiksnu.helpers.subscription-helpers"
    "jiksnu.triggers.subscription-triggers"
    "jiksnu.views.subscription-views"]))
