(ns jiksnu.model.feed-subscription
  (:require [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.templates.model :as templates.model]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import (org.joda.time DateTime)
           (org.bson.types ObjectId)))

(def collection-name "feed_subscriptions")
(def maker           #'model/map->FeedSubscription)
(def page-size       20)

(def create-validators
  (v/validation-set
   (vc/type-of :_id ObjectId)
   #_(vc/type-of :url      String)
   #_(vc/type-of :callback String)
   #_(vc/type-of :domain   String)
   #_(vc/type-of :local    Boolean)
   (vc/type-of :created DateTime)
   (vc/type-of :updated DateTime)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (fetch-all {:topic topic}))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index (db/get-connection) {:url 1 :callback 1} {:unique true})))
