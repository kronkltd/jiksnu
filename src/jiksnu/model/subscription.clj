(ns jiksnu.model.subscription
  (:use [jiksnu.transforms :only [set-_id set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [monger.collection :as mc]
            [monger.query :as mq])
  (:import jiksnu.model.Subscription))

(def collection-name "subscriptions")
(def default-page-size 20)

(def create-validators
  (validation-set
   (presence-of :from)
   (presence-of :to)
   (presence-of :created)
   (presence-of :updated)
   (presence-of :_id)))

(defn prepare
  [subscription]
  (-> subscription
      set-_id
      set-updated-time
      set-created-time))

(defn drop!
  []
  (mc/remove collection-name))

(defn delete
  [subscription]
  (mc/remove-by-id collection-name (:_id subscription)))

(defn find-record
  [args]
  (model/map->Subscription (mc/find-one-as-map collection-name args)))

(defn fetch-by-id
  [id]
  (if-let [subscription (mc/find-map-by-id collection-name id)]
    (model/map->Subscription subscription)
    (log/warnf "Could not find subscription with id: %s" id)))

(defn fetch-all
  "Fetch all users"
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [page (get options :page 1)
           page-size (get options :page-size default-page-size)]
       (->> (mq/with-collection collection-name
              (mq/find params)
              ;; TODO: sorting
              (mq/paginate :page page :per-page page-size))
            (map model/map->Subscription)))))

(defn create
  [subscription & options]
  (let [subscription (prepare subscription)
        errors (create-validators subscription)]
    (if (empty? errors)
      (do
        (log/debugf "creating subscription: %s" (pr-str subscription))
        (mc/insert collection-name subscription)
        (fetch-by-id (:_id subscription)))
      (throw+ {:type :validation :errors errors}))))

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
  (mc/any? collection-name {:from actor :to user}))

(defn subscribed?
  "Does the user have a subscription to the actor"
  [actor user]
  (mc/any? collection-name {:from user :to actor}))

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

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
