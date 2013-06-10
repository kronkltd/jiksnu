(ns jiksnu.model.item
  (:require [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates :as templates]
            [monger.collection :as mc])
  (:import jiksnu.model.Activity
           jiksnu.model.Item))

(def collection-name "items")
(def maker #'model/map->Item)
(def default-page-size 20)

(def create-validators
  (validation-set
   (type-of :_id           ObjectId)))

(def count-records (templates/make-counter     collection-name))
(def delete        (templates/make-deleter     collection-name))
(def drop!         (templates/make-dropper     collection-name))
(def set-field!    (templates/make-set-field!  collection-name))
(def fetch-by-id   (templates/make-fetch-by-id collection-name maker))
(def create        (templates/make-create      collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn    collection-name maker))

(defn push
  [user activity]
  (mc/insert collection-name {:user (:_id user)
                              :activity (:_id activity)}))
