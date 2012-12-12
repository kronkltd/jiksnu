(ns jiksnu.model.group
  (:use [jiksnu.transforms :only [set-_id set-created-time
                                  set-updated-time]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            [monger.result :as result])
  (:import jiksnu.model.Group))

(def collection-name "groups")
(def maker           #'model/map->Group)
(def page-size       20)

(def create-validators
  (validation-set
   (presence-of :_id)
   (presence-of :created)
   (presence-of :updated)))

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

(defn fetch-by-id
  [id]
  (s/increment (str collection-name "_fetched"))
  (when-let [item (mc/find-map-by-id collection-name id)]
    (model/map->Group item)))

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

(defn fetch-by-name
  [name]
  (model/map->Group (mc/find-one-as-map "groups" {:nickname name})))

