(ns jiksnu.model.domain
  (:use [ciste.config :only [config]]
        [jiksnu.transforms :only [set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [presence-of valid? validation-set]])
  (:require [clj-statsd :as s]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Domain))

(def collection-name "domains")

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(defn pending-domains-key
  [domain]
  (str "pending.domains." domain))

(def create-validators
  (validation-set
   (presence-of :_id)))

(defn set-discovered
  [record]
  (if (contains? record :discovered)
    record
    (assoc record :discovered (:local record))))

(def delete        (model/make-deleter collection-name))
(def drop!         (model/make-dropper collection-name))
(def count-records (model/make-counter collection-name))

(defn fetch-by-id
  [id]
  (s/increment "domains fetched")
  (if-let [domain (mc/find-map-by-id collection-name id)]
    (model/map->Domain domain)))

(defn create
  [domain & [options & _]]
  (let [errors (create-validators domain)]
    (if (empty? errors)
      (do
        (log/debugf "Creating domain: %s" domain)
        (s/increment "domains created")
        (mc/insert collection-name domain)
        (fetch-by-id (:_id domain)))
      (throw+ {:type :validation :errors errors}))))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (s/increment "domains searched")
     ((model/make-fetch-fn model/map->Domain collection-name)
      params options)))

(defn get-link
  [item rel content-type]
  (first (model/rel-filter rel (:links item) content-type)))

;; TODO: add the links to the list
(defn add-links
  [domain links]
  ;; TODO: This should push only if the link is not yet there
  (mc/update collection-name {:$pushAll {:links links}}))

(defn set-field
  [domain field value]
  (s/increment "domains field set")
  (mc/update collection-name
   {:_id (:_id domain)}
   {:$set {field value}}))

(defn ping-request
  [domain]
  {:type :get
   :to (tigase/make-jid "" (:_id domain))
   :from (tigase/make-jid "" (config :domain))
   :body (element/make-element ["ping" {"xmlns" "urn:xmpp:ping"}])})
