(ns jiksnu.model.group-membership
  (:use [jiksnu.transforms :only [set-_id set-created-time
                                  set-updated-time]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [validation-set presence-of]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :as mq]
            [monger.result :as result])
  (:import jiksnu.model.Group
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "group_memberships")
(def maker           #'model/map->GroupMembership)
(def page-size       20)

(def create-validators
  (validation-set
   (type-of :_id     ObjectId)
   (type-of :created DateTime)
   (type-of :updated DateTime)))

(def count-records (templates/make-counter     collection-name))
(def delete        (templates/make-deleter     collection-name))
(def drop!         (templates/make-dropper     collection-name))
(def set-field!    (templates/make-set-field!  collection-name))
(def fetch-by-id   (templates/make-fetch-by-id collection-name maker))
(def create        (templates/make-create      collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn    collection-name maker))
