(ns jiksnu.model.stream
  (:use [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(defonce page-size 20)
(def collection-name "streams")
(def maker model/map->Stream)

(def create-validators
  (validation-set
   (type-of :_id                   ObjectId)
   (type-of :title                 String)

   ;; (type-of :local                 Boolean)
   ;; (type-of :public                Boolean)

   (type-of :user                  ObjectId)

   (presence-of :created)
   ;; (type-of :created               DateTime)
   (presence-of :updated)
   ;; (type-of :updated               DateTime)
   ))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

