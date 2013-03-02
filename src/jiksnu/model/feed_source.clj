(ns jiksnu.model.feed-source
  (:use [jiksnu.transforms :only [set-_id set-created-time set-updated-time]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of validation-set presence-of]])
  (:require [clj-statsd :as s]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq])
  (:import jiksnu.model.FeedSource
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "feed_sources")

(def create-validators
  (validation-set
   (type-of :_id     ObjectId)
   (type-of :topic   String)
   (type-of :domain  String)
   (type-of :local   Boolean)
   (type-of :status  String)
   (type-of :created DateTime)
   (type-of :updated DateTime)))

(def set-field! (templates/make-set-field! collection-name))

(defn fetch-by-id
  [id]
  (when-let [item (mc/find-map-by-id collection-name id)]
    (model/map->FeedSource item)))

;; (defn push-value!
;;   [source key value]
;;   (update source
;;     {:$addToSet {key value}}))

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (log/debugf "Creating feed source: %s" params)
        (mc/insert collection-name params)
        (let [item (fetch-by-id (:_id params))]
          (trace/trace :feed-sources:created item)
          (s/increment "feed-sources_created")
          item))
      (throw+ {:type :validation :errors errors}))))

(def count-records (templates/make-counter collection-name))
(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))

(defn find-record
  [options & args]
  (if-let [record (mc/find-one-as-map collection-name options)]
    (model/map->FeedSource record)))

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
       (map model/map->FeedSource records))))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (find-record {:topic topic}))

(defn find-by-user
  "Returns the source associated with a user"
  [user]
  (fetch-by-id (:update-source user)))

(defn ensure-indexes
  []
  (doto collection-name
   (mc/ensure-index {:topic 1} {:unique true})))
