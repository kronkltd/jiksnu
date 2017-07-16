(ns jiksnu.modules.core.model.subscription
  (:require [jiksnu.db :as db]
            [jiksnu.modules.core.model :as model]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.modules.core.templates.model :as templates.model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.transforms :refer [set-_id set-updated-time set-created-time]]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "subscriptions")
(def maker #'model/map->Subscription)
(def default-page-size 20)

(def create-validators
  (v/validation-set
   (vc/type-of :from    String)
   (vc/type-of :to      String)
   (vc/type-of :created DateTime)
   (vc/type-of :updated DateTime)
   (vc/type-of :_id     ObjectId)))

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
  (model/map->Subscription (mc/find-one-as-map (db/get-connection) collection-name args)))

;; TODO: use set-field
(defn confirm
  [subscription]
  (mc/update (db/get-connection) collection-name {:_id (:_id subscription)}
             {:$set {:pending false}}))

(defn find-by-users
  [actor target]
  (fetch-all {:from (:_id actor) :to (:_id target)}))

;; TODO: Should take a subscription
(defn unsubscribe
  [actor target]
  (if-let [subscription (first (find-by-users actor target))]
    (do (mc/remove (db/get-connection) collection-name {:to (:_id target) :from (:_id actor)})
        subscription)))

(defn subscribing?
  "Does the actor have a subscription to the user"
  [actor target]
  (let [params {:from (:_id actor)
                :to (:_id target)}]
    (mc/any? (db/get-connection) collection-name params)))

(defn subscribed?
  "Does the user have a subscription to the actor"
  [actor target]
  (let [params {:from (:_id target)
                :to (:_id actor)}]
    (mc/any? (db/get-connection) collection-name params)))

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
