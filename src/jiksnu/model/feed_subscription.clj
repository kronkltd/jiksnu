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
   (presence-of :topic)))

(defn fetch-by-id
  [id]
  (if-let [record (mc/find-map-by-id collection-name id)]
    (model/map->FeedSubscription record)))

(defn prepare
  [params]
  (let [now (time/now)]
    (merge
     {:_id (model/make-id)
      :created now
      :updated now}
     params)))

(defn create
  [params & [options & _]]
  (let [params (prepare params)
        errors (create-validators params)]
    (if (empty? errors)
      (let [result (mc/insert collection-name params)]
        (if (result/ok? result)
          (fetch-by-id (:_id params))
          (throw+ {:type :error})))
      (throw+ {:type :validation
               :errors errors}))))

(defn delete
  "Delete feed source

This will generally not be called"
  [item]
  (log/debugf "Deleting subscription for %s" (:topic item))
  (mc/remove-by-id collection-name (:_id item))
  item)

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

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name)))

(defn drop!
  []
  (mc/remove collection-name))
