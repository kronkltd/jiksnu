(ns jiksnu.model.group
  (:require [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Group))

(defn create
  [options]
  (mc/insert "groups" options))

(defn index
  []
  (map model/map->Group
   (mc/find-maps "groups")))

(defn fetch-by-name
  [name]
  (model/map->Group (mc/find-one-as-map "groups"{:nickname name})))
