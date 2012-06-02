(ns jiksnu.model.domain
  (:use [ciste.config :only [config]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [presence-of valid? validation-set]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:import jiksnu.model.Domain))

(def collection-name "domains")

(def create-validators
  (validation-set
   (presence-of :_id)))

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  [id]
  (mc/find-map-by-id collection-name id))

(defn delete
  [domain]
  (let [domain (fetch-by-id (:_id domain))]
    (mc/remove-by-id collection-name (:_id domain))
    domain))

(defn create
  [domain]
  (let [[passed errors] (valid? create-validators domain)] 
    (if passed
      (do
        (log/debugf "Creating domain %s" (:_id domain))
        (mc/insert collection-name domain))
      (throw+ {:type :validation
               :errors errors}))))

;; TODO: deprecated
(defn index
  ([]
     (index {}))
  ([args]
     (mc/find-maps collection-name args)))

(defn fetch-all
  ([]
     (fetch-all {}))
  ([params opts]
     (mc/find-maps collection-name params)))

;; TODO: don't use
(defn update
  [domain]
  (mc/save collection-name domain))

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

(defn set-discovered
  [domain]
  (set-field domain :discovered true))

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
