(ns jiksnu.modules.core.model.conversation
  (:require [jiksnu.modules.core.db :as db]
            [jiksnu.modules.core.model :as model]
            [jiksnu.modules.core.templates.model :as templates.model]
            [jiksnu.modules.core.validators :as vc]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import (org.joda.time DateTime)
           (org.bson.types ObjectId)))

(def collection-name "conversations")
(def maker #'model/map->Conversation)
(def default-page-size 20)

(def create-validators
  (v/validation-set
   (vc/type-of :_id     ObjectId)
   #_(vc/type-of :url           String)
   #_(vc/type-of :domain        String)
   #_(vc/type-of :local         Boolean)
   #_(vc/type-of :update-source ObjectId)
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

(defn find-by-url
  [url]
  (fetch-all {:url url}))

(defn ensure-indexes
  []
  (doto collection-name
    (mc/ensure-index (db/get-connection) {:url 1} {:unique true})))
