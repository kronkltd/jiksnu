(ns jiksnu.model.item
  (:use [jiksnu.validators :only [type-of]]
        [validateur.validation :only [acceptance-of presence-of
                                      validation-set]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.templates.model :as templates.model]
            [monger.collection :as mc])
  (:import jiksnu.model.Item
           org.bson.types.ObjectId))

(def collection-name "items")
(def maker #'model/map->Item)
(def default-page-size 20)

(def create-validators
  (validation-set
   (type-of :_id           ObjectId)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn push
  [user activity]
  (mc/insert collection-name {:user (:_id user)
                              :activity (:_id activity)}))
