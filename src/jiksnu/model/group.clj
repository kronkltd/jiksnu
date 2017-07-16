(ns jiksnu.model.group
  (:require [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.templates.model :as templates.model]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import (org.bson.types ObjectId)
           (org.joda.time DateTime)))

(def collection-name "groups")
(def maker           #'model/map->Group)
(def page-size       20)

(def create-validators
  (v/validation-set
   (vc/type-of :_id     ObjectId)
   (vc/type-of :name    String)
   #_(v/presence-of :members)
   #_(v/presence-of :admins)
   (vc/type-of :created DateTime)
   (vc/type-of :updated DateTime)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def push-value!   (templates.model/make-push-value!   collection-name))
(def pop-value!    (templates.model/make-pop-value!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name
                                                       #'fetch-by-id
                                                       #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn fetch-by-name
  [name]
  (maker (mc/find-one-as-map (db/get-connection) collection-name {:nickname name})))
