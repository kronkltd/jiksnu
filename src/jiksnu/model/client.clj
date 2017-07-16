(ns jiksnu.model.client
  (:require [jiksnu.model :as model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.templates.model :as templates.model]
            [validateur.validation :as v])
  (:import (org.joda.time DateTime)))

(def collection-name "clients")
(def maker           #'model/map->Client)
(def page-size       20)

(def create-validators
  (v/validation-set
   (vc/type-of :_id            String)
   #_(vc/type-of :owner          String)
   #_(vc/type-of :type           String)
   #_(vc/type-of :secret         String)
   #_(vc/type-of :secret-expires Long)
   (vc/type-of :created DateTime)))

(def count-records (templates.model/make-counter       collection-name))
(def delete        (templates.model/make-deleter       collection-name))
(def drop!         (templates.model/make-dropper       collection-name))
(def remove-field! (templates.model/make-remove-field! collection-name))
(def set-field!    (templates.model/make-set-field!    collection-name))
(def fetch-by-id   (templates.model/make-fetch-by-id   collection-name maker false))
(def create        (templates.model/make-create        collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates.model/make-fetch-fn      collection-name maker))
