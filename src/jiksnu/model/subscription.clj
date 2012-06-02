(ns jiksnu.model.subscription
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-time.core :as time]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [monger.collection :as mc])
  (:import jiksnu.model.Subscription))

(def collection-name "subscriptions")

(defn delete
  [subscription]
  (mc/remove-by-id collection-name (:_id subscription)))

(defn drop!
  []
  (mc/remove collection-name))

(defn find-record
  [args]
  (model/map->Subscription (mc/find-one-as-map collection-name args)))

(defn fetch-by-id
  [id]
  (model/map->Subscription (mc/find-map-by-id collection-name id)))

(defn fetch-all
  "Fetch all users"
  ([] (fetch-all {}))
  ([params & options]
     (map model/map->Subscription
          (mc/find-maps collection-name params))))

(defn create
  [subscription & options]
  (if-let [from (:from subscription)]
    (if-let [to (:to subscription)]
      (let [option-map
            (merge {:created (time/now)}
                   subscription)
            subscription (find-record {:from from :to to})
            query {:from from :to to}
            response (mc/update collection-name query
                                    option-map :upsert)]
        (find-record {:from from :to to})))))

(defn subscribe
  [actor user]
  (create {:from actor :to user :pending true}))

(defn confirm
  [subscription]
  (mc/update collection-name {:$set {:pending false}}))

(defn find-by-users
  [actor target]
  (fetch-all {:from (:_id actor) :to (:_id target)}))

(defn unsubscribe
  [actor user]
  (if-let [subscription (find-by-users actor user)]
    (do (mc/remove collection-name subscription)
        subscription)))

(defn subscribing?
  "Does the actor have a subscription to the user"
  [actor user]
  (mc/any? {:from actor :to user}))

(defn subscribed?
  "Does the user have a subscription to the actor"
  [actor user]
  (mc/any? {:from user :to actor}))

(defn get-actor
  [subscription]
  (-> subscription
      :from model.user/fetch-by-id))

(defn get-target
  [subscription]
  (-> subscription
      :to model.user/fetch-by-id))

(defn subscribers
  [user]
  (fetch-all {:to (:_id user)}))

(defn subscriptions
  [user]
  (fetch-all {:from (:_id user)}))

(defn create-pending
  [actor user]
  (create {:from actor :to user :pending true}))

(defn pending?
  [subscription]
  (true? (:pending subscription)))

(defn subscriptions-request
  "returns a xmpp packet requesting subscriptions"
  [from to]
  (tigase/make-packet
   {:to (tigase/make-jid to)
    :from (tigase/make-jid from)
    :type :get
    :body (element/make-element
           ["pubsub" {"xmlns" ns/pubsub}
            ["subscriptions" {"node" ns/microblog}]])}))

(defn subscribers-request
  [from to]
  (tigase/make-packet
   {:to (tigase/make-jid to)
    :from (tigase/make-jid from)
    :type :get
    ;; :id (fseq :id)
    :body (element/make-element
           "pubsub" {"xmlns" ns/pubsub}
           ["subscribers" {"node" ns/microblog}])}))
