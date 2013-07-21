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

(def collection-name "resources")
(def maker           #'model/map->Resource)
(def page-size       20)

(def create-validators
  (validation-set
   (type-of :_id     ObjectId)
   (type-of :url     String)
   (type-of :domain  String)
   (type-of :local   Boolean)
   (type-of :created DateTime)
   (type-of :updated DateTime)))

(def count-records (templates/make-counter       collection-name))
(def delete        (templates/make-deleter       collection-name))
(def drop!         (templates/make-dropper       collection-name))
(def remove-field! (templates/make-remove-field! collection-name))
(def set-field!    (templates/make-set-field!    collection-name))
(def fetch-by-id   (templates/make-fetch-by-id   collection-name maker))
(def create        (templates/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn      collection-name maker))

(defn fetch-by-url
  [url]
  (first (fetch-all {:url url})))

(defn get-link
  [item rel content-type]
  (first (util/rel-filter rel (:links item) content-type)))

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
