(ns jiksnu.model.item
  (:require [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [monger.collection :as mc])
  (:import (jiksnu.model Activity Item)))

(def collection-name "items")

(defn drop!
  []
  (mc/remove collection-name))

(defn index
  [user]
  (mc/find-maps collection-name {:user (:_id user)}))

(defn fetch-activities
  [user]
  (doall
   (filter identity (map
                     #(-> % :activity model.activity/fetch-by-id)
                     (index user)))))

(defn push
  [user activity]
  (mc/insert collection-name {:user (:_id user)
                              :activity (:_id activity)}))
