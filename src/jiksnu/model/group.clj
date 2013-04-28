(ns jiksnu.model.group
  (:use [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [monger.collection :as mc]
            [monger.query :as mq])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "groups")
(def maker           #'model/map->Group)
(def page-size       20)

(def create-validators
  (validation-set
   (type-of :_id     ObjectId)
   (type-of :created DateTime)
   (type-of :updated DateTime)))

(def count-records (templates/make-counter     collection-name))
(def delete        (templates/make-deleter     collection-name))
(def drop!         (templates/make-dropper     collection-name))
(def set-field!    (templates/make-set-field!  collection-name))
(def fetch-by-id   (templates/make-fetch-by-id collection-name maker))
(def create        (templates/make-create      collection-name #'fetch-by-id #'create-validators))

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

(defn fetch-by-name
  [name]
  (maker (mc/find-one-as-map collection-name {:nickname name})))
