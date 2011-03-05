(ns jiksnu.model.subscription
  (:use jiksnu.model)
  (:require [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Subscription))

(defn drop!
  []
  (entity/delete-all Subscription))

(defn find-record
  [args]
  (entity/fetch-one Subscription args))

(defn create
  [subscription & options]
  (println "subscription: " subscription)
  (if-let [from (:from subscription)]
    (if-let [to (:to subscription)]
      (let [option-map
            (merge {:created (sugar/date)}
                   subscription)]
        (let [subscription (find-record {:from from :to to})
              query (sugar/where (sugar/eq :from from)
                                      (sugar/eq :to to))]
          (println "option-map: " option-map)
          (println "query: " query)
          (let [response (entity/update Subscription query
                                        option-map :upsert)]
            (println "response: " response)
            (find-record {:from from :to to})
            ))))))

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
  (entity/delete
   (entity/fetch-one Subscription {:from actor :to user})))

(defn subscribing?
  "Does the actor have a subscription to the user"
  [actor user]
  (show :from actor :to user))

(defn subscribed?
  "Does the user have a subscription to the actor"
  [actor user]
  (show :from user :to actor))

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
