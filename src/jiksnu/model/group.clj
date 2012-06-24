(ns jiksnu.model.group
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            [monger.result :as result])
  (:import jiksnu.model.Group))

(def collection-name "groups")

(defn drop!
  []
  (mc/remove collection-name))

(defn delete
  [group]
  (let [result (mc/remove-by-id collection-name (:_id group))]
    (if (result/ok? result)
      group)))

(defn fetch-by-id
  [id]
  (if-let [group (mc/find-map-by-id collection-name id)]
    (model/map->Group group)))

(defn create
  [params]
  (let [params (-> params
                   (assoc :_id (model/make-id)))
        result (mc/insert "groups" params)]
    (if (result/ok? result)
      (fetch-by-id (:_id params)))))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (let [page (get options :page 1)]
       (let [records  (mq/with-collection collection-name
                        (mq/find params)
                        (mq/paginate :page page :per-page 20))]
         (map model/map->Group records)))))

(defn fetch-by-name
  [name]
  (model/map->Group (mc/find-one-as-map "groups"{:nickname name})))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
