(ns jiksnu.model.dialback
  (:require [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.validators :refer [type-of]]
            [validateur.validation :refer [validation-set presence-of]]))

(defonce page-size 20)
(def collection-name "dialbacks")
(def maker model/map->Dialback)

(def create-validators
  (validation-set
   ;; (type-of :_id                   ObjectId)
   ;; (type-of :token                 String)
   ;; (type-of :date               DateTime)
   ;; (type-of :url                  String)

   ;; TODO: optional fields
   ;; :host      String
   ;; :webfinger String
   ;; TODO: webfinger and host are mutually exclusive
   ))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))
