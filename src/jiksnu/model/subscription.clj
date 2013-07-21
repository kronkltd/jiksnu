(ns jiksnu.model.subscription
  (:use [jiksnu.transforms :only [set-_id set-updated-time set-created-time]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.templates.model :as templates.model]
            [monger.collection :as mc]
            [monger.query :as mq])
  (:import jiksnu.model.Subscription
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "subscriptions")
(def maker #'model/map->Subscription)
(def default-page-size 20)

(def create-validators
  (validation-set
   (type-of :from    ObjectId)
   (type-of :to      ObjectId)
   (type-of :created DateTime)
   (type-of :updated DateTime)
   (type-of :_id     ObjectId)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn find-record
  [args]
  (model/map->Subscription (mc/find-one-as-map collection-name args)))

;; TODO: use set-field
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
  [actor target]
  (let [params {:from (:_id actor)
                :to (:_id target)}]
    (mc/any? collection-name params)))

(defn subscribed?
  "Does the user have a subscription to the actor"
  [actor target]
  (let [params {:from (:_id target)
                :to (:_id actor)}]
    (mc/any? collection-name params)))

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
