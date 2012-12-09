(ns jiksnu.model.resource
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>>]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of acceptance-of]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [monger.collection :as mc]
            [monger.query :as mq]))

(defonce page-size 20)
(def collection-name "resources")

(def create-validators
  (validation-set
   (presence-of   :_id)
   (presence-of   :url)
   (presence-of   :domain)
   (acceptance-of :local         :accept (partial instance? Boolean))

   ;; TODO: These should be joda times
   (presence-of   :created)
   (presence-of   :updated)))

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

(defn create
  [params]
  (let [errors (create-validators params)]
    (if (empty? errors)
      (do
        (mc/insert collection-name params)
        (let [item (fetch-by-id (:_id params))]
          (log/debugf "Creating resource: %s" (pr-str item))
          (trace/trace :resources:created item)
          (s/increment "resources created")
          item))
      (throw+ {:type :validation :errors errors}))))

(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))
(def count-records (templates/make-counter collection-name))

(defn ensure-indexes
  []
  (doto collection-name
   (mc/ensure-index {:url 1} {:unique true})))
