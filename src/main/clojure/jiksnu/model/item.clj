(ns jiksnu.model.item
  (:use jiksnu.model)
  (:require [karras.entity :as entity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Item
           jiksnu.model.Activity))

(defn drop!
  []
  (entity/delete-all Item))

(defn index
  [user]
  (entity/fetch Item {:user (:_id user)}))

(defn fetch-activities
  [user]
  (doall
   (filter identity (map
     #(-> % :activity model.activity/fetch-by-id)
     (index user)))))

(defn push
  [user activity]
  (entity/create Item {:user (:_id user)
                       :activity (:_id activity)}))
