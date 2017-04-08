(ns jiksnu.model.client
  (:require [jiksnu.model :as model]
            [jiksnu.templates.model :as templates.model]
            [jiksnu.validators :refer [type-of]]
            [validateur.validation :refer [validation-set]])
  (:import (org.joda.time DateTime)))

(def collection-name "clients")
(def maker           #'model/map->Client)
(def page-size       20)

(def create-validators
  (validation-set
   (type-of :_id            String)
    ;; (type-of :owner          String)
   #_(type-of :type           String)
   #_(type-of :secret         String)
   #_(type-of :secret-expires Long)
   (type-of :created DateTime)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker false))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))
