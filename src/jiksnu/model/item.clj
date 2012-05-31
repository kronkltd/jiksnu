(ns jiksnu.model.item
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (karras [entity :as entity]))
  (:import (jiksnu.model Activity Item)))

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
