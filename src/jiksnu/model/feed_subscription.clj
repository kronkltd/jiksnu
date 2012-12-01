(ns jiksnu.model.feed-subscription
  (:use [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [presence-of valid? validation-set]])
  (:require [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            [monger.result :as result])
  (:import jiksnu.model.FeedSubscription))

(def collection-name "feed_subscriptions")

(def create-validators
  (validation-set
   (presence-of :_id)
   (presence-of :url)
   (presence-of :callback)
   (presence-of :domain)
   (acceptance-of :local         :accept (partial instance? Boolean))

   (presence-of :created)
   (presence-of :updated)))

(defn set-field!
  "Update field to value"
  [item field value]
  (log/debugf "setting %s (%s = %s)" (:_id item) field value)
  (mc/update collection-name
             {:_id (:_id item)}
             {:$set {field value}}))

(defn fetch-by-id
  [id]
  (if-let [item (mc/find-map-by-id collection-name id)]
    (model/map->FeedSubscription item)))

(defn prepare
  [params]
  (let [now (time/now)]
    (merge
     {:_id (model/make-id)
      :created now
      :updated now}
     params)))

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating feed source: %s" params)

        (let [result (mc/insert collection-name params)]
         (if (result/ok? result)
           (fetch-by-id (:_id params))
           (throw+ {:type :error}))))
      (throw+ {:type :validation
               :errors errors}))))

(def count-records (model/make-counter collection-name))
(def delete        (model/make-deleter collection-name))
(def drop!         (model/make-dropper collection-name))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [page (get options :page 1)]
       (let [records (mq/with-collection collection-name
                        (mq/find params)
                        (mq/paginate :page page :per-page 20))]
         (map model/map->FeedSubscription records)))))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (fetch-all {:topic topic}))
