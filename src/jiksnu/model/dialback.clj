(ns jiksnu.model.dialback
  (:require [jiksnu.model :as model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.templates.model :as templates.model]
            [validateur.validation :as v])
  (:import (org.bson.types ObjectId)
           (org.joda.time DateTime)))

(defonce page-size 20)
(def collection-name "dialbacks")
(def maker model/map->Dialback)

;; TODO: optional fields
;; :host      String
;; :webfinger String
;; TODO: webfinger and host are mutually exclusive
(def create-validators
  (v/validation-set
   (vc/type-of :_id   ObjectId)
   (vc/type-of :token String)
   (vc/type-of :date  DateTime)
   (vc/type-of :url   String)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))
