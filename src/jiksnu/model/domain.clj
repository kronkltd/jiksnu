(ns jiksnu.model.domain
  (:use (ciste [config :only [config]]
               [debug :only [spy]])
        jiksnu.model)
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.tools [logging :as log])
            (karras [entity :as entity]))
  (:import jiksnu.model.Domain))

(defn drop!
  []
  (entity/delete-all Domain))

(defn fetch-by-id
  [id]
  (entity/fetch-one Domain {:_id id}))

(defn index
  ([]
     (index {}))
  ([args]
     (entity/fetch Domain args)))

(defn fetch-all
  ([]
     (fetch-all {}))
  ([params & opts]
     (apply entity/fetch Domain params opts)))

(defn create
  [domain]
  (if (:_id domain)
    (do
      (log/debugf "Creating domain %s" (:_id domain))
      (entity/create Domain domain))
    (throw (IllegalArgumentException.
            (str "Domain must have id: " domain)))))

(defn update
  [domain]
  (entity/save domain))

(defn delete
  [domain]
  (let [domain (fetch-by-id (:_id domain))]
    (entity/delete domain)
    domain))

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

(defn pending-domains-key
  [domain]
  (str "pending.domains." domain))

(defn host-meta-link
  [domain]
  (str "http://" (:_id domain) "/.well-known/host-meta"))

(defn count-records
  ([] (count-records {}))
  ([params]
     (entity/count-instances Domain params)))
