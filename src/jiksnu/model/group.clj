(ns jiksnu.model.group
  (:use [jiksnu.transforms :only [set-_id set-created-time
                                  set-updated-time]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            [monger.result :as result])
  (:import jiksnu.model.Group))

(def collection-name "groups")
(defonce page-size 20)

(def set-field!    (templates/make-set-field! collection-name))
(def count-records (templates/make-counter    collection-name))
(def delete        (templates/make-deleter    collection-name))
(def drop!         (templates/make-dropper    collection-name))

(def create-validators
  (validation-set
   (presence-of :_id)
   (presence-of :created)
   (presence-of :updated)))

(declare fetch-by-id)

(defn create
  [group]
  (let [errors (create-validators group)]
    (if (empty? errors)
      (do
        (log/debugf "Creating group: %s" (pr-str group))
        (mc/insert "groups" group)
        (fetch-by-id (:_id group)))
      (throw+ {:type :validation
               :errors errors}))))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [page (get options :page 1)]
       (let [records  (mq/with-collection collection-name
                        (mq/find params)
                        (mq/paginate :page page :per-page 20))]
         (map model/map->Group records)))))

(defn fetch-by-id
  [id]
  (if-let [group (mc/find-map-by-id collection-name id)]
    (model/map->Group group)))

(defn fetch-by-name
  [name]
  (model/map->Group (mc/find-one-as-map "groups"{:nickname name})))

