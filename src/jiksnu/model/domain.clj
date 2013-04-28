(ns jiksnu.model.domain
  (:use [ciste.config :only [config]]
        [jiksnu.transforms :only [set-updated-time set-created-time]]
        [jiksnu.validators :only [type-of]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of presence-of valid? validation-set]])
  (:require [clj-statsd :as s]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.templates :as templates]
            [jiksnu.util :as util]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Domain
           org.bson.types.ObjectId
           org.joda.time.DateTime))

(def collection-name "domains")
(def maker           #'model/map->Domain)
(def page-size       20)

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(def create-validators
  (validation-set
   (type-of :_id        String)
   (type-of :created    DateTime)
   (type-of :updated    DateTime)
   (type-of :local      Boolean)
   (type-of :discovered Boolean)))

(defn fetch-by-id
  [id]
  (s/increment "domains fetched")
  (if-let [item (mc/find-map-by-id collection-name id)]
    (maker item)))

(def count-records (templates/make-counter collection-name))
(def create        (templates/make-create  collection-name #'fetch-by-id #'create-validators))
(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (s/increment "domains searched")
     ((templates/make-fetch-fn maker collection-name)
      params options)))

(defn get-link
  [item rel content-type]
  (first (util/rel-filter rel (:links item) content-type)))

;; TODO: add the links to the list
(defn add-links
  [domain links]
  ;; TODO: This should push only if the link is not yet there
  (mc/update collection-name
    (select-keys domain #{:_id})
    {:$pushAll {:links links}}))

(def set-field! (templates/make-set-field! collection-name))

(defn ping-request
  [domain]
  {:type :get
   :to (tigase/make-jid "" (:_id domain))
   :from (tigase/make-jid "" (config :domain))
   :body (element/make-element ["ping" {"xmlns" "urn:xmpp:ping"}])})
