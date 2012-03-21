(ns jiksnu.actions.subscription-actions
  (:use (ciste [config :only [definitializer]]
               [core :only [defaction]]
               [debug :only [spy]]))
  (:require (clojure.tools [logging :as log])
            (jiksnu [model :as model]
                    [session :as session])
            (jiksnu.actions [feed-source-actions :as actions.feed-source]
                            [pubsub-actions :as actions.pubsub])
            
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
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
  true)

(defaction ostatussub
  [profile]
  ;; TODO: Allow for http uri's
  (if profile
    (let [[username domain] (clojure.string/split profile #"@")]
      (model.user/get-user username domain))
    (User.)))

(defaction remote-subscribe
  [& _])

(defaction remote-subscribe-confirm
  [& _])

(defaction subscribe
  [actor user]
  ;; Set up a feed source to that user's public feed
  (actions.feed-source/subscribe user)
  (model.subscription/create
   {:from (:_id actor)
    :to (:_id user)
    :pending true}))

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
  [actor-id user-id]
  (model.subscription/unsubscribe actor-id user-id))

(defaction subscribe-confirm
  [user]
  ;; TODO: unmark pending flag
  true)

(defaction confirm
  [subscription]
  (model.subscription/confirm subscription))

(definitializer
  (doseq [namespace ['jiksnu.filters.subscription-filters
                     'jiksnu.helpers.subscription-helpers
                     ;; 'jiksnu.sections.subscription-sections
                     'jiksnu.triggers.subscription-triggers
                     'jiksnu.views.subscription-views
                     ]]
    (require namespace)))
