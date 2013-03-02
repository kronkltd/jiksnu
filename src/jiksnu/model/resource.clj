(ns jiksnu.model.resource
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>>]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of acceptance-of]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.query :as mq]
            [net.cgrand.enlive-html :as enlive])
  (:import java.io.StringReader
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(defonce page-size 20)
(def collection-name "resources")

(def create-validators
  (validation-set
   (type-of :_id     ObjectId)
   (type-of :url     String)
   (type-of :domain  String)
   (type-of :local   Boolean)
   (type-of :created DateTime)
   (type-of :updated DateTime)))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
    (s/increment "resources searched")
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map model/map->Resource records))))

(defn fetch-by-url
  [url]
  (first (fetch-all {:url url})))

(defn get-link
  [item rel content-type]
  (first (util/rel-filter rel (:links item) content-type)))

(def set-field! (templates/make-set-field! collection-name))

(defn fetch-by-id
  [id]
  (let [id (if (string? id) (util/make-id id) id)]
    (s/increment "resources fetched")
    (if-let [item (mc/find-map-by-id collection-name id)]
      (model/map->Resource item))))

(def count-records (templates/make-counter collection-name))
(def create        (templates/make-create collection-name #'fetch-by-id #'create-validators))
(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))

(defn ensure-indexes
  []
  (doto collection-name
   (mc/ensure-index {:url 1} {:unique true})))

(defn response->tree
  [response]
  (enlive/html-resource (StringReader. (:body response))))

(defn get-links
  [tree]
  (enlive/select tree [:link]))

(defn meta->property
  "Convert a meta element to a property map"
  [meta]
  (let [attrs (:attrs meta)
        property (:property attrs)
        content (:content attrs)]
    (when (and property content)
      {property content})))

(defn get-meta-properties
  "Get a map of all the meta properties in the document"
  [tree]
  (->> (enlive/select tree [:meta])
       (map meta->property)
       (reduce merge)))

