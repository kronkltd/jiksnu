(ns jiksnu.model.feed-source
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.validators :refer [type-of]]
            [monger.collection :as mc]
            [validateur.validation :refer [validation-set]])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "feed_sources")
(def maker           #'model/map->FeedSource)
(def page-size       20)

(def create-validators
  (validation-set
   ;; (type-of :_id     ObjectId)
   ;; (type-of :topic   String)
   ;; (type-of :domain  String)
   ;; (type-of :local   Boolean)
   ;; (type-of :status  String)
   ;; (type-of :created DateTime)
   ;; (type-of :updated DateTime)
))

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
  [options & args]
  (if-let [item (mc/find-one-as-map collection-name options)]
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
  (doto collection-name
   (mc/ensure-index {:topic 1} {:unique true})))
