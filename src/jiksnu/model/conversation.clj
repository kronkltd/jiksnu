(ns jiksnu.model.conversation
  (:use [ciste.config :only [config]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of presence-of
                                      validation-set]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq])
  (:import jiksnu.model.Conversation
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "conversations")
(def maker #'model/map->Conversation)
(def default-page-size 20)

(def create-validators
  (validation-set
   (type-of :_id           ObjectId)
   (type-of :url           String)
   (type-of :created       DateTime)
   (type-of :updated       DateTime)
   (type-of :domain        String)
   (type-of :local         Boolean)
   (type-of :update-source ObjectId)))

(def count-records (templates/make-counter    collection-name))
(def delete        (templates/make-deleter    collection-name))
(def drop!         (templates/make-dropper    collection-name))
(def set-field!    (templates/make-set-field! collection-name))

(defn fetch-all
  [& [params options]]
  (s/increment (str collection-name "_searched"))
  (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
        records (mq/with-collection collection-name
                  (mq/find params)
                  (merge sort-clause)
                  (mq/paginate :page (:page options 1)
                               :per-page (:page-size options 20)))]
    (map maker records)))

(def fetch-by-id   (templates/make-fetch-by-id collection-name maker))

(def create        (templates/make-create collection-name #'fetch-by-id #'create-validators))

(defn find-by-url
  [url]
  (fetch-all {:url url}))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index {:url 1} {:unique true})))
