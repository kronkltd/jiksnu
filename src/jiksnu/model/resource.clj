(ns jiksnu.model.resource
  (:use [ciste.config :only [config]]
        [clojure.core.incubator :only [-?>>]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of acceptance-of]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.query :as mq]))

(defonce page-size 20)
(def collection-name "resources")

(def create-validators
  (validation-set
   (presence-of   :_id)
   (presence-of   :url)
   (acceptance-of :local         :accept (partial instance? Boolean))

   ;; TODO: These should be joda times
   (presence-of   :created)
   (presence-of   :updated)
   ))


(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map model/map->Resource records))))

(defn fetch-by-id
  [id]
  (let [id (if (string? id) (model/make-id id) id)]
    (if-let [activity (mc/find-map-by-id collection-name id)]
      (model/map->Resource activity))))

(defn create
  [item]
  (let [errors (create-validators item)]
    (if (empty? errors)
      (do
        (log/debugf "Creating resource: %s" (pr-str item))
        (mc/insert collection-name item)
        (fetch-by-id (:_id item)))
      (throw+ {:type :validation :errors errors}))))

(defn drop!
  []
  (mc/remove collection-name))

(defn delete
  [item]
  (mc/remove-by-id collection-name (:_id item))
  item)

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
