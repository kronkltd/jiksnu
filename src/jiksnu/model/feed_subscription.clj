(ns jiksnu.model.feed-subscription
  (:use [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of presence-of valid? validation-set]])
  (:require [clj-statsd :as s]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            [monger.result :as result])
  (:import jiksnu.model.FeedSubscription
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "feed_subscriptions")
(def maker           #'model/map->FeedSubscription)
(def page-size       20)

(def create-validators
  (validation-set
   (type-of :_id      ObjectId)
   (type-of :url      String)
   (type-of :callback String)
   (type-of :domain   String)
   (type-of :local    Boolean)
   (type-of :created  DateTime)
   (type-of :updated  DateTime)))

(def count-records (templates/make-counter       collection-name))
(def delete        (templates/make-deleter       collection-name))
(def drop!         (templates/make-dropper       collection-name))
(def remove-field! (templates/make-remove-field! collection-name))
(def set-field!    (templates/make-set-field!    collection-name))
(def fetch-by-id   (templates/make-fetch-by-id   collection-name maker))
(def create        (templates/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn      collection-name maker))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (fetch-all {:topic topic}))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index {:url 1 :callback 1} {:unique true})))
