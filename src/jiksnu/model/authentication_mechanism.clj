(ns jiksnu.model.authentication-mechanism
  (:use [jiksnu.validators :only [type-of]]
        [validateur.validation :only [acceptance-of presence-of valid? validation-set]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.util :as util]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:import jiksnu.model.AuthenticationMechanism
           org.bson.types.ObjectId))

(def collection-name "authentication_mechanisms")

(def set-field! (templates.model/make-set-field! collection-name))

(def create-validators
  (validation-set
   ;; (type-of :_id        ObjectId)
   ;; ;; (type-of :created    DateTime)
   ;; ;; (type-of :updated    DateTime)
   ;; ;; (type-of :local      Boolean)
   ;; ;; (type-of :discovered Boolean)
   ))

(defn fetch-by-id
  [id]
  (let [id (if (string? id) (util/make-id id) id)]
    (if-let [item (mc/find-map-by-id collection-name id)]
      (model/map->AuthenticationMechanism item))))

(def create        (templates.model/make-create collection-name #'fetch-by-id #'create-validators))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (map model/map->AuthenticationMechanism
          (mc/find-maps collection-name params))))

(defn fetch-by-user
  [user & options]
  (apply fetch-all {:user (:_id user)} options))

(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def count-records (templates.model/make-counter       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))

