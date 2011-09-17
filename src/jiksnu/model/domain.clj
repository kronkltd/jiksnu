(ns jiksnu.model.domain
  (:use (ciste [config :only (config)]
               [debug :only (spy)])
        jiksnu.model)
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.tools [logging :as log])
            (karras [entity :as entity]))
  (:import jiksnu.model.Domain))

(defn drop!
  []
  (entity/delete-all Domain))

(defn show
  [id]
  (entity/fetch-one Domain {:_id id}))

(defn index
  ([]
     (index {}))
  ([args]
     (entity/fetch Domain args)))

(defn create
  [domain]
  (if (:_id domain)
    (entity/create Domain domain)
    (throw (IllegalArgumentException. "Domain must have id"))))

(defn update
  [domain]
  (entity/save domain))

(defn delete
  [id]
  (let [domain (show id)]
    (entity/delete domain)
    domain))

(defn find-or-create
  [id]
  (or (show id)
      (create {:_id id})))

;; TODO: add the links to the list
(defn add-links
  [domain links]
  (update (assoc domain :links links)))

(defn set-field
  [domain field value]
  (entity/find-and-modify
   Domain
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
