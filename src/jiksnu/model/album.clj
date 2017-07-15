(ns jiksnu.model.album
  (:require [jiksnu.model :as model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.templates.model :as templates.model]
            [validateur.validation :refer [validation-set presence-of]])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(defonce page-size 20)
(def collection-name "albums")
(def maker model/map->Album)

(def create-validators
  (validation-set
   (vc/type-of :_id     ObjectId)
   (vc/type-of :name    String)
   (vc/type-of :owner   String)
   (vc/type-of :created DateTime)
   (vc/type-of :updated DateTime)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))
