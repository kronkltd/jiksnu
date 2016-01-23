(ns jiksnu.model.item
  (:require [jiksnu.db :refer [_db]]
            [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.validators :refer [type-of]]
            [monger.collection :as mc]
            [validateur.validation :refer [validation-set]]))

(def collection-name "items")
(def maker #'model/map->Item)
(def default-page-size 20)

(def create-validators
  (validation-set
   ;; (type-of :_id           ObjectId)
))

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
  (mc/insert @_db collection-name {:user (:_id user)
                              :activity (:_id activity)}))
