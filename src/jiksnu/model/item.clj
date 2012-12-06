(ns jiksnu.model.item
  (:require [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates :as templates]
            [monger.collection :as mc])
  (:import jiksnu.model.Activity
           jiksnu.model.Item))

(def collection-name "items")

(def set-field! (templates/make-set-field! collection-name))
(def count-records (templates/make-counter collection-name))
(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))

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
