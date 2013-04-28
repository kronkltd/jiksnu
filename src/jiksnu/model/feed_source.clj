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
(def maker           #'model/map->FeedSource)
(def page-size       20)

(def create-validators
  (validation-set
   (type-of :_id     ObjectId)
   (type-of :topic   String)
   (type-of :domain  String)
   (type-of :local   Boolean)
   (type-of :status  String)
   (type-of :created DateTime)
   (type-of :updated DateTime)))

(def count-records (templates/make-counter     collection-name))
(def delete        (templates/make-deleter     collection-name))
(def drop!         (templates/make-dropper     collection-name))
(def set-field!    (templates/make-set-field!  collection-name))
(def fetch-by-id   (templates/make-fetch-by-id collection-name maker))
(def create        (templates/make-create      collection-name #'fetch-by-id #'create-validators))

(defn find-record
  [options & args]
  (if-let [item (mc/find-one-as-map collection-name options)]
    (maker item)))

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
       (map maker records))))

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
