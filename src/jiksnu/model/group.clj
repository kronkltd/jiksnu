(ns jiksnu.model.group
  (:require [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Group))

(defn create
  [options]
  (mc/insert "groups" options))

(defn index
  []
  (map
   ->Group
   (mc/find-maps "groups")))

(defn fetch-by-name
  [name]
  (->Group (mc/find-one-as-map "groups"{:nickname name})))
