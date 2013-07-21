(ns jiksnu.model.dialback
  (:use [jiksnu.validators :only [type-of]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(defonce page-size 20)
(def collection-name "dialbacks")
(def maker model/map->Dialback)

(def create-validators
  (validation-set
   (type-of :_id                   ObjectId)
   (type-of :token                 String)
   (type-of :date               DateTime)
   (type-of :url                  String)

   ;; TODO: optional fields
   ;; :host      String
   ;; :webfinger String
   ;; TODO: webfinger and host are mutually exclusive
   ))

(def count-records (templates/make-counter       collection-name))
(def delete        (templates/make-deleter       collection-name))
(def drop!         (templates/make-dropper       collection-name))
(def remove-field! (templates/make-remove-field! collection-name))
(def set-field!    (templates/make-set-field!    collection-name))
(def fetch-by-id   (templates/make-fetch-by-id   collection-name maker))
(def create        (templates/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn      collection-name maker))
