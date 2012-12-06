(ns jiksnu.model.feed-subscription
  (:use [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of presence-of valid? validation-set]])
  (:require [clj-statsd :as s]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [lamina.trace :as trace]
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

(def set-field!    (model/make-set-field! collection-name))

(defn fetch-by-id
  [id]
  (if-let [item (mc/find-map-by-id collection-name id)]
    (model/map->FeedSubscription item)))

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating feed subscription: %s" params)
        (mc/insert collection-name params)
        (let [item (fetch-by-id (:_id params))]
          (trace/trace :feed-subscriptions:created item)
          (s/increment "feed-subscriptions_created")
          item))
      (throw+ {:type :validation
               :errors errors}))))

(def count-records (model/make-counter collection-name))
(def delete        (model/make-deleter collection-name))
(def drop!         (model/make-dropper collection-name))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (s/increment (str collection-name " searched"))
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map model/map->FeedSubscription records))))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (fetch-all {:topic topic}))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index {:url 1 :callback 1} {:unique true})))
