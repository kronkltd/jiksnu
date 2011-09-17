(ns jiksnu.model.domain
  (:use (ciste [debug :only (spy)])
        jiksnu.model)
  (:require (clojure.tools [logging :as log])
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
    (log/error "Domain must have id")))

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
  (if-let [domain (show id)]
    domain
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
