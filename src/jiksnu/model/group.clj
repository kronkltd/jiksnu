(ns jiksnu.model.group
  (:require [jiksnu.db :refer [_db]]
            [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.validators :refer [type-of]]
            [monger.collection :as mc]
            [validateur.validation :refer [validation-set presence-of]]))

(def collection-name "groups")
(def maker           #'model/map->Group)
(def page-size       20)

(def create-validators
  (validation-set
   ;; (type-of :_id     ObjectId)
   ;; (type-of :name    String)
   ;; (type-of :created DateTime)
   ;; (type-of :updated DateTime)

   ;; (presence-of :members)
   ;; (presence-of :admins)
))

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
  (maker (mc/find-one-as-map @_db collection-name {:nickname name})))
