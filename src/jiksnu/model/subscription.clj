(ns jiksnu.model.subscription
  (:use (ciste [debug :only (spy)])
        jiksnu.model)
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (jiksnu [namespace :as namespace])
            (jiksnu.model [user :as model.user])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import jiksnu.model.Subscription))

(defn drop!
  []
  (entity/delete-all Subscription))

(defn find-record
  [args]
  (entity/fetch-one Subscription args))

(defn create
  [subscription & options]
  (if-let [from (:from subscription)]
    (if-let [to (:to subscription)]
      (let [option-map
            (merge {:created (sugar/date)}
                   subscription)
            subscription (find-record {:from from :to to})
            query (sugar/where (sugar/eq :from from)
                               (sugar/eq :to to))
            response (entity/update Subscription query
                                    option-map :upsert)]
        (find-record {:from from :to to})))))

(defn index
  [& args]
  (let [option-map (apply hash-map args)]
    (entity/fetch Subscription option-map)))

(defn show
  [& args]
  (let [option-map (apply hash-map args)]
    (entity/fetch-one Subscription option-map)))

(defn delete
  [subscription]
  (entity/delete subscription))

(defn subscribe
  [actor user]
  (create {:from actor :to user :pending true}))

(defn confirm
  [subscription]
  (entity/save (assoc subscription :pending false)))

(defn unsubscribe
  [actor user]
  (if-let [subscription (entity/fetch-one Subscription {:from actor :to user})]
    (do (entity/delete subscription)
        subscription)
    (entity/make Subscription {:from actor :to user :created (sugar/date)})))

(defn subscribing?
  "Does the actor have a subscription to the user"
  [actor user]
  (show :from actor :to user))

(defn subscribed?
  "Does the user have a subscription to the actor"
  [actor user]
  (show :from user :to actor))

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
  (index :to (:_id user)))

(defn subscriptions
  [user]
  (index :from (:_id user)))

(defn create-pending
  [actor user]
  (entity/create Subscription
                 {:from actor
                  :to user
                  :pending true}))

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
           ["pubsub" {"xmlns" namespace/pubsub}
            ["subscriptions" {"node" namespace/microblog}]])}))

(defn subscribers-request
  [from to]
  (tigase/make-packet
   {:to (tigase/make-jid to)
    :from (tigase/make-jid from)
    :type :get
    ;; :id (fseq :id)
    :body (element/make-element
           "pubsub" {"xmlns" namespace/pubsub}
           ["subscribers" {"node" namespace/microblog}])}))
