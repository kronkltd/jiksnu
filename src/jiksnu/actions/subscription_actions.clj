(ns jiksnu.actions.subscription-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [ciste.model :only [implement]]
        [jiksnu.session :only [current-user]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import javax.security.sasl.AuthenticationException
           jiksnu.model.Subscription
           jiksnu.model.User))

(defaction delete
  "Deletes a subscription.

   This action is primarily for the admin console.
   In most cases, use the user-specific versions. (unsubscribe)"
  [subscription]
  (model.subscription/delete subscription))

(def index*
  (model/make-indexer 'jiksnu.model.subscription))

(defaction index
  [& options]
  (apply index* options))

(defaction ostatus
  [& _]
  (implement))

(defaction ostatussub
  [profile]
  ;; TODO: Allow for http uri's
  (if profile
    (let [[username domain] (clojure.string/split profile #"@")]
      (model.user/get-user username domain))
    (model/map->User {})))

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
       :local true
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
  "User requests a subscription to a uri"
  [uri]
  (if-let [actor (current-user)]
    (if-let [user  (if (re-matches #".*@.*" uri)
                     ;; uri is an acct uri
                     (actions.user/find-or-create-by-uri uri)
                     
                     ;; uri is a http uri
                     (actions.user/find-or-create-by-remote-id {:id uri}))]
      (subscribe actor user)
      (throw+ {:type :validation :message "Could not determine user"
               ;; TODO: list failed fields
               }))
    (throw+ {:type :authentication :message "must be logged in"})))

(defaction subscribed
  [actor user]
  (model.subscription/create
   {:from (:_id actor)
    :to (:_id user)
    :local false}))

(defaction get-subscribers
  [user]
  [user (index {:to (:_id user)})])

(defaction get-subscriptions
  [user]
  [user (index {:from (:_id user)})])


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
    "jiksnu.views.subscription-views"])

  (dosync
   (alter actions.user/delete-hooks
          conj
          (fn [user]
            (let [subscriptions (concat
                                 (:items (second (get-subscribers user)))
                                 (:items (second (get-subscriptions user))))]
              (doseq [subscription subscriptions]
                (delete subscription)))))))
