(ns jiksnu.modules.core.model.authentication-mechanism
  (:require [jiksnu.modules.core.db :as db]
            [jiksnu.modules.core.model :as model]
            [jiksnu.modules.core.templates.model :as templates.model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.util :as util]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import (org.bson.types ObjectId)
           (org.joda.time DateTime)))

(def collection-name "authentication_mechanisms")

(def set-field! (templates.model/make-set-field! collection-name))

(def create-validators
  (v/validation-set
   (vc/type-of :_id ObjectId)
   #_(vc/type-of :local      Boolean)
   #_(vc/type-of :discovered Boolean)
   (vc/type-of :created DateTime)
   (vc/type-of :updated DateTime)))

(defn fetch-by-id
  [id]
  (let [id (if (string? id) (util/make-id id) id)]
    (if-let [item (mc/find-map-by-id (db/get-connection) collection-name id)]
      (model/map->AuthenticationMechanism item))))

(def create        (templates.model/make-create collection-name #'fetch-by-id #'create-validators))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
   (map model/map->AuthenticationMechanism
        (mc/find-maps (db/get-connection) collection-name params))))

(defn fetch-by-user
  [user & options]
  (apply fetch-all {:user (:_id user)} options))

(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def count-records (templates.model/make-counter       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
