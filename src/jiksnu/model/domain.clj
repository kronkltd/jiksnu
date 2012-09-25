(ns jiksnu.model.domain
  (:use [ciste.config :only [config]]
        [jiksnu.transforms :only [set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [presence-of valid? validation-set]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Domain))

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(def collection-name "domains")

(def create-validators
  (validation-set
   (presence-of :_id)))

(defn set-discovered
  [record]
  (if (contains? record :discovered)
    record
    (assoc record :discovered false)))

(defn prepare
  [domain]
  (-> domain
      set-discovered
      set-created-time
      set-updated-time))

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  [id]
  (if-let [domain (mc/find-map-by-id collection-name id)]
    (model/map->Domain domain)))

(defn delete
  [domain]
  (mc/remove-by-id collection-name (:_id domain))
  domain)

(defn create
  [domain & [options & _]]
  (let [domain (prepare domain)
        errors (create-validators domain)]
    (if (empty? errors)
      (do
        (log/debugf "Creating domain: %s" domain)
        (mc/insert collection-name domain)
        (fetch-by-id (:_id domain)))
      (throw+ {:type :validation :errors errors}))))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
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
  (mc/update collection-name
   {:_id (:_id domain)}
   {:$set {field value}}))

;; (defn set-discovered
;;   [domain]
;;   (set-field domain :discovered true))

(defn ping-request
  [domain]
  {:type :get
   :to (tigase/make-jid "" (:_id domain))
   :from (tigase/make-jid "" (config :domain))
   :body (element/make-element ["ping" {"xmlns" "urn:xmpp:ping"}])})

(defn pending-domains-key
  [domain]
  (str "pending.domains." domain))

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
