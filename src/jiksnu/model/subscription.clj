(ns jiksnu.model.subscription
  (:require [jiksnu.db :refer [_db]]
            [jiksnu.model :as model]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.transforms :refer [set-_id set-updated-time set-created-time]]
            [jiksnu.validators :refer [type-of]]
            [monger.collection :as mc]
            [slingshot.slingshot :refer [throw+]]
            [validateur.validation :refer [validation-set presence-of]])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "subscriptions")
(def maker #'model/map->Subscription)
(def default-page-size 20)

(def create-validators
  (validation-set
   (type-of :from    String)
   (type-of :to      String)
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
  (model/map->Subscription (mc/find-one-as-map @_db collection-name args)))

;; TODO: use set-field
(defn confirm
  [subscription]
  (mc/update @_db collection-name {:_id (:_id subscription)}
             {:$set {:pending false}}))

(defn find-by-users
  [actor target]
  (fetch-all {:from (:_id actor) :to (:_id target)}))

(defn unsubscribe
  [actor target]
  (if-let [subscription (first (find-by-users actor target))]
    (do (mc/remove @_db collection-name {:to (:_id target) :from (:_id actor)})
        subscription)))

(defn subscribing?
  "Does the actor have a subscription to the user"
  [actor target]
  (let [params {:from (:_id actor)
                :to (:_id target)}]
    (mc/any? @_db collection-name params)))

(defn subscribed?
  "Does the user have a subscription to the actor"
  [actor target]
  (let [params {:from (:_id target)
                :to (:_id actor)}]
    (mc/any? @_db collection-name params)))

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
