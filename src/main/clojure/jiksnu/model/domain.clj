(ns jiksnu.model.domain
  (:use ciste.debug
        jiksnu.model)
  (:require [karras.entity :as entity])
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
  (entity/create Domain domain))

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
