(ns jiksnu.model.group
  (:require [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Group))

(def collection-name "groups")

(defn create
  [options]
  (mc/insert "groups" options))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (map model/map->Group
          (mc/find-maps "groups" params))))

(defn fetch-by-name
  [name]
  (model/map->Group (mc/find-one-as-map "groups"{:nickname name})))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
