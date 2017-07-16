(ns jiksnu.modules.core.model.feed-source
  (:require [jiksnu.modules.core.db :as db]
            [jiksnu.modules.core.model :as model]
            [jiksnu.modules.core.templates.model :as templates.model]
            [jiksnu.modules.core.validators :as vc]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "feed_sources")
(def maker           #'model/map->FeedSource)
(def page-size       20)

(def create-validators
  (v/validation-set
   (vc/type-of :_id     ObjectId)
   (vc/type-of :topic   String)
   (vc/type-of :domain  String)
   (vc/type-of :local   Boolean)
   (vc/type-of :status  String)
   (vc/type-of :created DateTime)
   (vc/type-of :updated DateTime)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def push-value!   (templates.model/make-push-value!   collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn find-record
  [options & _]
  (if-let [item (mc/find-one-as-map (db/get-connection) collection-name options)]
    (maker item)))

(defn fetch-by-topic
  "Fetch a single source by it's topic id"
  [topic]
  (find-record {:topic topic}))

(defn find-by-user
  "Returns the source associated with a user"
  [user]
  (when-let [id (:update-source user)]
    (fetch-by-id id)))

(defn ensure-indexes
  []
  (mc/ensure-index (db/get-connection) collection-name {:topic 1} {:unique true}))
