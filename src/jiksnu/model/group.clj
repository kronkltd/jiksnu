(ns jiksnu.model.group
  (:use [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [monger.collection :as mc]
            [monger.query :as mq])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "groups")
(def maker           #'model/map->Group)
(def page-size       20)

(def create-validators
  (validation-set
   (type-of :_id     ObjectId)
   (type-of :created DateTime)
   (type-of :updated DateTime)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))

(defn fetch-by-name
  [name]
  (maker (mc/find-one-as-map collection-name {:nickname name})))
