(ns jiksnu.model.subscription
  (:use (ciste [debug :only [spy]])
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

(defn fetch-by-id
  [id]
  (entity/fetch-by-id Subscription id))

(defn fetch-all
  "Fetch all users"
  ([] (fetch-all {}))
  ([params & options]
     (apply entity/fetch Subscription params options)))

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

(defn find-by-users
  [actor target]
  (entity/fetch-one Subscription {:from (:_id actor) :to (:_id target)}))

(defn unsubscribe
  [actor user]
  (if-let [subscription (find-by-users actor user)]
    (do (entity/delete subscription)
        subscription)
    #_(entity/make Subscription {:from actor :to user :created (sugar/date)})))

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
  (fetch-all {:to (:_id user)}))

(defn subscriptions
  [user]
  (fetch-all {:from (:_id user)}))

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
