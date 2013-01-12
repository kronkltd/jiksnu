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

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(defn pending-domains-key
  [domain]
  (str "pending.domains." domain))

(def create-validators
  (validation-set
   (type-of :_id        String)
   (type-of :created    DateTime)
   (type-of :updated    DateTime)
   (type-of :local      Boolean)
   (type-of :discovered Boolean)))

(def delete        (templates/make-deleter collection-name))
(def drop!         (templates/make-dropper collection-name))
(def count-records (templates/make-counter collection-name))

(defn fetch-by-id
  [id]
  (s/increment "domains fetched")
  (if-let [domain (mc/find-map-by-id collection-name id)]
    (model/map->Domain domain)))

(def create        (templates/make-create collection-name #'fetch-by-id #'create-validators))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (s/increment "domains searched")
     ((templates/make-fetch-fn model/map->Domain collection-name)
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
