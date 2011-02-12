(ns jiksnu.model.subscription
  (:use jiksnu.model)
  (:require [karras.entity :as entity]
            [karras.sugar :as sugar])
  (:import jiksnu.model.Subscription
           org.bson.types.ObjectId))

(defn make-id
  [id]
  (ObjectId. id))

(defn drop!
  []
  (entity/delete-all Subscription))

(defn create
  [subscription & options]
  (let [option-map
        (merge {:to ""
                :from ""
                :created (sugar/date)}
               subscription)]
    (entity/create Subscription option-map)))

(defn index
  [& args]
  (let [option-map (apply hash-map args)]
    (entity/fetch Subscription option-map)))

(defn show
  [& args]
  (let [option-map (apply hash-map args)]
    (entity/fetch-one Subscription option-map)))

(defn delete
  [id]
  (if-let [subscription (show :_id (make-id id))]
    (entity/delete subscription)))

(defn subscribe
  [actor user]
  (create {:from actor :to user}))

(defn unsubscribe
  [actor user]
  (delete {:from actor :to user}))

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
  (index :to user))

(defn subscriptions
  [user]
  (index :from user))
