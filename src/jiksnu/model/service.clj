(ns jiksnu.model.service
  (:require [ciste.config :refer [config]]
            [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.modules.core.templates.model :as templates.model]
            [jiksnu.modules.core.validators :as vc]
            [jiksnu.transforms :refer [set-updated-time set-created-time]]
            [jiksnu.util :as util]
            [monger.collection :as mc]
            [validateur.validation :as v])
  (:import (org.joda.time DateTime)
           (org.bson.types ObjectId)))

(def collection-name "services")
(def maker           #'model/map->Service)
(def page-size       20)

(defn statusnet-url
  [service]
  (str "http://" (:_id service) (:context service) "/api/statusnet/config.json"))

(def create-validators
  (v/validation-set
   (vc/type-of :_id ObjectId)
   #_(vc/type-of :local      Boolean)
   #_(vc/type-of :discovered Boolean)
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

;; TODO: add the links to the list
(defn add-links
  [service links]
  ;; TODO: This should push only if the link is not yet there
  (mc/update (db/get-connection) collection-name
             (select-keys service #{:_id})
             {:$pushAll {:links links}}))

(defn get-xrd-url
  [service user-uri]
  (when user-uri
    (when-let [template (or (:xrdTemplate service)
                            (some->> service
                                     :links
                                     (filter #(= (:rel %) "lrdd"))
                                     (filter #(or (nil? (:type %))
                                                  (= (:type %) "application/xrd+xml")))
                                     first
                                     :template))]
      (util/replace-template template user-uri))))

(defn get-jrd-url
  [service user-uri]
  (when user-uri
    (when-let [template (or (:jrdTemplate service)
                            (some->> service
                                     :links
                                     (filter #(= (:rel %) "lrdd"))
                                     (filter #(= (:type %) "application/json"))
                                     first
                                     :template))]
      (util/replace-template template user-uri))))
